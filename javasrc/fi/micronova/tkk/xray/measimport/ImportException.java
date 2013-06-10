package fi.micronova.tkk.xray.measimport;
/** Import exception.
 *
 * <p>
 *
 * This is thrown when the file format of the measurement file to import is
 * invalid.
 */

public class ImportException extends Exception {
    /** Default constructor.
     *
     * <p>
     *
     * The default error message is "Invalid file format".
     *
     */
    public ImportException() {
        super("Invalid file format");
    }
    /** Constructor.
     *
     * @param s the human-readable error message
     */
    public ImportException(String s) {
        super(s);
    }
}
