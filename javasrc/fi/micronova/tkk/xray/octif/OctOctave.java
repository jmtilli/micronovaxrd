package fi.micronova.tkk.xray.octif;
import java.io.*;


/** The octave interface.
 *
 * <p>
 *
 * This class communicates with an Octave instance via its input, output and
 * error streams. It allows to get and put matrices and execute arbitrary
 * commands.
 *
 * <p>
 *
 * The Octave interface is not thread safe. Access must be synchronized
 * properly.
 *
 * <p>
 *
 * Debugging information and error messages are saved to octcmds.txt and
 * octerrs.txt in the working directory. Old files are overwritten.
 *
 */

public class OctOctave implements Oct {
    private static final byte[] errbuf = new byte[512];

    private static final String OCTAVE_CODE = (
"function octavejavainterface_printrealmatrix(m,d)\n"+
"printf('BEGINMATRIX %d\\n',d);\n"+
"s = size(m);\n"+
"printf('%d\\n',s(1));\n"+
"printf('%d\\n',s(2));\n"+
"printf('%.16g\\n',m);\n"+
"printf('ENDMATRIX\\n');\n"+
"end\n");


    private final Process octp;
    private final BufferedReader input;
    private final PrintWriter output, fileoutput;
    private final InputStream error;
    private final OutputStream errfileout;
    private int c;

