package fi.micronova.tkk.xray.xrdmodel;

/** X-ray properties of an element are not found.
 * 
 * Thrown when the x-ray properties of an element for a specific wavelength are
 * not found in the lookup table.
 *
 */

public class ElementNotFound extends Exception {
    /** Constructor
     *
     * @param s An English message specifying the element and wavelength for
     * which the properties are not found. This is intended to be shown to the
     * user.
     */
    public ElementNotFound(String s) {
        super(s);
    }
}
