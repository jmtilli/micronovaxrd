package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import java.io.*;
import java.util.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import fi.micronova.tkk.xray.complex.*;

/* immutable */
public class SimpleMaterial implements Material {
    private final String name, comment;
    private final Miller reflection;
    private final double xyspace, zspace, poisson;
    private final UnitCell unitcell;

    public boolean equals(Object o2) {
        if(this == o2)
            return true;
        if(o2 == null)
            return false;
        try {
            SimpleMaterial m2 = (SimpleMaterial)o2;
            return m2.name.equals(name) &&
                   m2.reflection.equals(reflection) &&
                   m2.xyspace == xyspace &&
                   m2.zspace == zspace &&
                   m2.poisson == poisson &&
                   m2.unitcell.equals(unitcell);
        }
        catch(ClassCastException ex) {
            return false;
        }
    }

    public String toString() {
        return "simple material: "+name;
    }

    public SimpleMaterial flatten() {
        return this;
    }
    public SimpleMaterial getStrainedMaterial(double forcexyspace) {
        double xystrain = (forcexyspace-xyspace)/xyspace;
        double zstrain = -2*poisson/(1-poisson)*xystrain;
        double newzspace = this.zspace*(1+zstrain);
        double V = unitcell.getV();
        double V2 = V * forcexyspace * forcexyspace * newzspace / xyspace / xyspace / zspace;
        UnitCell newCell = unitcell.distort(V2);
        return new SimpleMaterial(name,comment,reflection,forcexyspace,newzspace,poisson,newCell);
    }
    public double getXYSpace() {
        return xyspace;
    }
    public double getZSpace() {
        return zspace;
    }
    public double getPoisson() {
        return poisson;
    }
    public UnitCell getUnitCell() {
        return unitcell;
    }
    public Miller getReflection() {
        return reflection;
    }
    public String getName() {
        return name;
    }
    public String getComment() {
        return comment;
    }
    public String octaveRepr(double lambda) throws UnsupportedWavelength {
        StringBuffer buf = new StringBuffer();

        // atomic properties
        StringBuffer B = new StringBuffer();
        StringBuffer hoenl = new StringBuffer();
        StringBuffer sf_a = new StringBuffer();
        StringBuffer sf_b = new StringBuffer();
        StringBuffer sf_c = new StringBuffer();
        StringBuffer occupation = new StringBuffer();
        StringBuffer r = new StringBuffer();

        B.append("[");
        hoenl.append("[");
        sf_a.append("[");
        sf_b.append("[");
        sf_c.append("[");
        occupation.append("[");
        r.append("[");

        int maxGaussians = 0;
        for(LatticeAtom atom: unitcell.atoms) {
            ASF asf = atom.atom.asf();
            if(asf.gaussians.size() > maxGaussians)
                maxGaussians = asf.gaussians.size();
        }
        for(LatticeAtom atom: unitcell.atoms) {
            Complex atomHoenl = atom.atom.hoenl(lambda);
            ASF asf = atom.atom.asf();

            B.append(atom.atom.bFactor()+"; ...\n");
            hoenl.append("((" + atomHoenl.real + ")+(" + atomHoenl.imag + ")*i); ...\n");
            for(int i=0; i<asf.gaussians.size(); i++) {
                ASFGaussian gaussian = asf.gaussians.get(i);
                sf_a.append(gaussian.a+",");
                sf_b.append(gaussian.b+",");
            }
            for(int i=asf.gaussians.size(); i<maxGaussians; i++) {
                sf_a.append("0,");
                sf_b.append("0,");
            }
            sf_a.append("; ...\n");
            sf_b.append("; ...\n");
            sf_c.append(asf.c+"; ...\n");
            occupation.append(atom.occupation+"; ...\n");
            r.append(atom.pos.x+","+atom.pos.y+","+atom.pos.z+"; ...\n");
        }
        B.append("]");
        hoenl.append("]");
        sf_a.append("]");
        sf_b.append("]");
        sf_c.append("]");
        occupation.append("]");
        r.append("]");

        buf.append("cell2struct({");
        buf.append(unitcell.V);
        buf.append(", ...\n");
        buf.append(zspace);
        buf.append(", ...\n");
        buf.append(xyspace);
        buf.append(", ...\n");
        buf.append(poisson);
        buf.append(", ...\n");
        buf.append(B);
        buf.append(", ...\n");
        buf.append(hoenl);
        buf.append(", ...\n");
        buf.append(lambda);
        buf.append(", ...\n");
        buf.append(sf_a);
        buf.append(", ...\n");
        buf.append(sf_b);
        buf.append(",");
        buf.append(sf_c);
        buf.append(",");
        buf.append(occupation);
        buf.append(",");
        buf.append("["+reflection.h+","+reflection.k+","+reflection.l+"]");
        buf.append(",");
        buf.append(r);
        buf.append("},{'V','zspace','xyspace','poissonratio','B','hoenl',");
        buf.append("'lambda','sf_a','sf_b','sf_c','occupation','H','r'},2)");
        /* paitsi Z korjataan seuraavilla:
         * B
         * hoenl
         * lambda
         * sf_a
         * sf_b
         * sf_c
         */
        return buf.toString();
    }