    /** Constructor.
     *
     * <p>
     *
     * Starts an Octave instance.
     *
     * @param path Command to start the Octave executable. Must contain the "-q" parameter.
     *
     * @throws OctException if an I/O or Octave error occurs.
     *
     */
    public OctOctave(String path) throws OctException {
        try {
            octp = Runtime.getRuntime().exec(path);
            input = new BufferedReader(new InputStreamReader(octp.getInputStream(),"US-ASCII"));
            error = octp.getErrorStream();
            output = new PrintWriter(
                       new BufferedWriter(
                         new OutputStreamWriter(octp.getOutputStream(),"US-ASCII")));
            fileoutput = new PrintWriter(
                           new BufferedWriter(
                             new OutputStreamWriter(new FileOutputStream("octcmds.txt"),"US-ASCII")));
            errfileout = new FileOutputStream("octerrs.txt");
            output.println(OCTAVE_CODE);
            fileoutput.println(OCTAVE_CODE);
            if(output.checkError() || fileoutput.checkError())
                throw new OctException();
            c = 0;

            sync();
        }
        catch(IOException ex) {
            throw new OctException();
        }
    };
    private void throwException() throws OctException {
        writeErrors();
        throw new OctException();
    }
    /** Waits for octave to execute the last command.
     * Ensures that an error hasn't occurred during the execution of the last command.
     *
     * @throws OctException if the execution of the last command didn't succeed
     */
    public void sync() throws OctException {
        double[][] matrix;
        execute("javaoctaveinterface_a = [1,2;3.5,4]", false);
        execute("javaoctaveinterface_b = [5,6;7,8]", false);
        execute("javaoctaveinterface_r = javaoctaveinterface_a*javaoctaveinterface_b", false);
        matrix = getMatrix("javaoctaveinterface_r", false);
        if(!(matrix.length == 2 && matrix[0].length == 2 &&
                    Math.abs(matrix[0][0] - 19) < 1e-6 &&
                    Math.abs(matrix[0][1] - 22) < 1e-6 &&
                    Math.abs(matrix[1][0] - 45.5) < 1e-6 &&
                    Math.abs(matrix[1][1] - 53) < 1e-6))
            throwException();
    }
    private void writeErrors() throws OctException {
        try {
            int toRead;
            /* clear error stream; it might be blocking Octave */
            while((toRead = error.available()) > 0) {
                /* XXX: should throw an Exception */
                int bytesRead = error.read(errbuf, 0, Math.min(errbuf.length, toRead));
                errfileout.write(errbuf, 0, bytesRead);
                errfileout.flush();
                //System.out.print("OCTERR: ");
                //System.out.println(new String(errbuf,0,bytesRead));
            }
        }
        catch(IOException ex) {
            throw new OctException();
        }
    }
    private String readLine() throws OctException {
        try {
            writeErrors();
            return input.readLine();
        }
        catch(IOException ex) {
            throw new OctException();
        }
    }
    /** Escapes a string.
     *
     * <p>
     *
     * The string may be passed to Octave as "\"" + Oct.escape(s) + "\"" or "'" + Oct.escape(s) + "'".
     * The last syntax is the only one supported by both Matlab and Octave.
     *
     * @param s The string to escape
     * @return The escaped string
     */
    public String escape(String s) {
        /* You don't have to understand this. It just works. */
        return s.replaceAll("\\\\","\\\\\\\\").replaceAll("\\n","\\\\n").replaceAll("\"","\\\\\"").replaceAll("'","\\\\'");
    }
    /** Execute statements from an input stream
     *
     * @param s the input stream containing the statements
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void source(InputStream s) throws OctException {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(s,"US-ASCII"));
            String line;
            while((line = r.readLine()) != null) {
                output.println(line);
                fileoutput.println(line);
            }
            if(output.checkError() || fileoutput.checkError())
                throw new OctException();
            sync();
        }
        catch(IOException ex) {
            throw new OctException();
        }
    }
    /** Puts a row vector.
     *
     * @param m the row vector
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putRowVector(String s, double[] m) throws OctException {
        double[][] m2 = new double[1][];
        m2[0] = m;
        putMatrix(s,m2);
    }
    /** Puts a scalar.
     *
     * @param m the scalar
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putScalar(String s, double m) throws OctException {
        double[] m2 = new double[1];
        m2[0] = m;
        putRowVector(s,m2);
    }
    /** Puts a matrix.
     *
     * @param m the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putMatrix(String s, double[][] m) throws OctException {
        int h = m.length,w = m[0].length;
        int y,x;

        execute(s + " = fscanf(stdin,'%f',["+h+","+w+"])");
        for(x=0;x<w;x++) {
            for(y=0;y<h;y++) {
                output.println(m[y][x]);
                fileoutput.println(m[y][x]);
            }
        }
        if(output.checkError() || fileoutput.checkError())
            throw new OctException();
        sync();
    }
    /** Gets a matrix.
     *
     * @return the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public double[][] getMatrix(String s) throws OctException {
        return getMatrix(s, true);
    }
    /* the real code to get a matrix. log specifies whether the commands to get the matrix are saved to octcmds.txt */
    private double[][] getMatrix(String s, boolean log) throws OctException {
        try {
            String line;
            c++;
            double[][] matrix;

            int h,w;
            int y,x;

            execute("octavejavainterface_printrealmatrix("+s+","+c+")", log);
            line = readLine();
            if(line == null) throwException();
            if(!line.equals("BEGINMATRIX "+c)) throwException();

            line = readLine();
            if(line == null) throwException();
            h = Integer.parseInt(line);

            line = readLine();
            if(line == null) throwException();
            w = Integer.parseInt(line);

            matrix = new double[h][];
            for(y=0;y<h;y++)
                matrix[y] = new double[w];

            for(x=0;x<w;x++) {
                for(y=0;y<h;y++) {
                    line = readLine();
                    if(line == null) throwException();
                    matrix[y][x] = Double.parseDouble(line);
                }
            }

            line = readLine();
            if(line == null) throwException();
            if(!line.equals("ENDMATRIX")) throwException();

            return matrix;
        }
        catch(NumberFormatException ex) {
            throw new OctException("Communication with Octave out of sync");
        }
    }
    /** Executes an Octave command.
     *
     * @param s the command string
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void execute(String s) throws OctException {
        execute(s, true);
    };
    /* the real code to execute a command. log specifies whether the command is saved to octcmds.txt */
    private void execute(String s, boolean log) throws OctException {
        output.println(s + ";");
        if(output.checkError())
            throw new OctException();
        if(log) {
            fileoutput.println(s + ";");
            if(fileoutput.checkError())
                throw new OctException();
            //System.out.print("TO OCTAVE: ");
            //System.out.println(s + ";");
        }
    };
    /** Closes the Octave instance and waits.
     *
     * <p>
     *
     * After calling this method, this object will be in an unspecified state.
     *
     * @throws InterruptedException if the waiting is interrupted
     */
    public void exit() throws InterruptedException {
        try {
            execute("exit");
            while(readLine() != null);
            octp.waitFor();
        }
        catch (OctException ex) {}
    };

    public static void main(String[] args) {
      try {
        double[][] m;
        Oct oct = new OctOctave("octave -q");
        oct.sync();
        oct.execute("a=2");
        oct.sync();
        m = oct.getMatrix("c");
        System.out.println(m.length);
        System.out.println(m[0].length);
        System.out.println(m[0][0]);
        oct.exit();
      }
      catch(OctException ex) {
        System.out.println("exception caught");
      }
      catch(InterruptedException ex) {
        System.out.println("interrupt");
      }
    }
}
