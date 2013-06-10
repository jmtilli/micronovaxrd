package fi.micronova.tkk.xray.octif;
import java.io.*;


/** The octave/matlab interface.
 *
 * <p>
 *
 * This interface is implemented by classes that interface Octave, Matlab or a
 * compatible language. It allows to get and put matrices and execute arbitrary
 * commands.
 *
 * <p>
 *
 * The interface is not guaranteed to be thread safe. Access must be
 * synchronized properly.
 *
 * <p>
 *
 * 
 *
 * <p>
 *
 * An exception is always fatal. After an exception occurs, the implementation
 * might be in an unspecified state.
 *
 * <p>
 *
 * Debugging information and error messages are saved to octcmds.txt and
 * octerrs.txt in the working directory. Old files are overwritten.
 *
 */

public interface Oct {
    /** Ensures that no error has happened.
     *
     * <p>
     *
     * This method waits for Octave/Matlab to execute all the pending commands
     * and throws an exception if one of them resulted in an error.
     */
    public void sync() throws OctException;
    /** Escapes a string.
     *
     * <p>
     *
     * The string may be passed to Octave as "\"" + Oct.escape(s) + "\"" or "'" + Oct.escape(s) + "'".
     * The last syntax is the only one supported by both Matlab and Octave. Generally, you should use
     * the last syntax. Note that Matlab does not support including newlines and null characters in
     * quoted strings.
     *
     * @param s The string to escape
     * @return The escaped string
     * @throws OctException if s contains illegal characters
     */
    public String escape(String s) throws OctException;
    /* XXX: what if s contains unicode characters? TODO: investigate */

    /** Execute statements from an input stream.
     *
     * <p>
     *
     * This executes all the statements (delimited by newlines) from the input
     * stream. It is functionally equivalent to multiple execute calls, but it
     * might be more efficient. Please note that unlike Octave, Matlab does not
     * support embedding function definitions with statements. In Matlab functions
     * must be defined in their own .m-files.
     *
     * @param s the input stream containing the statements
     *
     * @throws OctException if an I/O or Octave error has occurred
     */

    public void source(InputStream s) throws OctException;
    /** Puts a row vector.
     *
     * @param m the row vector
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putRowVector(String s, double[] m) throws OctException;
    /** Puts a scalar.
     *
     * @param m the scalar
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putScalar(String s, double m) throws OctException;
    /** Puts a matrix.
     *
     * @param m the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public void putMatrix(String s, double[][] m) throws OctException;
    /** Gets a matrix.
     *
     * @return the matrix as a two-dimensional array, the first dimension of which is the row index
     *
     * @throws OctException if an I/O or Octave error has occurred
     */
    public double[][] getMatrix(String s) throws OctException;

    /** Evaluates an expression.
     *
     * <p>
     *
     * The method might not return errors immediately. If you want to know
     * whether there was an error, you must call sync or use another method
     * that waits for input from Octave/Matlab, eg. getMatrix. Calling sync
     * is preferable since getMatrix might report its own error.
     *
     * @param s the command string
     *
     * @throws OctException if an I/O or Octave/Matlab error has occurred
     */
    public void execute(String s) throws OctException;
    /** Closes the Octave instance and waits.
     *
     * <p>
     *
     * After calling this method, this object will be in an unspecified state.
     *
     * <p>
     *
     * Some implementations (for example, the Matlab implementation) may require
     * this to be called even if an exception occurred
     *
     * @throws InterruptedException if the waiting is interrupted
     */
    public void exit() throws InterruptedException;
}
