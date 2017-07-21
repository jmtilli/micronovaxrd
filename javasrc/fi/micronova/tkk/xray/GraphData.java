package fi.micronova.tkk.xray;
import java.util.*;
import java.io.*;
import fi.micronova.tkk.xray.xrdmodel.*;





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

    private static class PoissonApproxGenerator {
        private final Random rand = new Random();
        /**
         * Calculate natural logarithm of the gamma function.
         *
         * @param xx Argument of the gamma function
         * @return gamma function value
         */
        private static double log_gamma(double xx)
        {
            double xx2 = xx*xx;
            double xx3 = xx2*xx;
            double xx5 = xx3*xx2;
            double xx7 = xx5*xx2;
            double xx9 = xx7*xx2;
            double xx11 = xx9*xx2;
            return xx*Math.log(xx) - xx - 0.5*Math.log(xx/(2*Math.PI)) + 
                   1/(12*xx) - 1/(360*xx3) + 1/(1260*xx5) - 1/(1680*xx7) +
                   1/(1188*xx9) - 691/(360360*xx11);
        }
        private int nextValueSmall(double mean)
        {
            int result = 0;
            double L = Math.exp(-mean);
            double p = 1;
            do
            {
                result++;
                p *= rand.nextDouble();
            }
            while (p > L);
            result--;
            return result;
        }
        private int nextValueLarge(double mean)
        {
            double r;
            double x, m;
            double sqrt_mean = Math.sqrt(mean);
            double log_mean = Math.log(mean);
            /*
             * Ok, this requires a bit of an explanation. First, we are
             * generating random numbers from the Cauchy distribution:
             * f(x) = 1/(pi*gamma)*(gamma^2/((x-x0)^2+gamma^2))
             * y = F(x) = 1/pi*arctan((x-x0)/gamma) + 1/2
             * x = x0 + gamma*tan(pi*(y-1/2))
             * We select x0 = mean and gamma = sqrt(mean).
             * Now, the distribution has negative values as well, so they need
             * to be filtered out.
             * We evaluate the expression mean^m*exp(-mean)/m! at
             * m = floor(x), so the expression becomes
             * g(x) = exp(m*log(mean) - mean - lgamma(m + 1))
             *      = exp(floor(x)*log(mean) - mean - lgamma(floor(x) + 1))
             * We then calculate the ratio g(x) / f(x) / 2.4. Why 2.4, you ask?
             * Well, that's to make the ratio always smaller than 1.0 given any
             * mean value.
             * Then we reject if a uniform random number is larger than
             * g(x) / f(x) / 2.4, otherwise we accept.
             */
            if (mean < 10 || Double.isInfinite(mean) || Double.isNaN(mean))
            {
                // We haven't verified with mean < 10 that the ratio is below 1
                throw new IllegalArgumentException();
            }
            do
            {
                do
                {
                    x = mean + sqrt_mean*Math.tan(Math.PI*(rand.nextDouble()-1/2.0));
                }
                while (x < 0 || Double.isInfinite(x) || Double.isNaN(x));
                m = Math.floor(x);
                r = Math.exp(m*log_mean - mean - log_gamma(m + 1)) * ((x-mean)*(x-mean) + mean) * Math.PI/2.4 / sqrt_mean;
            }
            while (rand.nextDouble() > r || m > (double)Integer.MAX_VALUE);
            return (int)m;
        }
        /**
         * Create next random Poisson-distributed value.
         *
         * @param mean Mean of the Poisson distribution
         * @return Random Poisson-distributed value
         */
        public int nextValue(double mean) {
            if (mean < 60.0)
            {
                return nextValueSmall(mean);
            }
            else
            {
                return nextValueLarge(mean);
            }
        }
    }
    /** Adds noise to the measured data.
     *
     * @param photon linear intensity of a photon
     */
    public GraphData addNoise(double photon) {
        GraphData lin = convertToLinear();
        PoissonApproxGenerator gen = new PoissonApproxGenerator();
        for(int i=0; i<lin.meas.length; i++) {
            lin.meas[i] /= photon;
            lin.meas[i] = gen.nextValue(lin.meas[i]);
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
              {
                  if (meas[i] == 0)
                  {
                      newmeas[i] = -200;
                  }
                  else
                  {
                      newmeas[i] = 10*Math.log(meas[i])/Math.log(10);
                  }
              }
            }
            if(simul != null) {
              newsimul = new double[simul.length];
              for(int i=0; i<simul.length; i++)
              {
                  if (simul[i] == 0)
                  {
                      newsimul[i] = -200;
                  }
                  else
                  {
                      newsimul[i] = 10*Math.log(simul[i])/Math.log(10);
                  }
              }
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

    public GraphData crop(double firstAngle, double lastAngle)
    {
        double[] alpha_0, meas = null, simul = null;
        int count = 0;
        for (double alpha_0_i: this.alpha_0)
        {
            if (alpha_0_i >= firstAngle && alpha_0_i <= lastAngle)
            {
                count++;
            }
        }
        alpha_0 = new double[count];
        if (this.meas != null)
        {
            meas = new double[count];
        }
        if (this.simul != null)
        {
            simul = new double[count];
        }
        count = 0;
        for (int i = 0; i < this.alpha_0.length; i++)
        {
            if (this.alpha_0[i] >= firstAngle && this.alpha_0[i] <= lastAngle)
            {
                alpha_0[count] = this.alpha_0[i];
                if (this.meas != null)
                {
                    meas[count] = this.meas[i];
                }
                if (this.simul != null)
                {
                    simul[count] = this.simul[i];
                }
                count++;
            }
        }
        return new GraphData(alpha_0, meas, simul);
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

        simul = tempStack.xrdCurveFaster(alpha0rad);

        return new GraphData(alpha_0, meas, simul);
    }

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
    } */
}
