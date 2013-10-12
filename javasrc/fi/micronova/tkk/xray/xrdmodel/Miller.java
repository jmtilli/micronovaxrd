package fi.micronova.tkk.xray.xrdmodel;
import fi.iki.jmtilli.javaxmlfrag.*;

public class Miller implements XMLRowable {
    public final int h,k,l;

    public Miller() {
        this(0, 0, 0);
    }
    public Miller(int h, int k, int l) {
        this.h = h;
        this.k = k;
        this.l = l;
    }
    public Miller(DocumentFragment frag) {
        h = frag.getAttrIntNotNull("h");
        k = frag.getAttrIntNotNull("k");
        l = frag.getAttrIntNotNull("l");
    }
    public void toXMLRow(DocumentFragment frag) {
        frag.setAttrInt("h", this.h);
        frag.setAttrInt("k", this.k);
        frag.setAttrInt("l", this.l);
    }
    public boolean equals(Object o) {
        if(o == null)
            return false;
        try {
            Miller m2 = (Miller)o;
            return h == m2.h && k == m2.k && l == m2.l;
        }
        catch(ClassCastException ex) {
            return false;
        }
    }
    public Miller neg() {
        return new Miller(-h, -k, -l);
    }
    public String toString()
    {
      if (   h < 10 && k < 10 && l < 10
          && h >= 0 && k >= 0 && l >= 0)
      {
        return "(" + h + "" + k + "" + l + ")";
      }
      return "(" + h + " " + k + " " + l + ")";
    }
}
