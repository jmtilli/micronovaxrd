package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
/* Layer */

import java.util.*;
import org.w3c.dom.*;



/** The layer class.
 *
 * <p>
 *
 * The layer class is used to store information about one layer.  A layer has a
 * name and properties. Thickness, density and roughness are stored as a
 * FitValue with minimum, maximum and fitted values.  Chemical composition,
 * which is modeled by a mixture of two compounds, is not fitted.
 *
 * <p>
 *
 * Layer objects are mutable, so access must be synchronized carefully if
 * accessed by multiple threads even if only one thread modifies the object.
 * Objects can be copied by the deepCopy method. Another possible way to
 * implement thread safety is to make a copy of the object for each thread.
 *
 * <p>
 *
 * Layer objects store also wavelength and the lookup table used to find
 * elements. These are needed to allow changing composition without specifying
 * the wavelength and the lookup table each time. Changes to composition will
 * throw an exception if an element is not found in the lookup table for the
 * wavelength.
 *
 * <p>
 *
 * Fencode serialization is implemented by structImport and structExport which
 * convert between a layer object and its fencodeable structure representation.
 *
 * <p>
 *
 * All units are SI units.
 *
 */

public class Layer implements ValueListener {
    private String name;

    /* the following FitValues are "owned" by this object and are never
     * actually changed (their new values are set by deepCopyFrom */
    private final FitValue d, p, r; /* thickness (in SI units), composition */

    private Material mat1, mat2;
    private Mixture mixture; /* this must be changed whenever mat1 and mat2 are */

    private final Set<LayerListener> listeners = new HashSet<LayerListener>();

    public void addLayerListener(LayerListener listener) {
        listeners.add(listener);
    }
    public void removeLayerListener(LayerListener listener) {
        listeners.remove(listener);
    }

    public Layer(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
        List<Material> materials = new ArrayList<Material>();
        this.name = n.getAttributes().getNamedItem("name").getNodeValue();
        for(Node n2 = n.getFirstChild(); n2 != null; n2 = n2.getNextSibling()) {
            if(n2.getNodeType() != Node.ELEMENT_NODE)
                continue;
            Material mat = MaterialImportDispatcher.doImport(n2,table);
            if(mat != null)
                materials.add(mat);
        }
        Node dNode = XMLUtil.getNamedChildElements(n,"d").get(0);
        Node pNode = XMLUtil.getNamedChildElements(n,"p").get(0);
        if(!XMLUtil.getNamedChildElements(n,"r").isEmpty()) {
            Node rNode = XMLUtil.getNamedChildElements(n,"r").get(0);
            r = new FitValue(XMLUtil.getNamedChildElements(rNode,"fitvalue").get(0));
        } else {
            r = new FitValue(0,0,1,false);
        }
        d = new FitValue(XMLUtil.getNamedChildElements(dNode,"fitvalue").get(0));
        p = new FitValue(XMLUtil.getNamedChildElements(pNode,"fitvalue").get(0));
        d.addValueListener(this);
        p.addValueListener(this);
        r.addValueListener(this);
        assert(materials.size() == 2);
        mat1 = materials.get(0);
        mat2 = materials.get(1);

        mix(mat1, mat2, p);
    }

    public Element export(Document doc) {
        Element layerElement = doc.createElement("layer");
        Element dprop = doc.createElement("d");
        Element pprop = doc.createElement("p");
        Element rprop = doc.createElement("r");
        layerElement.setAttribute("name",name);
        dprop.appendChild(d.export(doc));
        pprop.appendChild(p.export(doc));
        rprop.appendChild(r.export(doc));
        layerElement.appendChild(dprop);
        layerElement.appendChild(pprop);
        layerElement.appendChild(rprop);
        layerElement.appendChild(mat1.export(doc));
        layerElement.appendChild(mat2.export(doc));
        return layerElement;
    }

    /** This can be sent by a FitValue */
    public void valueChanged(ValueEvent ev) {
        signalEvent(null);
    };

    /* The mixture is a dummy parameter: it is used to distinguish between different constructors */
    private Layer(String name, FitValue d, FitValue p, FitValue r, Material mat1, Material mat2, Mixture mixture) {
        this.name = name;
        this.d = d.deepCopy();
        this.p = p.deepCopy();
        this.r = r.deepCopy();
        this.d.addValueListener(this);
        this.p.addValueListener(this);
        this.r.addValueListener(this);
        this.mat1 = mat1;
        this.mat2 = mat2;
        //this.mixture = mixture;
    }

    private static Mixture mix(Material mat1, Material mat2, FitValue p) throws InvalidMixtureException {
        List<Mixture.Constituent> constituents = new ArrayList<Mixture.Constituent>();
        constituents.add(new Mixture.Constituent(1-p.getExpected(), mat1));
        constituents.add(new Mixture.Constituent(p.getExpected(), mat2));
        return new Mixture(constituents);
    }

    public Layer(String name, FitValue d, FitValue p, FitValue r, Material mat1, Material mat2) throws InvalidMixtureException {
        this(name, d, p, r, mat1, mat2, mix(mat1, mat2, p));
    }

