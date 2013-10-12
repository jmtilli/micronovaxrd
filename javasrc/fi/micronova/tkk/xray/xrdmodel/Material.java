package fi.micronova.tkk.xray.xrdmodel;
import fi.iki.jmtilli.javaxmlfrag.*;


public interface Material {
    public SimpleMaterial flatten();
    public String toString();
    /**
       Return XML representation of the material.

       This class does not implement XMLRowable because the name of the
       element depends on the type of the material, so the whole element
       needs to be constructed in this method.

       @return XML representation of the material
     */
    public DocumentFragment toXMLRow();
    public String octaveRepr(double lambda) throws UnsupportedWavelength;
};

