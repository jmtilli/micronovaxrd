package fi.micronova.tkk.xray.chart;

/** Array of data with a scaling factor.
 *
 * This class contains an array of real-valued data and a scaling factor
 * to convert the data to the desired unit.
 */
public class DataArray {
    public final double scale; /* used when plotting */
    public final double[] array;
    public DataArray(double scale, double[] array) {
        this.scale = scale;
        this.array = array;
    }
};
