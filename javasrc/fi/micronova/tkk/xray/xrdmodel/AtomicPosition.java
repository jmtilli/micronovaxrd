package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import org.w3c.dom.*;

public class AtomicPosition {
    public final double x,y,z;
    public AtomicPosition(Node n) {
        x = Double.parseDouble(n.getAttributes().getNamedItem("x").getNodeValue());
        y = Double.parseDouble(n.getAttributes().getNamedItem("y").getNodeValue());
        z = Double.parseDouble(n.getAttributes().getNamedItem("z").getNodeValue());
    }
    public double dot(Miller m) {
        return x*m.h + y*m.k + z*m.l;
    }
    public Element export(Document doc) {
        Element pos;
        pos = doc.createElement("pos");
        pos.setAttribute("x",""+this.x);
        pos.setAttribute("y",""+this.y);
        pos.setAttribute("z",""+this.z);
        return pos;
    }
    public boolean equals(Object o2) {
        if(o2 == null)
            return false;
        if(this == o2)
            return true;
        try {
            AtomicPosition p2 = (AtomicPosition)o2;
            return p2.x == x && p2.y == y && p2.z == z;
        }
        catch(ClassCastException ex) {
            return false;
        }
    }
};
