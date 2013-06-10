package fi.micronova.tkk.xray.util;
public class DataTools {
    /** Tests whether the values in x are uniformly spaced.
     * 
     * Uniform spacing is necessary in DFT and convolution.
     *
     * @param x the array to test
     * @return true if x is uniformly spaced
     *
     */
    public static boolean isUniformlySpaced(double[] x) {
        final double dx_accuracy = 1e-4;
        double dx = (x[x.length-1] - x[0])/(x.length-1);
        for(int i=1; i<x.length; i++) {
            double diff = x[i] - x[i-1];
            if(diff > (1+dx_accuracy)*dx ||
               diff < (1-dx_accuracy)*dx) {
                return false;
            }
        }
        return true;
    }



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
    public static double[] applyOddFilter(double[] filter, double[] data) {
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
    public static double[] gaussianFilter(double dalpha0rad, double stddevrad, double stddevs) {
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
}
