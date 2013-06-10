package fi.micronova.tkk.xray;
import java.util.*;
import java.io.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.octif.*;





/** Stores alpha_0, measurement and simulation in linear or logarithmic format.
 *
 * This class is used to store alpha_0, meas and simul in one place. These may
 * be stored in linear or logarithmic format and may be converted between these
 * formats by this class.
 *
 * To achieve thread safety, no field may be assigned outside of this class. A
 * copy must be made by convertToDB or convertToLinear before using these
 * fields in a thread while another thread may be calling newData. After a
 * GraphData object is constructed, the arrays alpha_0, meas and simul must not
 * be written to.
 */
public class GraphData {
    /* Even though these fields are not read-only, they (and the objects they refer to)
     * must be considered read-only */
    /* alpha_0 is in degrees */
    public double[] alpha_0, meas, simul;
    public boolean logformat;

    /** Creates a GraphData in linear or logarithmic format.
     *
     * alpha_0 is in degrees
     *
     * The arrays must be of equal length and may be null.
     */
    public GraphData(double[] alpha_0, double[] meas, double[] simul, boolean logformat) {
        newData(alpha_0, meas, simul, logformat);
    }
    /** Creates a GraphData in linear format.
     *
     * alpha_0 is in degrees
     *
     * The arrays must be of equal length and may be null.
     */
    public GraphData(double[] alpha_0, double[] meas, double[] simul) {
        this(alpha_0, meas, simul, false);
    }

    /** Adds noise to the measured data.
     *
     * @param photon linear intensity of a photon
     */
    public GraphData addNoise(double photon) {
        Random gen = new Random();
        GraphData lin = convertToLinear();
        for(int i=0; i<lin.meas.length; i++) {
            /* we should use a Poisson-distributed variable but Java doesn't seem
             * to offer such a random number generator */
            lin.meas[i] /= photon;
            lin.meas[i] += gen.nextGaussian()*Math.sqrt(lin.meas[i]);
            if(lin.meas[i] <= 0)
                lin.meas[i] = 0;
            lin.meas[i] = (int)lin.meas[i];
            lin.meas[i] *= photon;
        }
        return lin;
    }

    /** Changes the fields of GraphData.
     *
     * alpha_0 is in degrees
     *
     * The arrays must be of equal length and may be null. This method is thread-safe.
     */
    synchronized public void newData(double[] alpha_0, double[] meas, double[] simul, boolean logformat) {
        if(alpha_0 != null && meas != null)
            assert(alpha_0.length == meas.length);
        if(alpha_0 != null && simul != null)
            assert(alpha_0.length == simul.length);
        if(meas != null && simul != null)
            assert(meas.length == simul.length);
        this.alpha_0 = alpha_0;
        this.meas = meas;
        this.simul = simul;
        this.logformat = logformat;
    }

    /** Makes a copy of GraphData in a logarithmic format.
     *
     * This method is thread-safe.
     *
     * @return a new object, which can therefore be used from the calling thread without worrying about thread safety
     */
    synchronized public GraphData convertToDB() {
        if(logformat) {
            return new GraphData(alpha_0, meas, simul, true);
        } else {
            double[] newmeas = null, newsimul = null;
            if(meas != null) {
              newmeas = new double[meas.length];
              for(int i=0; i<meas.length; i++)
                  newmeas[i] = 10*Math.log(meas[i])/Math.log(10);
            }
            if(simul != null) {
              newsimul = new double[simul.length];
              for(int i=0; i<simul.length; i++)
                  newsimul[i] = 10*Math.log(simul[i])/Math.log(10);
            }
            return new GraphData(alpha_0, newmeas, newsimul, true);
        }
    }

    /** Makes a copy of GraphData in a linear format.
     *
     * This method is thread-safe.
     *
     * @return a new object, which can therefore be used from the calling thread without worrying about thread safety
     */
    synchronized public GraphData convertToLinear() {
        if(!logformat) {
            return new GraphData(alpha_0, meas, simul, false);
        } else {
            double[] newmeas = null, newsimul = null;
            if(meas != null) {
              newmeas = new double[meas.length];
              for(int i=0; i<meas.length; i++)
                  newmeas[i] = Math.exp(Math.log(10)*meas[i]/10);
            }
            if(simul != null) {
              newsimul = new double[simul.length];
              for(int i=0; i<simul.length; i++)
                  newsimul[i] = Math.exp(Math.log(10)*simul[i]/10);
            }
            return new GraphData(alpha_0, newmeas, newsimul, false);
        }
    }

