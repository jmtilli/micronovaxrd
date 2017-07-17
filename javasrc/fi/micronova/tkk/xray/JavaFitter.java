package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.chart.*;
import fi.micronova.tkk.xray.xrdde.*;
import fi.micronova.tkk.xray.de.*;

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

public class JavaFitter implements FitterInterface {
    private long start;
    private Thread t;
    private ExecutorService exec;
    private JPlotArea light;
    private LayerStack stack;
    private Image green, yellow;
    private LayerTask endTask;
    private LayerTask plotTask;
    private Runnable errTask;
    private Algorithm algo;
    private volatile boolean closing = false;
    private int iterations;
    private List<Layer> layerList;
    private XRDFittingCtx ctx;
    private boolean autostop;
    private int autostopFigures;



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

    public JavaFitter(JPlotArea light, GraphData data, LayerTask endTask, LayerTask plotTask, Runnable errTask, LayerStack stack, int popsize, int iterations, double firstAngle, double lastAngle, Image green, Image yellow,
            Algorithm algo, FitnessFunction func, double dBthreshold, int pNorm,
            boolean autostop, int autostopFigures,
            AdvancedFitOptions opts)
            throws SimulationException, FittingNotStartedException
    {
        FittingErrorFunc func2;
        boolean ok = false;
        stack = stack.deepCopy();
        data = data.convertToLinear();
        data = data.crop(firstAngle, lastAngle);
        if (data.alpha_0.length < 2)
        {
            throw new FittingNotStartedException();
        }
        this.green = green;
        this.yellow = yellow;
        this.light = light;
        this.endTask = endTask;
        this.plotTask = plotTask;
        this.errTask = errTask;
        this.iterations = iterations;
        this.autostop = autostop;
        this.autostopFigures = autostopFigures;
        this.stack = stack;
        this.start = System.nanoTime();
        closing = false;
        if (stack.getLayers().isEmpty())
        {
            throw new FittingNotStartedException();
        }

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
        int cpus = Runtime.getRuntime().availableProcessors();
        this.exec = new ThreadPoolExecutor(cpus, cpus,
                                           1, TimeUnit.SECONDS,
                                           new LinkedBlockingQueue<Runnable>());
        switch (func)
        {
          case relchi2:
            func2 = new RelChi2FittingErrorFunc(Math.exp(Math.log(10)*dBthreshold/10));
            break;
          case logfitness:
            func2 = new LogFittingErrorFunc(pNorm);
            break;
          case sqrtfitness:
            func2 = new SqrtFittingErrorFunc(pNorm);
            break;
          case chi2:
            func2 = new Chi2FittingErrorFunc();
            break;
          default:
            throw new IllegalArgumentException();
        }
        try {
            this.ctx = new XRDFittingCtx(stack, data,
                                         algo == Algorithm.JavaCovDE,
                                         algo != Algorithm.JavaEitherOrDE,
                                         popsize, func2, exec, opts);
            ok = true;
        }
        finally
        {
            if (!ok)
            {
                exec.shutdown();
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
        int round = 0;
        double bestfit = 1e6;
        light.newImage(yellow);
        try {
            while (!closing) {
                double[] results;
                double medianfit, worstfit;
                String msg;
                ctx.iteration();
                bestfit = ctx.bestFittingError();
                medianfit = ctx.medianFittingError();
                worstfit = ctx.worstFittingError();
                results = ctx.bestIndividual();
                stack.setFitValues(results);

                msg = "iteration = "+(round+1) + ", bestfit = " + String.format(Locale.US,"%.7g",bestfit) + ", medianfit = "+String.format(Locale.US,"%.7g",medianfit);

                final String msg2 = msg;

                final LayerStack stackToPlot = stack.deepCopy();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if(plotTask != null)
                            plotTask.run(stackToPlot,msg2);
                    }
                });
                round++;
                if (!autostop && round >= iterations)
                {
                    break;
                }
                if (autostop && worstfit/bestfit - 1 < Math.pow(0.1,autostopFigures))
                {
                    break;
                }
            }
        }
        catch(Exception ex) {
            SwingUtilities.invokeLater(errTask);
            light.newImage(green);
            this.exec.shutdown();
            return;
        }
        this.exec.shutdown();
        final int finalRound = round;
        final double finalBestfit = bestfit;
        final LayerStack stackToReturn = stack.deepCopy();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String msg = "";
                if(ctx.reportPerf())
                {
                    long end = System.nanoTime();
                    msg = "Fitting took " +
                        String.format("%.2f", (end - start) / 1e9) +
                        " seconds and " + finalRound + " iterations" +
                        " to obtain fitting error value " +
                        String.format(Locale.US,"%.4g",finalBestfit);
                }
                if(plotTask != null)
                    plotTask.run(stackToReturn,"");
                if(endTask != null)
                    endTask.run(stackToReturn,msg);
            }
        });
        light.newImage(green);
    }
}
