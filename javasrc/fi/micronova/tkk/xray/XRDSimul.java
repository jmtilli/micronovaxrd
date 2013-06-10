package fi.micronova.tkk.xray;
import java.util.*;
import java.io.*;
import fi.micronova.tkk.xray.octif.*;

/** XRD simulation utility class.
 *
 * <p>
 *
 * This class contains the XRD simulation code implementation in Java.
 *
 */

public class XRDSimul {

    private XRDSimul() {}

    /*
     * Calculates output of a fir filter during the time interval [low,high[.
     * Default of Octave's filter/fftfilt: low=0, high=s.length
     * Default of Octave's conv: low=0, high=s.length+b.length-1
     * Symmetric convolution (b.length odd): low=(b.length-1)/2, high = s.length + (b.length-1)/2
     *
     * In Octave code:
     *   filter(b,1,[s,zeros(1,high-low-length(s))])(low:end)
     * where high and low use 1-based indexing instead of 0-based as in Java
     */
    private static double[] fir(double[] b, double[] s, int low, int high) {
        double[] result = new double[high-low];
        for(int i=low; i<high; i++) {
            for(int j=Math.max(0,i-(s.length-1)); j<Math.min(b.length,i+1); j++) {
                result[i-low] += s[i-j]*b[j];
            }
        }
        return result;
    }

    /* Apply an odd filter. */
    /* In Octave:
     *   fir(filter, data, filterside, length(data)+filterside)
     *   where filterside = (filter.length+1)/2 (note the difference!)
     */
    private static double[] applyOddFilter(double[] filter, double[] data) {
        int filterside = (filter.length-1)/2;
        assert((filter.length-1)%2 == 0);
        return fir(filter, data, filterside, data.length+filterside);
    }

    /*
     * Create an odd gaussian filter
     *
     * in Octave:
     *
       function filter = gaussian_filter(dalpha0rad, stddevrad, stddevs)
         filterside = round(stddevs*stddevrad/dalpha0rad);
         filter = exp(-((dalpha0rad*((-filterside):filterside)/stddevrad).^2)/2);
         filter /= sum(filter);
       end
     */
    private static double[] gaussianFilter(double dalpha0rad, double stddevrad, double stddevs) {
        int filterside;
        double[] filter = null;

        filterside = (int)(stddevs*stddevrad/dalpha0rad + 0.5);
        if(filterside <= 0)
            return null;

        filter = new double[2*filterside+1];

        double sum = 0;
        for(int i=0; i<filter.length; i++) {
            double x = dalpha0rad*(i-filterside)/stddevrad;
            filter[i] = Math.exp(-x*x/2);
            sum += filter[i];
        }
        for(int i=0; i<filter.length; i++) {
            filter[i] /= sum;
        }

        return filter;
    }



    /** Unit test */
    public static void main(String[] args) {
        String path;
        Random rand = new Random();

        try {
            FileInputStream rawopf = new FileInputStream("octave_path.txt");
            InputStreamReader rawopr = new InputStreamReader(rawopf);
            BufferedReader octpathf = new BufferedReader(rawopr);
            path = octpathf.readLine();
            if(path == null)
                throw new NullPointerException();
        }
        catch (IOException ex) {
            System.out.println("Can't read octave_path.txt");
            return;
        }
        catch (NullPointerException ex) {
            System.out.println("Can't read octave_path.txt");
            return;
        }

        Oct oct;
       
        try {
	    oct = XRDApp.startOctave();
            for(int i=0; i<50; i++) {
                int filterlen = 2*rand.nextInt(50)+1;
                int datalen = rand.nextInt(500);
                double[] filter = new double[filterlen];
                double[] data = new double[datalen];
                for(int j=0; j<filterlen; j++) {
                    filter[j] = rand.nextDouble();
                }
                for(int j=0; j<datalen; j++) {
                    data[j] = rand.nextDouble();
                }
                double[] result1 = applyOddFilter(filter, data);
                double[] result2;
                double[][] result_matrix;
                oct.putRowVector("filter",filter);
                oct.putRowVector("data",data);
                oct.execute("result_matrix = apply_odd_filter(filter, data)");
                result_matrix = oct.getMatrix("result_matrix");
                assert(result_matrix.length == 1);
                result2 = result_matrix[0];
                assert(result2.length == result1.length);
                for(int j=0; j<result1.length; j++) {
                    assert(Math.abs(result1[j] - result2[j]) < 1e-7*Math.abs(result1[j] + result2[j])/2+1e-99);
                }
            }
            for(int i=0; i<300; i++) {
                double stddevs = 5*rand.nextDouble();
                double dalpha0rad = 1;
                double stddevrad = 10*rand.nextDouble();

                double[] result1 = gaussianFilter(dalpha0rad, stddevrad, stddevs);
                double[] result2;
                oct.putScalar("stddevs",stddevs);
                oct.putScalar("dalpha0rad",dalpha0rad);
                oct.putScalar("stddevrad",stddevrad);
                oct.execute("result_matrix = gaussian_filter(dalpha0rad, stddevrad, stddevs)");
                double[][] result_matrix = oct.getMatrix("result_matrix");
                assert(result_matrix.length == 1);
                result2 = result_matrix[0];
                if(result1 == null) {
                    assert(result2.length == 1 && Math.abs(result2[0] - 1) < 1e-7);
                } else {
                    assert(result2.length == result1.length);
                    for(int j=0; j<result1.length; j++) {
                        assert(Math.abs(result1[j] - result2[j]) < 1e-7*Math.abs(result1[j] + result2[j])/2+1e-99);
                    }
                }

            }
        }
        catch (OctException ex) {
            System.out.println("Octave error");
            return;
        }
    }



}