    /** Makes a simulated copy of GraphData in a linear format.
     *
     * This method is thread-safe. Alpha_0 and meas are copied from this
     * object, but simul is a new simulation of the tempStack layer model
     *
     * @return a new object, which can therefore be used from the calling
     *         thread without worrying about thread safety
     */
    public GraphData simulate(LayerStack tempStack) throws SimulationException {
        double[] alpha_0, alpha0rad, meas, simul;
        GraphData linear = convertToLinear();

        tempStack = tempStack.deepCopy();

        alpha_0 = linear.alpha_0;
        meas = linear.meas;

        alpha0rad = new double[alpha_0.length];
        for(int i=0; i<alpha0rad.length; i++) {
            alpha0rad[i] = alpha_0[i]*Math.PI/180;
        }

        simul = tempStack.xrdCurve(alpha0rad);

        return new GraphData(alpha_0, meas, simul);
    }

    public GraphData octSimulate(LayerStack tempStack, Oct oct) throws SimulationException, OctException {
        double[] alpha_0, alpha0rad, meas, simul;
        GraphData linear = convertToLinear();

        tempStack = tempStack.deepCopy();

        alpha_0 = linear.alpha_0;
        meas = linear.meas;

        alpha0rad = new double[alpha_0.length];
        for(int i=0; i<alpha0rad.length; i++) {
            alpha0rad[i] = alpha_0[i]*Math.PI/180;
        }

        simul = tempStack.octXRDCurve(oct, alpha0rad);

        return new GraphData(alpha_0, meas, simul);
    }














    /*
    private GraphData octSimulate(Oct oct, LayerStack tempStack) throws OctException {
        synchronized(oct) {
            double stddevrad = tempStack.getStdDev()*Math.PI/180;
            oct.putScalar("stddevrad",stddevrad);

            String dCmd = "d = [0,";
            String rCmd = "r = [";
            String rhoECmd = "rho_e = [0,";
            String betaCmd = "beta_coeff = [0,";
            int size = tempStack.getSize();
            for(int i=0; i<tempStack.getSize(); i++) {
                Layer l = tempStack.getElementAt(i);
                dCmd += l.getThickness().getExpected()+",";
                rCmd += l.getRoughness().getExpected()+",";
                rhoECmd += l.getDensity().getExpected()*l.getXRRCompound().getRhoEPerRho()+",";
                betaCmd += l.getXRRCompound().getBetaPerDelta()+",";
            }
            dCmd += "]";
            rCmd += "0]";
            rhoECmd += "]";
            betaCmd += "]";
            oct.execute(dCmd);
            oct.execute(rCmd);
            oct.execute(rhoECmd);
            oct.execute(betaCmd);
            oct.putScalar("lambda",tempStack.getLambda());
            oct.putRowVector("alpha_0",alpha_0);
            oct.execute("simul = xrrCurve(alpha_0*pi/180, d, rho_e, beta_coeff, r, lambda, stddevrad)"); // convert to radians

            return new GraphData(alpha_0, meas, oct.getMatrix("simul")[0]);
        }
    }
    */