    public SimpleMaterial(String name, String comment, Miller reflection, double xyspace, double zspace, double poisson, UnitCell unitcell) {
        this.name = name;
        this.comment = comment;
        this.reflection = reflection;
        this.xyspace = xyspace;
        this.zspace = zspace;
        this.poisson = poisson;
        this.unitcell = unitcell;
    }
    public SimpleMaterial(Node n, LookupTable table) throws ElementNotFound {
        this.name = n.getAttributes().getNamedItem("name").getNodeValue();
        this.xyspace = Double.parseDouble(n.getAttributes().getNamedItem("xy").getNodeValue());
        this.zspace = Double.parseDouble(n.getAttributes().getNamedItem("z").getNodeValue());
        this.poisson = Double.parseDouble(n.getAttributes().getNamedItem("poisson").getNodeValue());
        this.comment = XMLUtil.getNamedChildElements(n,"comment").get(0).getTextContent();
        this.reflection = new Miller(XMLUtil.getNamedChildElements(n,"refl").get(0));
        this.unitcell = new UnitCell(XMLUtil.getNamedChildElements(n,"unitcell").get(0), table);

        /*
        for(Node n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            String n2name = n2.getNodeName();
            if(n2.getNodeType() != Node.ELEMENT_NODE)
                continue;
            if(n2name.equals("comment")) {
                this.comment = n2.getTextContent();
            }
            else if(n2name.equals("refl")) {
                this.reflection = new Miller(n2);
            }
            else if(n2name.equals("unitcell")) {
                unitcell = new UnitCell(n2,table);
            }
        }
        */
    }
    public Element export(Document doc) {
        Element mat,comment,refl,spacing;

        mat = doc.createElement("mat");
        mat.setAttribute("name",this.name);
        mat.setAttribute("xy",""+xyspace);
        mat.setAttribute("z",""+zspace);
        mat.setAttribute("poisson",""+poisson);
        //mat.appendChild(name = doc.createElement("name"));
        //name.appendChild(doc.createTextNode(this.name));
        mat.appendChild(comment = doc.createElement("comment"));
        comment.appendChild(doc.createTextNode(this.comment));
        mat.appendChild(reflection.export(doc,"refl"));
        mat.appendChild(unitcell.export(doc));
        return mat;
    }




    public static void main(String[] args) {
        Material absmat;
        SimpleMaterial mat;
        try {
            DocumentBuilderFactory bf;
            DocumentBuilder b;
            Document d;
            TransformerFactory tf;
            Transformer t;
            LookupTable tab = SFTables.defaultLookup();

            bf = DocumentBuilderFactory.newInstance();
            b = bf.newDocumentBuilder();
            d = b.parse(new FileInputStream(new File(args[0])));
            //mat = new SimpleMaterial(d.getDocumentElement(),new DummyLookupTable());
            absmat = MaterialImportDispatcher.doImport(d.getDocumentElement(),tab);
            mat = absmat.flatten();
            System.out.println(absmat.octaveRepr(1.54056e-10));
            System.out.println(mat.susc(1.54056e-10));
            /*
            System.out.println(mat.getName());
            System.out.println(mat.getComment());
            System.out.println(mat.getReflection().h);
            System.out.println(mat.getXYSpace());
            System.out.println(mat.getPoisson());
            d = b.newDocument();
            d.appendChild(mat.export(d));
            tf = TransformerFactory.newInstance();
            t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(d), new StreamResult(System.out));
            */
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
        catch(ParserConfigurationException ex) {
            ex.printStackTrace();
        }
        /*
        catch(TransformerConfigurationException ex) {
            ex.printStackTrace();
        }
        catch(TransformerException ex) {
            ex.printStackTrace();
        }
        */
        catch(SAXException ex) {
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public Susceptibilities susc(double lambda) throws UnsupportedWavelength { /* structure factor */
        Complex chi_0 = unitcell.susc(new Miller(), 0, lambda);
        Complex chi_h = unitcell.susc(reflection, 1/(2*zspace), lambda);
        Complex chi_h_neg = unitcell.susc(reflection.neg(), 1/(2*zspace), lambda);
        return new Susceptibilities(chi_0, chi_h, chi_h_neg);
    }
};
