package fi.micronova.tkk.xray.octif;

/** Octave error
 */
public class OctException extends Exception {
    public OctException() {
        super("Octave error");
    }
    public OctException(String s) {
        super(s);
    }
};