    /** regression testing */
    /*
    public static void main(String[] args) {
        double[] alpha0 = new double[2000];
        LookupTable table = new TestLookup();
        final double lambda = 1.5405600e-10; // Cu K_alpha
        LayerStack[] layerStacks = new LayerStack[2];

        for(int i=0; i<alpha0.length; i++)
            alpha0[i] = 5.0*i/alpha0.length;

        GraphData data = new GraphData(alpha0, null, null);






        try {
            LayerStack layers = new LayerStack(lambda,table);
            layers.add(new Layer("Substrate", new FitValue(0,0,0),
                       new FitValue(2.26e3,2.33e3,2.4e3), new FitValue(0,3e-9,3e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("Si"),0,table,lambda));
            layers.add(new Layer("Native oxide", new FitValue(0e-9,7e-9,8e-9),
                       new FitValue(1e3,2.1e3,3e3), new FitValue(0,2e-9,2e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("O"),2.0/3,table,lambda));
            layers.add(new Layer("Thin film", new FitValue(40e-9,55e-9,70e-9),
                       new FitValue(1e3,3.4e3,4e3), new FitValue(0,1e-9,1e-9),
                       new ChemicalFormula("Al"),new ChemicalFormula("O"),3/5.0,table,lambda));
            layerStacks[0] = layers;

            layers = new LayerStack(lambda,table);
            layers.add(new Layer("Substrate", new FitValue(0,0,0),
                       new FitValue(2.26e3,2.33e3,2.4e3), new FitValue(0,3e-9,3e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("Si"),0,table,lambda));
            layers.add(new Layer("Native oxide", new FitValue(0e-9,7e-9,8e-9),
                       new FitValue(1e3,2.1e3,3e3), new FitValue(0,2e-9,2e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("O"),2.0/3,table,lambda));
            layers.add(new Layer("Thin film 1", new FitValue(40e-9,55e-9,70e-9),
                       new FitValue(1e3,3.4e3,4e3), new FitValue(0,1e-9,1e-9),
                       new ChemicalFormula("Al"),new ChemicalFormula("O"),3/5.0,table,lambda));
            layers.add(new Layer("Thin film 2", new FitValue(40e-9,41e-9,70e-9),
                       new FitValue(1e3,8.6e3,15e3), new FitValue(0,5e-9,8e-9),
                       new ChemicalFormula("Al"),new ChemicalFormula("Al"),3/5.0,table,lambda));
            layerStacks[1] = layers;
        }
        catch(ChemicalFormulaException ex) {
            throw new RuntimeException("Doesn't get thrown");
        }
        catch(ElementNotFound ex) {
            throw new RuntimeException("Doesn't get thrown");
        }


        Oct oct;
       
        try {
	    oct = XRRApp.startOctave();
            oct.sync();
            for(int j=0; j<layerStacks.length; j++) {
                for(int dummy=0; dummy<20; dummy++) {
                    LayerStack layers = layerStacks[j];
                    double stddevdeg = Math.random()*0.05;
                    layers.setStdDev(stddevdeg);
                    GraphData data2 = data.simulate(layers);
                    GraphData data3 = data.octSimulate(oct, layers);
                    assert(data2.alpha_0.length == data3.alpha_0.length);
                    assert(data2.simul.length == data3.simul.length);
                    assert(data2.simul.length == data3.alpha_0.length);
                    for(int i=0; i<data2.simul.length; i++) {
                        assert(data2.alpha_0[i] == data3.alpha_0[i]);
                        assert(Math.abs(data2.simul[i] - data3.simul[i]) < 1e-6*(data2.simul[i] + data3.simul[i])/2);
                    }
                }
            }

            for(int i=0; i<layerStacks.length; i++) {
                LayerStack layers = layerStacks[i];
                for(int j=0; j<layers.getSize(); j++) {
                    Layer l = layers.getElementAt(j);
                    Map<String,Double> m = l.getComposition();
                    String layerCmd = "layers = {{'Air',1};{";
                    String rhoCmd = "rho = [0,";
                    for(String element : m.keySet()) {
                        layerCmd += "'" + oct.escape(element) + "',";
                        layerCmd += m.get(element) + ",";
                    }
                    rhoCmd += l.getDensity().getExpected()*1e3; // g/m^3
                    layerCmd += "}}"; 
                    rhoCmd += "]";
                    oct.execute(layerCmd);
                    oct.execute(rhoCmd);
                    oct.execute("rho_e = calculate_electron_density(layers, rho)");
                    oct.execute("beta_coeff = calculate_beta_coeff(layers)");
                    double rho1 = oct.getMatrix("rho_e")[0][1];
                    double rho2 = l.getXRRCompound().getRhoEPerRho()*l.getDensity().getExpected();
                    double beta1 = oct.getMatrix("beta_coeff")[0][1];
                    double beta2 = l.getXRRCompound().getBetaPerDelta();
                    assert(Math.abs(rho1 - rho2) < 1e-6*(rho1 + rho2)/2);
                    assert(Math.abs(beta1 - beta2) < 1e-6*(beta1 + beta2)/2);
                }
            }
        }
        catch (OctException ex) {
            System.out.println("Octave error");
            return;
        }
    } */
}
