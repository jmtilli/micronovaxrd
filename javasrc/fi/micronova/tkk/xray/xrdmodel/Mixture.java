package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import java.util.*;
import org.w3c.dom.*;

/* immutable */
public class Mixture implements Material {
    public static class Constituent {
        public final double p;
        public final Material mat; /* we can flatten this */
        public Constituent(double p, Material mat) {
            this.p = p;
            this.mat = mat;
        }
        public Constituent(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
            p = Double.parseDouble(n.getAttributes().getNamedItem("p").getNodeValue());
            mat = MaterialImportDispatcher.doImport(XMLUtil.getChildElements(n).get(0),table);
        }
        public Element export(Document doc) {
            Element c;
            c = doc.createElement("constituent");
            c.setAttribute("p",""+p);
            c.appendChild(mat.export(doc));
            return c;
        }
    };
    private static class SimpleConstituent {
        public final double p;
        public final SimpleMaterial mat;
        public SimpleConstituent(double p, SimpleMaterial mat) {
            this.p = p;
            this.mat = mat;
        }
    };
    public final List<Constituent> materials;
    public final Miller reflection;
    public final double avgUlVolume;

    public String octaveRepr(double lambda) throws UnsupportedWavelength {
        List<SimpleConstituent> flatMaterials = simpleList();
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        for(SimpleConstituent sc: flatMaterials) {
            buf.append(sc.p);
            buf.append(",");
            buf.append(sc.mat.octaveRepr(lambda));
            buf.append(";");
        }
        buf.append("}");
        return buf.toString();
        //return this.flatten().octaveRepr(lambda);
    }

    public String toString() {
        return "mixture";
    }

    private static List<Constituent> doImport(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
        List<Constituent> materials = new ArrayList<Constituent>();
        for(Node n2: XMLUtil.getNamedChildElements(n,"constituent"))
            materials.add(new Constituent(n2,table));
        return materials;
    }

    public Mixture(List<? extends Constituent> materials) throws InvalidMixtureException {
        double avgUlVolume = 0;
        List<Double> ulVolumes = new ArrayList<Double>(); /* unitless volumes; v/(xyspace^2*zspace) */
        Miller reflection = null;
        List<SimpleConstituent> flatMaterials;

        this.materials = Collections.unmodifiableList(new ArrayList<Constituent>(materials));
        flatMaterials = simpleList();

        for(SimpleConstituent c: flatMaterials) {
            if(reflection != null) {
                if(!reflection.equals(c.mat.getReflection()))
                    throw new InvalidMixtureException("Mixture of materials with different miller indices");
            } else {
                reflection = c.mat.getReflection();
            }
            ulVolumes.add(c.mat.getUnitCell().getV() / (c.mat.getXYSpace() * c.mat.getXYSpace() * c.mat.getZSpace()));
        }
        for(double ulV: ulVolumes) {
            avgUlVolume += ulV;
        }
        avgUlVolume /= ulVolumes.size();
        for(double ulV: ulVolumes) {
            if(Math.abs(ulV/avgUlVolume - 1) > 3e-4)
                throw new InvalidMixtureException("Mixture of materials with different unit cell structures");
        }

        if(reflection == null)
            throw new InvalidMixtureException("empty mixture");

        this.reflection = reflection;
        this.avgUlVolume = avgUlVolume;
    }

    public Mixture(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
        this(doImport(n, table));
    }
    public Element export(Document doc) {
        Element mix;
        mix = doc.createElement("mixture");
        for(Constituent c: materials)
            mix.appendChild(c.export(doc));
        return mix;
    }

    private List<SimpleConstituent> simpleList() {
        List<SimpleConstituent> flatMaterials = new ArrayList<SimpleConstituent>();
        for(Constituent c: materials) {
            flatMaterials.add(new SimpleConstituent(c.p, c.mat.flatten()));
        }
        return flatMaterials;
    }

    public SimpleMaterial flatten() {
        SimpleMaterial flat;
        List<SimpleConstituent> flatMaterials = simpleList();
        UnitCell cell;
        double xyspace = 0, zspace = 0, poisson = 0, propsum = 0;

        for(SimpleConstituent c: flatMaterials) {
            propsum += c.p;
            xyspace += c.p*c.mat.getXYSpace();
            zspace += c.p*c.mat.getZSpace();
            poisson += c.p*c.mat.getPoisson();
        }
        zspace /= propsum;
        xyspace /= propsum;
        poisson /= propsum;

        cell = new UnitCell(avgUlVolume*xyspace*xyspace*zspace);

        for(SimpleConstituent c: flatMaterials) {
            cell = cell.append(c.mat.getUnitCell().getProportion(c.p/propsum));
        }
        return new SimpleMaterial("flattened material","flattened material",reflection, xyspace, zspace, poisson, cell);
    }

    /* the different compounds must have the same crystal structure. Otherwise
     * averaging their susceptibilities makes no sense. */

    /*
    public Susceptibility getSusceptibility(double lambda) {
        double psum = 0;
        double zspace = getZSpace();
        double xyspace = getXYSpace();
        double x0r = 0, x0i = 0;
        double xhr = 0, xhi = 0;
        double xhinvr = 0, xhinvi = 0;
        for(Constituent c: materials) {
            Susceptibility s = c.mat.getSusceptibility(lambda);
            double oldxyspace = c.mat.getXYSpace();
            double oldzspace = c.mat.getZSpace();
            // proportional change of volume and equivalently susceptibility
            // due to compression or expansion of the material
            double pV = (zspace*xyspace*xyspace)/(oldzspace*oldxyspace*oldxyspace);

            // s_new = (V_old*s_old) / V_new = s_old / pV
            x0r += (c.p/pV) * s.x0r;
            x0i += (c.p/pV) * s.x0i;
            xhr += (c.p/pV) * s.xhr;
            xhi += (c.p/pV) * s.xhi;
            xhinvr += (c.p/pV) * s.xhinvr;
            xhinvi += (c.p/pV) * s.xhinvi;

            psum += c.p;
        }
        x0r /= psum;
        x0i /= psum;
        xhr /= psum;
        xhi /= psum;
        xhinvr /= psum;
        xhinvi /= psum;
        return new Susceptibility(lambda, x0r, x0i, xhr, xhi, xhinvr, xhinvi);
    }
    */
};
