package fi.micronova.tkk.xray.xrdmodel;
import org.w3c.dom.*;

public class Miller {
    public final int h,k,l;

    public Miller() {
        this(0, 0, 0);
    }
    public Miller(int h, int k, int l) {
        this.h = h;
        this.k = k;
        this.l = l;
    }
    public Miller(Node n) {
        h = Integer.parseInt(n.getAttributes().getNamedItem("h").getNodeValue());
        k = Integer.parseInt(n.getAttributes().getNamedItem("k").getNodeValue());
        l = Integer.parseInt(n.getAttributes().getNamedItem("l").getNodeValue());
    }
    public Element export(Document doc, String nodeName) {
        Element e = doc.createElement(nodeName);
        e.setAttribute("h",""+this.h);
        e.setAttribute("k",""+this.k);
        e.setAttribute("l",""+this.l);
        return e;
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
