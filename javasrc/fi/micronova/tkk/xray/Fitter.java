package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import fi.micronova.tkk.xray.octif.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.chart.*;

import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import java.util.List;



/** Thread for automatic fitting.
 *
 * This is the high-level interface to Octave fitting code. It does the actual
 * interfacing in a separate thread in order to allow normal use of the program
 * while an automatic fitting is in progress. Results are reported to tasks
 * that are invoked in the event thread.
 */

public class Fitter {
    private Thread t;
    private Oct oct;
    private JPlotArea light;
    private LayerStack stack;
    private Image green, yellow;
    private LayerTask endTask;
    private LayerTask plotTask;
    private Runnable errTask;
    private GraphData data;
    private Algorithm algo;
    private volatile boolean closing = false;
    private int iterations;
    private List<Layer> layerList;


    /** Constructor.
     *
     * Fit the layer model represented by the parameter stack to the
     * measurement data in the parameter data in another thread. An internal
     * copy of the stack and the data is made, so they can be used without
     * having to worry about thread synchronization.
     *
     * The fitting is implemented in Octave code, so the fitting thread needs
     * an exclusive access to oct. Before accessing oct, a lock must always be
     * obtained.
     *
     * Progress is reported by periodically calling plotTask so that fitting
     * progress can be plotted in the user interface.
     *
     * After the fitting is completed, endTask is called. In the case of an
     * error, errTask is called instead.
     *
     * @param light A light which is either yellow (during fitting) or green.
     *              The color is changed automatically by this thread.
     * @param green A green image for the light
     * @param yellow A yellow image for the light
     * @param oct Octave
     * @param data The measurement data used to fit the layer model
     * @param stack The layer model to fit
     *
     * @param endTask Called after the fitting is complete
     * @param plotTask Called periodically to report fitting progress
     * @param errTask Called when an Octave error has occurred during the fitting
     *
     * @param popsize Option for the fitting code: population size
     * @param iterations Option for the fitting code: the number of iterations
     * @param firstAngle Option for the fitting code: the minimum angle to include in fitting
     * @param lastAngle Option for the fitting code: the maximum angle to include in fitting
     * @param algo Option for the fitting code: the algorithm to use
     *
     * @throws OctException If an Octave error has occurred during the preparation for the fitting
     *
     */

