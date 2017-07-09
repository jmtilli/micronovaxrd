package fi.micronova.tkk.xray.measimport;
import java.io.*;
import fi.iki.jmtilli.javaxmlfrag.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import java.util.*;
import java.util.zip.*;


/** Measurement importing code.
 *
 * <p>
 *
 * Imports measurements exported from PANalytical's software
 */

public class PANImport {
    private PANImport() {}
    /** The imported data */
    public static class Data {
        public final double[][] arrays;
        public final boolean[] valid;
        public Data(double[][] arrays) {
            this.arrays = arrays;
            this.valid = new boolean[arrays.length];
            for (int i = 0; i < arrays.length; i++)
            {
                this.valid[i] = (arrays[i] != null);
            }
        }
    };
    /** Imports measurement file from an InputStream.
     *
     * @param s the stream to import the measurement from
     * @return the imported data points
     * @throws IOException if an I/O error occurs
     * @throws ImportException if the file format is invalid
     * */
    public static Data X00Import(InputStream s) throws ImportException, IOException {
        double[] alpha_0;
        double[] meas;
        Boolean is_2theta_omega = null;
        try {
            Double firstAngle = null, stepWidth = null;
            Integer nrOfData = null;
            InputStreamReader rr = new InputStreamReader(s);
            BufferedReader r = new BufferedReader(rr);

            String line = r.readLine();
            if(line == null || !line.equals("HR-XRDScan"))
                throw new ImportException();
            for(;;) {
                line = r.readLine();
                if(line == null)
                    throw new ImportException();
                if(line.length() > 10 && line.substring(0,10).equals("ScanAxis, ")) {
                    String axis;
                    if(is_2theta_omega != null)
                        throw new ImportException();
                    axis = line.substring(10);
                    if (axis.equals("2Theta/Omega"))
                    {
                      is_2theta_omega = Boolean.TRUE;
                    }
                    else if (axis.equals("Omega/2Theta"))
                    {
                      is_2theta_omega = Boolean.FALSE;
                    }
                    else
                    {
                      throw new ImportException();
                    }
                }
                if(line.length() > 12 && line.substring(0,12).equals("FirstAngle, ")) {
                    if(firstAngle != null)
                        throw new ImportException();
                    firstAngle = Double.valueOf(line.substring(12));
                }
                if(line.length() > 11 && line.substring(0,11).equals("StepWidth, ")) {
                    if(stepWidth != null)
                        throw new ImportException();
                    stepWidth = Double.valueOf(line.substring(11));
                }
                if(line.length() > 10 && line.substring(0,10).equals("NrOfData, ")) {
                    if(nrOfData != null)
                        throw new ImportException();
                    nrOfData = Integer.valueOf(line.substring(10));
                }
                if(line.equals("ScanData"))
                    break;
            }
            if(stepWidth == null || firstAngle == null || nrOfData == null)
                throw new ImportException();

            int size = nrOfData.intValue();
            double alpha = firstAngle.doubleValue();
            double width = stepWidth.doubleValue();
            if (is_2theta_omega == null)
              throw new ImportException();
            if (is_2theta_omega)
            {
              alpha /= 2;
              width /= 2;
            }

            alpha_0 = new double[size];
            meas = new double[size];

            for(int i=0; i<size; i++) {
                line = r.readLine();
                if(line == null)
                    throw new ImportException();
                alpha_0[i] = alpha + i*width;
                meas[i] = Double.parseDouble(line);
            }
            if(r.readLine() != null)
                throw new ImportException();
        }
        catch(NumberFormatException ex) {
            throw new ImportException();
        }
        return new Data(new double[][]{alpha_0, meas});
    }
    public static Data XRDMLImport(InputStream s) throws ImportException, IOException
    {
        DocumentFragment doc_frag;
        try
        {
            doc_frag = DocumentFragmentHandler.parseWhole(s);
        }
        catch(ParserConfigurationException ex)
        {
            return null;
        }
        catch(SAXException ex)
        {
            return null;
        }
        try {
            DocumentFragment measurement;
            DocumentFragment scan;
            DocumentFragment dataPoints;
            DocumentFragment commonCountingTime;
            DocumentFragment intensities;
            String axis, unit;
            String[] counts;
            double time = 1.0;
            double[] times = null;
            double start = 0, end = 90, step;
            boolean valid = false;
            double[] alpha_0, meas;
            doc_frag.assertTag("xrdMeasurements");
            measurement = doc_frag.getNotNull("xrdMeasurement");
            if (!measurement.getAttrStringNotNull("measurementType").equals("Scan"))
            {
                throw new ImportException();
            }
            if (!measurement.getAttrStringNotNull("status").equals("Completed"))
            {
                throw new ImportException();
            }
            scan = measurement.getNotNull("scan");
            axis = scan.getAttrStringNotNull("scanAxis");
            if (!axis.equals("Omega-2Theta") &&
                !axis.equals("2Theta-Omega"))
            {
                throw new ImportException();
            }
            dataPoints = scan.getNotNull("dataPoints");
            commonCountingTime = dataPoints.get("commonCountingTime");
            if (commonCountingTime == null)
            {
                String[] strtimes;
                DocumentFragment countingTimes;
                countingTimes = dataPoints.getNotNull("countingTimes");
                unit = countingTimes.getAttrStringNotNull("unit");
                if (!unit.equals("seconds"))
                {
                    throw new ImportException();
                }
                strtimes = dataPoints.getStringNotNull("countingTimes").trim().split("[ \t\r\n]+", 0);
                times = new double[strtimes.length];
                for (int i = 0; i < times.length; i++)
                {
                    times[i] = Double.parseDouble(strtimes[i]);
                }
            }
            else
            {
                unit = commonCountingTime.getAttrStringNotNull("unit");
                if (!unit.equals("seconds"))
                {
                    throw new ImportException();
                }
                time = dataPoints.getDoubleNotNull("commonCountingTime");
            }
            intensities = dataPoints.getNotNull("intensities");
            unit = intensities.getAttrStringNotNull("unit");
            if (!unit.equals("counts"))
            {
                throw new ImportException();
            }
            counts = dataPoints.getStringNotNull("intensities").trim().split("[ \t\r\n]+", 0);
            for (DocumentFragment positions: dataPoints.getMulti("positions"))
            {
                if (!positions.getAttrStringNotNull("axis").equals("2Theta"))
                {
                    continue;
                }
                if (!positions.getAttrStringNotNull("unit").equals("deg"))
                {
                    throw new ImportException();
                }
                start = positions.getDoubleNotNull("startPosition")/2.0;
                end = positions.getDoubleNotNull("endPosition")/2.0;
                valid = true;
            }
            if (!valid || counts.length <= 1)
            {
                throw new ImportException();
            }
            step = (end-start)/(counts.length-1);
            meas = new double[counts.length];
            alpha_0 = new double[counts.length];
            if (times != null && times.length != counts.length)
            {
                throw new ImportException();
            }
            for (int i = 0; i < meas.length; i++)
            {
                if (times == null)
                {
                    meas[i] = Double.parseDouble(counts[i])/time;
                }
                else
                {
                    meas[i] = Double.parseDouble(counts[i])/times[i];
                }
                alpha_0[i] = start + i*step;
            }
            return new Data(new double[][]{alpha_0, meas});
        }
        catch(NumberFormatException ex)
        {
            throw new ImportException();
        }
        catch(XMLException ex)
        {
            throw new ImportException();
        }
    }
    public static Data asciiImport(InputStream is) throws ImportException, IOException {
        ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
        double[][] arrays;
        int cols = -1;
        int validCols = 0;
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while((line = r.readLine()) != null) {
                line = line.trim().replaceAll("[#%].*$", "").trim();
                if (line.equals(""))
                {
                    continue;
                }
                StringTokenizer t = new StringTokenizer(line,  " \t\n\r\f;|:");
                int curcols = 0;
                ArrayList<Double> list = new ArrayList<Double>();
                while (t.hasMoreTokens())
                {
                    String s = t.nextToken().replace(",",".");
                    double d;
                    try
                    {
                        d = Double.parseDouble(s);
                    }
                    catch (NumberFormatException ex)
                    {
                        if (curcols == 0)
                        {
                            throw ex;
                        }
                        else
                        {
                            curcols++;
                            list.add(null);
                            continue;
                        }
                    }
                    if (curcols == 0 && (d < 0 || d > 90))
                    {
                        throw new ImportException();
                    }
                    curcols++;
                    list.add(d);
                }
                if ((cols >= 0 && cols != curcols) || curcols < 2)
                {
                    throw new ImportException();
                }
                if (cols < 0)
                {
                    cols = curcols;
                }
                data.add(list);
            }
        }
        catch(NumberFormatException ex) {
            throw new ImportException();
        }
        catch(NoSuchElementException ex) {
            throw new ImportException();
        }
        arrays = new double[cols][];
        for (int i = 0; i < cols; i++)
        {
            boolean ok = true;
            for (int j = 0; j < data.size(); j++)
            {
                if (data.get(j).get(i) == null)
                {
                    ok = false;
                    break;
                }
            }
            if (ok)
            {
                validCols++;
                arrays[i] = new double[data.size()];
                for (int j = 0; j < data.size(); j++)
                {
                    arrays[i][j] = data.get(j).get(i);
                }
            }
            else
            {
                arrays[i] = null;
            }
        }
        if (validCols < 2 || arrays[0] == null)
        {
            throw new ImportException();
        }
        return new Data(arrays);
    }
    private static void readWhiteSpace(BufferedInputStream bs) throws IOException
    {
        byte[] array = new byte[1024];
        int j = 0;
outer:
        while (true)
        {
            bs.mark(2048);
            bs.read(array, 0, 1024);
            for (int i = 0; i < 1024; i++)
            {
                if (array[i] != '\t' && array[i] != '\n' &&
                    array[i] != '\r' && array[i] != ' ')
                {
                    bs.reset();
                    j = i;
                    break outer;
                }
            }
        }
        bs.read(array, 0, j);
    }
    /** Imports measurement file from an InputStream.
     *
     * @param s the stream to import the measurement from
     * @return the imported data points
     * @throws IOException if an I/O error occurs
     * @throws ImportException if the file format is invalid
     * */
    public static Data PANImport(InputStream s) throws ImportException, IOException {
        BufferedInputStream bs = new BufferedInputStream(s);
        Data data;
        byte[] header = new byte[10];
        int ch;
        bs.mark(16);
        bs.read(header, 0, 10);
        bs.reset();
        if (header[0] == (byte) (GZIPInputStream.GZIP_MAGIC&0xFF) &&
            header[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8))
        {
            GZIPInputStream gz = new GZIPInputStream(bs);
            bs = new BufferedInputStream(gz);
            bs.mark(16);
            bs.read(header, 0, 10);
            bs.reset();
        }
        if (new String(header).equals("HR-XRDScan"))
        {
            return X00Import(bs);
        }
        readWhiteSpace(bs);
        bs.mark(16);
        ch = bs.read();
        bs.reset();
        if (ch == '<')
        {
            data = XRDMLImport(bs);
            if (data == null)
            {
                throw new ImportException();
            }
            return data;
        }
        return asciiImport(bs);
    }
};
