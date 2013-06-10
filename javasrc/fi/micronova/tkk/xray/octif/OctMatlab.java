package fi.micronova.tkk.xray.octif;
import java.io.*;
import java.util.logging.*;

import jmatlink.JMatLink;
import jmatlink.JMatLinkException;


/** The octave interface.
 *
 * <p>
 *
 * This class communicates with a Matlab instance via a platform-dependent
 * manner. It uses JMatLink, a Java library to Matlab native interface.
 *
 * <p>
 * 
 * Unlike the simple pipe-based Octave interface, the Matlab native interface
 * is able to recover from errors, but in order to make the interface
 * compatible with the Octave interface, Matlab is automatically shut down in
 * case of an error.
 *
 * <p>
 *
 * The Matlab interface might not be thread safe. Access must be synchronized
 * properly.
 *
 * <p>
 *
 * Debugging information is saved to matcmds.txt. Error messages should appear
 * in stdout (at least that is the case with Unix systems). If there is already
 * a file named matcmds.txt, it is overwritten.
 *
 */

public class OctMatlab implements Oct {
    private int statementNumber; /* used to check whether a command was succesful */

    private PrintWriter fileoutput;

    private JMatLink link;


    /* We must call one of these in case of an OctException. It shuts down
     * Matlab. The Java interface to the native interface is thread-based, so
     * Matlab doesn't close automatically.
     *
     * TODO: consider implementing a threadless Matlab interface to the native
     * interface from scratch.
     *
     * */
    private void throwOctException() throws OctException {
        throwOctException("Matlab error");
    }
    private void throwOctException(String s) throws OctException {
        exit();
        throw new OctException(s);
    }

    /** Constructor.
     *
     * <p>
     *
     * Starts a Matlab instance. On Unix systems, Matlab is started with the command 'matlab'.
     * If Matlab needs specific environment variables or is started by another command,
     * you must write a shell script named 'matlab' to start Matlab.
     *
     * @throws OctException if an I/O or Matlab error occurs.
     *
     */
    public OctMatlab(boolean debug) throws OctException {
        try {
            if(debug) {
                fileoutput = new PrintWriter(
                               new BufferedWriter(
                                 new OutputStreamWriter(new FileOutputStream("matcmds.txt"),"US-ASCII")));
                if(fileoutput.checkError())
                    throwOctException("Can't keep a log");
            } else {
                fileoutput = null;
            }

            link = new JMatLink();
            link.engOpen();
            statementNumber = 1;
            String cwd = new File( "." ).getCanonicalPath();
            link.engEvalString("cd('"+escape(cwd)+"')");
            //System.out.println(cwd);

            // XXX: does this do anything useful?
            link.engEvalString("warning off matlab:end_without_block;");

            //putScalar("javamatlabinterface_statementnumber",statementNumber);
            link.engEvalString("javamatlabinterface_statementnumber="+statementNumber+";");

            sync();
        }
        catch(IOException ex) {
            throwOctException("I/O error during startup: "+ex.getMessage());
        }
        catch(JMatLinkException ex) {
            throwOctException("Matlab error during startup: "+ex.getMessage());
        }
    };
    /** Waits for octave to execute the last command.
     * Ensures that an error hasn't occurred during the execution of the last command.
     *
     * @throws OctException if the execution of the last command didn't succeed
     */
    public void sync() throws OctException {
        double[][] matrix;
        execute("javamatlabinterface_a = [1,2;3.5,4]", false);
        execute("javamatlabinterface_b = [5,6;7,8]", false);
        execute("javamatlabinterface_r = javamatlabinterface_a*javamatlabinterface_b", false);
        matrix = getMatrix("javamatlabinterface_r");
        if(!(matrix.length == 2 && matrix[0].length == 2 &&
                    Math.abs(matrix[0][0] - 19) < 1e-6 &&
                    Math.abs(matrix[0][1] - 22) < 1e-6 &&
                    Math.abs(matrix[1][0] - 45.5) < 1e-6 &&
                    Math.abs(matrix[1][1] - 53) < 1e-6))
            throwOctException("Lost sync");
    }

