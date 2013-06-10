package fi.micronova.tkk.xray.xrdmodel;
import org.w3c.dom.*;


public interface Material {
    public SimpleMaterial flatten();
    public String toString();
    public Element export(Document doc);
    public String octaveRepr(double lambda) throws UnsupportedWavelength;
};

