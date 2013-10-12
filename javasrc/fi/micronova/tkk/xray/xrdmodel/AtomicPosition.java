package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;

public class AtomicPosition implements XMLRowable {
    public final double x,y,z;
    public AtomicPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public AtomicPosition(DocumentFragment frag)
    {
        x = frag.getAttrDoubleNotNull("x");
        y = frag.getAttrDoubleNotNull("y");
        z = frag.getAttrDoubleNotNull("z");
    }
    public void toXMLRow(DocumentFragment frag)
    {
        frag.setAttrDouble("x", this.x);
        frag.setAttrDouble("y", this.y);
        frag.setAttrDouble("z", this.z);
    }
    public double dot(Miller m) {
        return x*m.h + y*m.k + z*m.l;
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