    public Fitter(JPlotArea light, Oct oct, GraphData data, LayerTask endTask, LayerTask plotTask, Runnable errTask, LayerStack stack, int popsize, int iterations, double firstAngle, double lastAngle, Image green, Image yellow,
            Algorithm algo, FitnessFunction func, double dBthreshold, int pNorm) throws OctException, SimulationException {
        stack = stack.deepCopy();
        data = data.convertToLinear();
        this.data = data;
        this.green = green;
        this.yellow = yellow;
        this.light = light;
        this.oct = oct;
        this.endTask = endTask;
        this.plotTask = plotTask;
        this.errTask = errTask;
        this.iterations = iterations;
        this.stack = stack;
        closing = false;
        t = new Thread(new Runnable() {
            public void run() {
                runThread();
            }
        });

        /* the stack may have multiple references to the same layer object */
        /* the ids used here start from 0. Octave code uses ids starting from 1 */
        int unused_id = 0;
        Map<Layer,Integer> numbering = new HashMap<Layer,Integer>();
        List<Layer> tempLayerList = new ArrayList<Layer>(); /* no duplicates, layerList.get(numbering.get(l)) == l */
        List<Integer> mixing = new ArrayList<Integer>();
        for(Layer l: stack.getLayers()) {
            int id;
            if(numbering.containsKey(l)) {
                id = numbering.get(l);
            } else {
                id = unused_id++;
                numbering.put(l,id);
                tempLayerList.add(l);
            }
            mixing.add(id);
        }
        for(int i=0; i<tempLayerList.size(); i++)
            assert(numbering.get(tempLayerList.get(i)) == i);
        numbering = Collections.unmodifiableMap(numbering);
        this.layerList = Collections.unmodifiableList(tempLayerList);
        mixing = Collections.unmodifiableList(mixing);


        synchronized(oct) {
            double lambda = stack.getLambda();

            oct.putRowVector("alpha_0",data.alpha_0);
            oct.putRowVector("meas",data.meas);
            oct.putScalar("lambda",lambda);

            if(DataTools.isUniformlySpaced(data.alpha_0))
                oct.putScalar("stddevrad",stack.getStdDev().getExpected());
            else
                oct.putScalar("stddevrad",0);

            /* TODO: check we have >0 points to fit */
            oct.execute("[fitdeg,fitmeas] = crop(alpha_0,meas,"+firstAngle+","+lastAngle+")");
            oct.execute("ndata = length(fitdeg)");
            if(oct.getMatrix("ndata")[0][0] < 2)
                throw new NoDataPointsException("Too few data points in the fitting range");

            String dMinCmd = "g_d_min = [";
            String dCmd = "g_d = [";
            String dMaxCmd = "g_d_max = [";

            String pMinCmd = "g_p_min = [";
            String pCmd = "g_p = [";
            String pMaxCmd = "g_p_max = [";

            String rMinCmd = "g_r_min = [";
            String rCmd = "g_r = [";
            String rMaxCmd = "g_r_max = [";

            String mixingCmd = "g_indices = [";

            String mixturesCmd = "g_mixtures = { ...\n";

            for(Layer l: layerList) {
                dCmd += l.getThickness().getExpected()+",";
                if(l.getThickness().getEnabled()) {
                    dMinCmd += l.getThickness().getMin()+",";
                    dMaxCmd += l.getThickness().getMax()+",";
                } else {
                    dMinCmd += l.getThickness().getExpected()+",";
                    dMaxCmd += l.getThickness().getExpected()+",";
                }

                pCmd += l.getComposition().getExpected()+",";
                if(l.getComposition().getEnabled()) {
                    pMinCmd += l.getComposition().getMin()+",";
                    pMaxCmd += l.getComposition().getMax()+",";
                } else {
                    pMinCmd += l.getComposition().getExpected()+",";
                    pMaxCmd += l.getComposition().getExpected()+",";
                }

                rCmd += l.getRelaxation().getExpected()+",";
                if(l.getRelaxation().getEnabled()) {
                    rMinCmd += l.getRelaxation().getMin()+",";
                    rMaxCmd += l.getRelaxation().getMax()+",";
                } else {
                    rMinCmd += l.getRelaxation().getExpected()+",";
                    rMaxCmd += l.getRelaxation().getExpected()+",";
                }

                /* in order to handle all possible cases we must flatten the material and make
                 * a fixed mixture of it */
                mixturesCmd += "cell2struct({0, ...\n";
                mixturesCmd += "{1.0, " + l.getMat1().flatten().octaveRepr(lambda) + "} ...\n";
                mixturesCmd += "{1.0, " + l.getMat2().flatten().octaveRepr(lambda) + "} ...\n";
                mixturesCmd += "},{'x','first','second'},2), ...\n";
            }
            for(int i: mixing)
                mixingCmd += (i+1)+",";
            dMinCmd += "]";
            dCmd += "]";
            dMaxCmd += "]";
            pMinCmd += "]";
            pCmd += "]";
            pMaxCmd += "]";
            rMinCmd += "]";
            rCmd += "]";
            rMaxCmd += "]";
            mixingCmd += "]";
            mixturesCmd += "}";

            /*
            mixturesCmd += "cell2struct({0, ...\n";
            mixturesCmd += "{1.0, " + stack.getSubstrate().flatten().octaveRepr(lambda) + "} ...\n";
            mixturesCmd += "{1.0, " + stack.getSubstrate().flatten().octaveRepr(lambda) + "} ...\n";
            mixturesCmd += "},{'x','first','second'},2)}";
            */

            oct.execute(dMinCmd);
            oct.execute(dCmd);
            oct.execute(dMaxCmd);
            oct.execute(pMinCmd);
            oct.execute(pCmd);
            oct.execute(pMaxCmd);
            oct.execute(rMinCmd);
            oct.execute(rCmd);
            oct.execute(rMaxCmd);
            if(stack.getProd().getEnabled()) {
                oct.execute("g_prodfactor_min = "+stack.getProd().getMin());
                oct.execute("g_prodfactor = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor_max = "+stack.getProd().getMax());
            } else {
                oct.execute("g_prodfactor_min = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor = "+stack.getProd().getExpected());
                oct.execute("g_prodfactor_max = "+stack.getProd().getExpected());
            }
            if(stack.getOffset().getEnabled()) {
                oct.execute("g_thetaoffset_min = "+stack.getOffset().getMin());
                oct.execute("g_thetaoffset = "+stack.getOffset().getExpected());
                oct.execute("g_thetaoffset_max = "+stack.getOffset().getMax());
            } else {
                oct.execute("g_thetaoffset_min = "+stack.getOffset().getExpected());
                oct.execute("g_thetaoffset = "+stack.getOffset().getExpected());
                oct.execute("g_thetaoffset_max = "+stack.getOffset().getExpected());
            }
            if(stack.getSum().getEnabled()) {
                oct.execute("g_sumterm_min = "+stack.getSum().getMin());
                oct.execute("g_sumterm = "+stack.getSum().getExpected());
                oct.execute("g_sumterm_max = "+stack.getSum().getMax());
            } else {
                oct.execute("g_sumterm_min = "+stack.getSum().getExpected());
                oct.execute("g_sumterm = "+stack.getSum().getExpected());
                oct.execute("g_sumterm_max = "+stack.getSum().getExpected());
            }
            oct.execute(mixturesCmd);
            oct.execute(mixingCmd);

            oct.sync();

            this.algo = algo;
            if(algo.isDE) {
                oct.execute("g_g.pnorm = "+pNorm);
                oct.execute("g_g.threshold = 10^(("+dBthreshold+")/10)");
                oct.execute("g_ctx = fitDE_initXRD(fitdeg*pi/180, fitmeas, g_mixtures, lambda, stddevrad, g_r_min, g_r, g_r_max, g_d_min, g_d, g_d_max, g_p_min, g_p, g_p_max, g_prodfactor_min, g_prodfactor, g_prodfactor_max, g_sumterm_min, g_sumterm, g_sumterm_max, g_thetaoffset_min, g_thetaoffset, g_thetaoffset_max, g_indices, '"+oct.escape(algo.octName)+"', "+popsize+",@"+func.octName+", g_g)");
                oct.sync();
            } else {
                throw new RuntimeException("Not yet supported");
                /*
                oct.execute("xrrGA('"+oct.escape(algo.octName)+"','mode')");
                oct.sync();
                //oct.execute("xrrGA("+(nonlinear ? "'yes'" : "'no'")+",'nonlinear')");
                //oct.sync();
                oct.execute("xrrGA([fitdeg;fitmeas],'measurement')");
                oct.sync();
                oct.execute("xrrGA([g_d;g_rho_e;g_r;g_dd;g_drho_e;g_dr;g_beta_coeff],'physics')");
                oct.sync();
                oct.execute("xrrGA(lambda,'lambda')");
                oct.sync();
                oct.execute("xrrGA('EFICA','ICAmode')");
                oct.sync();
                oct.execute("xrrGA(stddevrad,'stddevrad')");
                oct.sync();
                oct.execute("xrrGA("+iterations+",'cycleMax')");
                oct.sync();
                oct.execute("g_GAdata = xrrGA("+popsize+",'initialize')");
                oct.sync();
                */
            }
        }
        t.start();
    }

