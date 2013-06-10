package fi.micronova.tkk.xray.measimport;
import java.io.*;
import java.util.*;

/** ASCII importing class.
 *
 * Imports measurements and simulations exported from this XRD software as
 * ASCII files */

public class AsciiImport {
    private AsciiImport() {}
    /** Imported data.
     *
     * This contains the imported alpha_0, measurement and simulation data.
     */
    public static class AsciiData {
        public final double[] alpha_0, meas, simul;
        public AsciiData(double[] alpha_0, double[] meas, double[] simul) {
            this.alpha_0 = alpha_0;
            this.meas = meas;
            this.simul = simul;
        }
    };
    /** One data point.
     *
     * This contains alpha_0, meas, simul values for one data point.
     */
    private static class AlphaMeasSimul {
        public final double alpha_0, meas, simul;
        public AlphaMeasSimul(double alpha_0, double meas, double simul) {
            this.alpha_0 = alpha_0;
            this.meas = meas;
            this.simul = simul;
        }
    };
    /** ASCII import.
     *
     * This function imports the data from an ASCII file.
     *
     * @param is the input stream to import the data from
     * @return all the alpha_0, meas and simul values in AsciiData class.
     */
    public static AsciiData AsciiImport(InputStream is) throws ImportException, IOException {
        ArrayList<AlphaMeasSimul> data = new ArrayList<AlphaMeasSimul>();
        double[] alpha_0, meas, simul;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while((line = r.readLine()) != null) {
                StringTokenizer t = new StringTokenizer(line);
                String s1 = t.nextToken();
                String s2 = t.nextToken();
                String s3 = t.nextToken();
                double d1 = Double.parseDouble(s1);
                double d2 = Double.parseDouble(s2);
                double d3 = Double.parseDouble(s3);
                if(d1 < 0 || d1 > 90 || d2 < 0 || d3 < 0 || t.hasMoreElements())
                    throw new ImportException();
                data.add(new AlphaMeasSimul(d1, d3, d2));
            }
        }
        catch(NumberFormatException ex) {
            throw new ImportException();
        }
        catch(NoSuchElementException ex) {
            throw new ImportException();
        }
        alpha_0 = new double[data.size()];
        meas = new double[data.size()];
        simul = new double[data.size()];

        for(int i=0; i<data.size(); i++) {
            AlphaMeasSimul d = data.get(i);
            alpha_0[i] = d.alpha_0;
            meas[i] = d.meas;
            simul[i] = d.simul;
        }
        return new AsciiData(alpha_0, meas, simul);
    }
};