    public Layer deepCopy() {
        return new Layer(name, d.deepCopy(), p.deepCopy(), r.deepCopy(), mat1, mat2, null);
    }

    /** Changes the two compounds.
     *
     * <p>
     *
     * This method changes the two compounds this layer consists of. If an
     * element from a compound is not found in the lookup table for the used
     * wavelength, an exception is thrown and the layer composition is rolled
     * back.
     */
    public void setMaterials(Material mat1, Material mat2) throws InvalidMixtureException {
        /* the order is exception-safe */
        mix(mat1, mat2, p);
        this.mat1 = mat1;
        this.mat2 = mat2;

        signalEvent(null);
    };

    private void signalEvent(LayerEvent ev) {
        if(ev == null)
            ev = new LayerEvent(this);
        for(LayerListener listener: listeners)
            listener.layerPropertyChanged(ev);
    }

    public String getName() {
        return name;
    }

    /* set all values without having to send multiple events */
    public void newValues(String name, FitValue d, FitValue p, FitValue r, Material mat1, Material mat2) throws InvalidMixtureException {
        mix(mat1, mat2, p);
        this.mat1 = mat1;
        this.mat2 = mat2;
        this.name = name;

        /* Disable event temporarily in order not to flood listeners of this
         * Layer with events. Other listeners that have directly attached to
         * FitValues are sent events, but that's just what we want. */

        this.d.removeValueListener(this);
        this.d.deepCopyFrom(d);
        this.d.addValueListener(this);
        this.p.removeValueListener(this);
        this.p.deepCopyFrom(p);
        this.p.addValueListener(this);
        this.r.removeValueListener(this);
        this.r.deepCopyFrom(r);
        this.r.addValueListener(this);


        signalEvent(null);
    }

    /** Changes the FitValue object of layer relaxation.
     *
     * <p>
     *
     * The layer relaxation can also be changed by modifying the existing FitValue
     * returned by getRelaxation().
     *
     */
    public void setRelaxation(FitValue r) {
        /*
        this.r.removeValueListener(this);
        this.r = r;
        r.addValueListener(this);
        */

        this.r.deepCopyFrom(r);
        //signalEvent(null);
    };

    /** Changes the FitValue object of layer thickness.
     *
     * <p>
     *
     * The layer thickness can also be changed by modifying the existing FitValue
     * returned by getThickness().
     *
     */
    public void setThickness(FitValue d) {
        /*
        this.d.removeValueListener(this);
        this.d = d;
        d.addValueListener(this);
        */

        this.d.deepCopyFrom(d);
        //signalEvent(null);
    };
    /** Changes the FitValue object of composition.
     *
     * <p>
     *
     * The roughness can also be changed by modifying the existing FitValue
     * returned by getComposition().
     */
    public void setComposition(FitValue p) {
        /*
        this.p.removeValueListener(this);
        this.p = p;
        p.addValueListener(this);
        */
        this.p.deepCopyFrom(p);
        //signalEvent(null);
    };

    public FitValue getRelaxation() {
        return r;
    }
    public Material getMat1() {
        return mat1;
    }
    public Material getMat2() {
        return mat2;
    }
    public FitValue getThickness() {
        return d;
    }
    public FitValue getComposition() {
        return p;
    }

    public double calcXYSpace(double basexyspace) {
        try {
            SimpleMaterial material;
            double xyspace;
            Mixture mixture = mix(mat1, mat2, p);
            double rel = r.getExpected();

            material = mixture.flatten();
            xyspace = material.getXYSpace();

            if(basexyspace != 0)
                return basexyspace*(1-rel) + xyspace*rel;
            else
                return xyspace;
        }
        catch(InvalidMixtureException ex) {
            throw new RuntimeException("never happens"); /* XXX: very ugly hack */
        }
    }

    public SimpleMaterial getSimpleMaterial(double forcexyspace) {
        try {
            SimpleMaterial material;
            Mixture mixture = mix(mat1, mat2, p);
            double rel = r.getExpected();

            material = mixture.flatten();

            if(forcexyspace != 0)
                material = material.getStrainedMaterial(forcexyspace);

            return material;
        }
        catch(InvalidMixtureException ex) {
            throw new RuntimeException("never happens"); /* XXX: very ugly hack */
        }
    }

    /** String representation.
     *
     * <p>
     *
     * The methods returns a string representation for the layer which is shown
     * in layer stack lists.
     *
     */
    public String toString() {
        return this.name +
	", d = " + String.format(Locale.US,"%.6g",this.d.getExpected()*1e9) + " nm " + (this.d.getEnabled() ? "(fit)" : "(no fit)") +
	", p = " + String.format(Locale.US,"%.6g",this.p.getExpected()) + " " + (this.p.getEnabled() ? "(fit)" : "(no fit)") +
	", r = " + String.format(Locale.US,"%.6g",this.r.getExpected()) + " " + (this.r.getEnabled() ? "(fit)" : "(no fit)");
    }

};
