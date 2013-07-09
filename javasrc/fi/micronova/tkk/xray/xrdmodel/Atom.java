package fi.micronova.tkk.xray.xrdmodel;
import fi.iki.jmtilli.javacomplex.Complex;
public interface Atom {
    public int getZ();
    public double bFactor();
    public double getMass(); /* in amu */
    public ASF asf();
    public Complex hoenl(double lambda) throws UnsupportedWavelength;
};