    /** Stop the fitting without waiting. */
    public void closeWithoutWaiting() {
        closing = true;
    }

    /** Stop the fitting and wait. */
    public void close() {
        boolean ok = false;
        closing = true;
        while(!ok) {
            try {
                t.join();
                ok = true;
            }
            catch(InterruptedException e) {}
        }
    }


    /* This methods runs in another thread.
     * It acquires the following locks:
     * - oct, when octave is called
     */
    private void runThread() {
        light.newImage(yellow);
        try {
            for(int round = 0; round < iterations && !closing; round++) {
                double[] results;
                String msg;
                synchronized(oct) {
                    oct.sync();
                    if(algo.isDE) {
                        double bestfit, medianfit;
                        oct.execute("g_ctx = fitDE(g_ctx)");
                        oct.execute("fitresults = fitDE_best(g_ctx)");
                        oct.execute("bestfitness = fitDE_best_fitness(g_ctx)");
                        oct.execute("medianfitness = fitDE_median_fitness(g_ctx)");
                        bestfit = oct.getMatrix("bestfitness")[0][0];
                        medianfit = oct.getMatrix("medianfitness")[0][0];
                        msg = "iteration = "+(round+1) + ", bestfit = " + String.format(Locale.US,"%.4g",bestfit) + ", medianfit = "+String.format(Locale.US,"%.4g",medianfit);
                    } else {
                        throw new RuntimeException("Not yet supported");
                        /*
                        oct.execute("g_GAdata = xrrGA(g_GAdata,'GA')");
                        //System.out.println("?");
                        oct.sync();
                        //System.out.println("!");
                        results = oct.getMatrix("g_GAdata")[0];
                        msg = "iteration = "+(round+1);
                        assert(results.length == 3*(stack.getSize()+1)+1);
                        oct.execute("fitresults = [g_d-g_dd,g_rho_e-g_drho_e,g_r-g_dr]+2*g_GAdata(1,1:end-1).*[g_dd,g_drho_e,g_dr]");
                        */
                    }
                    results = oct.getMatrix("fitresults")[0];
                    assert(results.length == 3*layerList.size()+3); /* XXX */
                }
                int size = layerList.size();
                for(int i=0; i<size; i++) {
                    Layer l = layerList.get(i);

                    double d = results[3+0*(size)+i];
                    double p = results[3+1*(size)+i];
                    double r = results[3+2*(size)+i];


                    /* Debugging code: we don't use it because of the limited accuracy of floating point arithmetic */
                    /*
                    if(d < l.getThickness().getMin() || d > l.getThickness().getMax())
                        System.out.println("thickness not in range, min = "+l.getThickness().getMin()+
                                ", val = "+d+", max = "+l.getThickness().getMax()+", layer = "+l);
                    if(rho < l.getDensity().getMin() || rho > l.getDensity().getMax())
                        System.out.println("density not in range, min = "+l.getDensity().getMin()+
                                ", val = "+rho+", max = "+l.getDensity().getMax()+", layer = "+l);
                    if(r < l.getRoughness().getMin() || r > l.getRoughness().getMax())
                        System.out.println("roughness not in range, min = "+l.getRoughness().getMin()+
                                ", val = "+r+", max = "+l.getRoughness().getMax()+", layer = "+l);
                    */

                    if(l.getThickness().getEnabled())
                      l.getThickness().setExpected(d);
                    if(l.getComposition().getEnabled())
                      l.getComposition().setExpected(p);
                    if(l.getRelaxation().getEnabled())
                      l.getRelaxation().setExpected(r);
                }
                if(stack.getProd().getEnabled())
                  stack.getProd().setExpected(results[0]);
                if(stack.getSum().getEnabled())
                  stack.getSum().setExpected(results[1]);
                if(stack.getOffset().getEnabled())
                  stack.getOffset().setExpected(results[2]);

                final String msg2 = msg;

                final LayerStack stackToPlot = stack.deepCopy();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(plotTask != null)
                            plotTask.run(stackToPlot,msg2);
                    }
                });
            }
        }
        catch(OctException ex) {
            SwingUtilities.invokeLater(errTask);
            light.newImage(green);
            return;
        }
        final LayerStack stackToReturn = stack.deepCopy();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if(plotTask != null)
                    plotTask.run(stackToReturn,"");
                if(endTask != null)
                    endTask.run(stackToReturn,"");
            }
        });
        light.newImage(green);
    }
}