    /** Escapes a string.
     *
     * <p>
     *
     * The string may be passed to Matlab as "'" + Oct.escape(s) + "'".
     * The syntax works for both Matlab and Octave.
     *
     * <p>
     *
     * Please note that Matlab is a particularly good example of what
     * happens if an engineer designs a computer programming language.
     * It has insufficient string escaping syntax, and therefore
     * multiple important characters cannot be included inside
     * a quoted string. These characters include newline and various control characters.
     * Fortunately, the only supported quoting characters of Matlab, single quotes, work. I think
     * the only characters that don't work are newline and '\0'.
     * 
     *
     * @param s The string to escape
     * @return The escaped string
     */
    public String escape(String s) throws OctException {
        if(s.indexOf('\000') >= 0 || s.indexOf('\n') >= 0)
            throw new OctException("the unescaped string contains illegal characters");
        return s.replaceAll("'","''");
    }
    /* XXX TODO: what if s contains Unicode characters? */

    
    /** Execute statements from an input stream
     *
     * @param s the input stream containing the statements
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    public void source(InputStream s) throws OctException {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(s,"US-ASCII"));
            String line;
            while((line = r.readLine()) != null) {
                execute(line, true);
            }
            sync();
        }
        catch(IOException ex) {
            throwOctException("Error sourcing a stream");
        }
    }

    private static final Logger log = Logger.getLogger(OctMatlab.class.getName());
    /** Puts a row vector.
     *
     * @param m the row vector
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    public void putRowVector(String s, double[] m) throws OctException {
        if(fileoutput == null) {
            try {
                link.engPutArray(s,m);
            }
            catch(JMatLinkException ex) {
                throwOctException("Can't put a vector "+s);
            }
        } else {
            double[][] m2 = new double[1][];
            m2[0] = m;
            putMatrix(s,m2);
        }
    }
    /** Puts a scalar.
     *
     * @param m the scalar
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    public void putScalar(String s, double m) throws OctException {
        if(fileoutput == null) {
            try {
                link.engPutArray(s,m);
            }
            catch(JMatLinkException ex) {
                throwOctException("Can't put a scalar "+s);
            }
        } else {
            double[] m2 = new double[1];
            m2[0] = m;
            putRowVector(s,m2);
        }
    }




    /** Puts a matrix.
     *
     * @param m the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putMatrix(String s, double[][] m) throws OctException {
        if(fileoutput != null) {
            StringWriter sw = new StringWriter();
            int h = m.length,w = m[0].length;
            int y,x;

            sw.write(s);
            sw.write(" = [");
            for(y=0;y<h;y++) {
                for(x=0;x<w;x++) {
                  sw.write(m[y][x]+"...\n");
                }
                sw.write("\n");
            }
            sw.write("]");
            execute(sw.toString());
        } else {
            try {
                link.engPutArray(s,m);
            }
            catch(JMatLinkException ex) {
                throwOctException("Can't put a matrix "+s);
            }
        }

        /*
        for(x=0;x<w;x++) {
            for(y=0;y<h;y++) {
                sw.write(m[y][x]+" ");
            }
        }
        execute(s + " = sscanf('"+sw.toString()+"','%f',["+h+","+w+"])");
        */

    }




        /* this implementation is more efficient but doesn't work with log */
    /* Puts a matrix.
     *
     * @param m the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    /*
    public void putMatrix(String s, double[][] m) throws OctException {

        try {
            link.engPutArray(s,m);
        }
        catch(JMatLinkException ex) {
            throwOctException("Can't put a matrix "+s);
        }
    }
    */

    /** Gets a matrix.
     *
     * @return the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    public double[][] getMatrix(String s) throws OctException {
        try {
            double[][] result;
            result = link.engGetArray(s);
            if(result == null)
                throwOctException("Matlab error: can't get variable "+s);
            return result;
        }
        catch(JMatLinkException ex) {
            throwOctException("Matlab error: can't get variable "+s); /* throws always */
        }
        return null; /* NOTREACHED */
    }
    /** Executes a Matlab command.
     *
     * @param s the command string
     *
     * @throws OctException if an I/O or Matlab error has occurred
     */
    public void execute(String s) throws OctException {
        execute(s, true);
    };
    /* the real code to execute a command. log specifies whether the command is saved to matcmds.txt */
    private void execute(String s, boolean log) throws OctException {
        /* We ensure that no error happened by appending a statement number command
         * after the actual command. If the actual command succeeds, the statement
         * number command will be executed. After that, we check the statement number.
         */
        double[][] matlabStatement;
        statementNumber++;
        statementNumber %= 65536;
        try {
            link.engEvalString(s + ";" + "javamatlabinterface_statementnumber="+statementNumber+";");
        }
        catch(JMatLinkException ex) {
            throwOctException("Matlab error: can't execute a statement");
        }
        matlabStatement = getMatrix("javamatlabinterface_statementnumber");
        /* avoid the unlikely case of exactly 65535 successive failing commands */
        // infinite recursion
        //putScalar("javamatlabinterface_statementnumber",statementNumber);
        try {
            link.engEvalString("javamatlabinterface_statementnumber="+statementNumber+";");
        }
        catch(JMatLinkException ex) {
            throwOctException();
        }
        if(log && fileoutput != null) {
            fileoutput.println(s + ";");
            if(fileoutput.checkError())
                throwOctException("Matlab error: can't keep a log");
            //System.out.print("TO OCTAVE: ");
            //System.out.println(s + ";");
        }
        if(matlabStatement.length != 1 || matlabStatement[0].length != 1 ||
            (int)(matlabStatement[0][0]+1e-2) != statementNumber)
          throwOctException("command failed");
    };
    /** Closes the Matlab instance.
     *
     * <p>
     *
     * After calling this method, this object will be in an unspecified state.
     *
     * @throws InterruptedException if the waiting is interrupted
     */
    public void exit() {
        try {
          if(link != null) {
            link.engClose();
            link = null;
          }
        }
        catch (JMatLinkException ex) {}
        /*
        try {
            execute("exit");
            while(readLine() != null);
            octp.waitFor();
        }
        catch (OctException ex) {}
        */
    };

    /* This doesn't seem to work as expected. If the main method exits, this
     * isn't called for some reason. Maybe Java decides to delay GC of this
     * object, since the thread of JMatLink is running.
     */
    protected void finalize() throws Throwable {
        try {
            exit();
        }
        finally {
            super.finalize();
        }
    }


    public static void main(String[] args) {
      try {
        double[][] m;
        Oct oct = new OctMatlab(false);
        m = new double[2][];
        m[0] = new double[2];
        m[1] = new double[2];
        m[0][0] = 1;
        m[0][1] = 2;
        m[1][0] = 3;
        m[1][1] = 4;
        oct.putMatrix("foo",m);
        oct.execute("a=2");
        oct.execute("a=2;");
        oct.execute("a=[1+2j,3+4j]");
        oct.execute("fprintf(1,'foo\\n')");
        m = oct.getMatrix("a");
        System.out.println(m.length);
        System.out.println(m[0].length);
        System.out.println(m[0][1]);
        oct.exit();
      }
      catch(OctException ex) {
        System.out.print("exception caught: ");
        System.out.println(ex.getMessage());
      }
      catch(InterruptedException ex) {
        System.out.println("interrupt");
      }
    }
}
