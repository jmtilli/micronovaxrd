package fi.micronova.tkk.xray.chart;

/** Array of data with a scaling factor and a name.
 *
 * This class contains an array of real-valued data and a scaling factor
 * to convert the data to the desired unit. It also has a name.
 */
public class NamedArray extends DataArray {
    public final String name;
    public NamedArray(double scale, double[] al, String name) {
        super(scale, al);
        this.name = name;
    }
};
