package fi.micronova.tkk.xray;
/* Used by DataDialog */

public class DataOptions {
    int ndata;
    double min, max;
    public DataOptions(int ndata, double min, double max) {
        this.ndata = ndata;
        this.min = min;
        this.max = max;
    }
}
