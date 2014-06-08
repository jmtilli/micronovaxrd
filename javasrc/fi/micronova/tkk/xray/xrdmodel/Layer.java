package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
/* Layer */

import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;



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

    /* the following FitValues are "owned" by this object and are not
       usually actually changed (their new values are set by deepCopyFrom)

       However, the setXXXObject() methods do change these values. Calling
       the setXXXObject() method is allowed only for layers that are not
       owned by any layer stack
     */
    private FitValue d, p; /* thickness (in SI units), composition */
    private FitValue r, wh; /* degree of relaxation, susceptibility factor */

    private Material mat1, mat2;
    //private Mixture mixture; /* this must be changed whenever mat1 and mat2 are */

    private final Set<LayerListener> listeners = new HashSet<LayerListener>();

    private static boolean utilEquals(Object o1, Object o2)
    {
      if (o1 == null)
      {
        return o2 == null;
      }
      if (o2 == null)
      {
        return false;
      }
      return o1.equals(o2);
    }
    /**
       Deep equality comparison for layers.

       LayerStack.getNumbering() requires that the equals method of Layer is
       the same as for objects, ie. two layers are the same only if they are
       the same object.

       This method here does deep equality comparison, ie. it compares the
       contents of the objects.
     */
    public static boolean layerDeepEquals(Layer l1,
                                          Map<FitValue,Integer> l1_numbering,
                                          Layer l2,
                                          Map<FitValue,Integer> l2_numbering)
    {
      if (   l1_numbering.get(l1.d) != l2_numbering.get(l2.d)
          || l1_numbering.get(l1.p) != l2_numbering.get(l2.p)
          || l1_numbering.get(l1.r) != l2_numbering.get(l2.r)
          || l1_numbering.get(l1.wh) != l2_numbering.get(l2.wh))
      {
        return false;
      }
      if (   !utilEquals(l1.name, l2.name)
          || !utilEquals(l1.d, l2.d)
          || !utilEquals(l1.p, l2.p)
          || !utilEquals(l1.r, l2.r)
          || !utilEquals(l1.wh, l2.wh)
          || !utilEquals(l1.mat1, l2.mat1)
          || !utilEquals(l1.mat2, l2.mat2))
      {
        return false;
      }
      return true;

    }

    public void addLayerListener(LayerListener listener) {
        listeners.add(listener);
    }
    public void removeLayerListener(LayerListener listener) {
        listeners.remove(listener);
    }

    private FitValue getFitValue(DocumentFragment f,
                                 Map<String, FitValue> fitValueById)
    {
        if (f.getNotNull("fitvalue").getAttrStringObject("fitvalrefid") != null)
        {
            FitValue result = fitValueById.get(
                f.getNotNull("fitvalue").getAttrStringNotNull("fitvalrefid"));
            if (result == null)
            {
                throw new NullPointerException();
            }
            return result;
        }
        FitValue result = new FitValue(f.getNotNull("fitvalue"));
        String id = f.getNotNull("fitvalue").getAttrStringObject("fitvalid");
        if (id != null)
        {
            fitValueById.put(id, result);
        }
        return result;
    }

    public Layer(DocumentFragment frag, LookupTable table,
                 Map<String, FitValue> fitValueById)
      throws ElementNotFound, InvalidMixtureException
    {
        List<Material> materials = new ArrayList<Material>();
        this.name = frag.getAttrStringNotNull("name");
        d = getFitValue(frag.getNotNull("d"), fitValueById);
        p = getFitValue(frag.getNotNull("p"), fitValueById);
        if (frag.get("r") != null)
        {
            r = getFitValue(frag.getNotNull("r"), fitValueById);
        }
        else
        {
            r = new FitValue(0,0,1,false);
        }
        if (frag.get("wh") != null)
        {
            wh = getFitValue(frag.getNotNull("wh"), fitValueById);
        }
        else
        {
            wh = new FitValue(0,1,1,false);
        }
        for(DocumentFragment frag2: frag.getChildren()) {
            Material mat = MaterialImportDispatcher.doImport(frag2, table);
            if(mat != null)
                materials.add(mat);
        }
        d.addValueListener(this);
        p.addValueListener(this);
        r.addValueListener(this);
        wh.addValueListener(this);
        if(materials.size() != 2)
        {
            throw new RuntimeException("must have 2 materials");
        }
        mat1 = materials.get(0);
        mat2 = materials.get(1);

        mix(mat1, mat2, p);
    }

    public XMLRowable xmlRowable(final Map<FitValue, Integer> fitValueNumbering,
                                 final Set<FitValue> alreadyAdded)
    {
        return new XMLRowable() {
            public void setFitValue(DocumentFragment f, String name,
                                    FitValue val)
            {
                if (fitValueNumbering.containsKey(val))
                {
                    if (alreadyAdded.contains(val))
                    {
                        f.set(name).setRow("fitvalue", val);
                        f.get(name).get("fitvalue").setAttrInt(
                            "fitvalrefid", fitValueNumbering.get(val));
                    }
                    else
                    {
                        f.set(name).setRow("fitvalue", val);
                        f.get(name).get("fitvalue").setAttrInt(
                            "fitvalid", fitValueNumbering.get(val));
                        alreadyAdded.add(val);
                    }
                }
                else
                {
                    f.set(name).setRow("fitvalue", val);
                }
            }
            public void toXMLRow(DocumentFragment f) {
                f.setAttrString("name", name);
                setFitValue(f, "d", d);
                setFitValue(f, "p", p);
                setFitValue(f, "r", r);
                setFitValue(f, "wh", wh);
                // NB: these may have the same tag name (mixture or mat):
                f.add(mat1.toXMLRow());
                f.add(mat2.toXMLRow());
            }
        };
    }

    /** This can be sent by a FitValue */
    public void valueChanged(ValueEvent ev) {
        signalEvent(null);
    };

    /* The mixture is a dummy parameter: it is used to distinguish between different constructors */
    private Layer(String name, FitValue d, FitValue p, FitValue r, FitValue wh, Material mat1, Material mat2, Mixture mixture) {
        this.name = name;
        this.d = d;
        this.p = p;
        this.r = r;
        this.wh = wh;
        this.d.addValueListener(this);
        this.p.addValueListener(this);
        this.r.addValueListener(this);
        this.wh.addValueListener(this);
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

    public Layer(String name, FitValue d, FitValue p, FitValue r, FitValue wh, Material mat1, Material mat2) throws InvalidMixtureException {
        this(name, d.deepCopy(), p.deepCopy(), r.deepCopy(), wh.deepCopy(), mat1, mat2, mix(mat1, mat2, p));
    }

    private static FitValue newFitValue(
        Map<FitValue, Integer> fitValueNumbering,
        Map<Integer, FitValue> newFitValues,
        FitValue old)
    {
        if (fitValueNumbering.containsKey(old))
        {
            int i = fitValueNumbering.get(old);
            if (newFitValues.containsKey(i))
            {
                return newFitValues.get(i);
            }
            FitValue val = old.deepCopy();
            newFitValues.put(i, val);
            return val;
        }
        else
        {
          return old.deepCopy();
        }
    }

    public Layer deepCopy(Map<FitValue, Integer> fitValueNumbering,
                          Map<Integer, FitValue> newFitValues)
    {
        return new Layer(
            name,
            newFitValue(fitValueNumbering, newFitValues, d),
            newFitValue(fitValueNumbering, newFitValues, p),
            newFitValue(fitValueNumbering, newFitValues, r),
            newFitValue(fitValueNumbering, newFitValues, wh),
            mat1, mat2, null);
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
    public void newValues(String name, FitValue d, FitValue p, FitValue r, FitValue wh, Material mat1, Material mat2) throws InvalidMixtureException {
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
        this.wh.removeValueListener(this);
        this.wh.deepCopyFrom(wh);
        this.wh.addValueListener(this);


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

    public Layer getAllLinked() {
        Layer result = this.deepCopy(new HashMap<FitValue,Integer>(),
                                     new HashMap<Integer,FitValue>());
        result.setThicknessObject(this.getThickness());
        result.setCompositionObject(this.getComposition());
        result.setRelaxationObject(this.getRelaxation());
        result.setSuscFactorObject(this.getSuscFactor());
        return result;
    }

    /**
       Change the thickness object to be the same object as the parameter.
      
       Calling this is only allowed for layers that are not owned by a layer
       stack!
      
       @param d The new thickness object
     */
    public void setThicknessObject(FitValue d)
    {
        this.d.removeValueListener(this);
        this.d = d;
        this.d.addValueListener(this);
    }
    /**
       Change the composition object to be the same object as the parameter.
      
       Calling this is only allowed for layers that are not owned by a layer
       stack!
      
       @param p The new composition object
     */
    public void setCompositionObject(FitValue p)
    {
        this.p.removeValueListener(this);
        this.p = p;
        this.p.addValueListener(this);
    }
    /**
       Change the relaxation object to be the same object as the parameter.
      
       Calling this is only allowed for layers that are not owned by a layer
       stack!
      
       @param r The new relaxation object
     */
    public void setRelaxationObject(FitValue r)
    {
        this.r.removeValueListener(this);
        this.r = r;
        this.r.addValueListener(this);
    }
    /**
       Change the susceptibility factor object to be the same object as the
       parameter.
      
       Calling this is only allowed for layers that are not owned by a layer
       stack!
      
       @param wh The new susceptibility factor object
     */
    public void setSuscFactorObject(FitValue wh)
    {
        this.wh.removeValueListener(this);
        this.wh = wh;
        this.wh.addValueListener(this);
    }

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

    /** Changes the FitValue object of susceptibility factor.
     *
     * <p>
     *
     * The susceptibility factor can also be changed by modifying the existing FitValue
     * returned by getSuscFactor().
     */
    public void setSuscFactor(FitValue wh) {
        /*
        this.p.removeValueListener(this);
        this.p = p;
        p.addValueListener(this);
        */
        this.wh.deepCopyFrom(wh);
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
    public FitValue getSuscFactor() {
        return wh;
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
      
       <p>
      
       The methods returns a string representation for the layer which is shown
       in layer stack lists.
      
       @return String representation
      
     */
    public String toString() {
        return toString(new HashMap<FitValue, Integer>());
    }
    /** String representation.
      
       <p>
      
       The methods returns a string representation for the layer which is shown
       in layer stack lists.
      
       @param fitValueNumbering Link numbers for linked FitValues
       @return String representation
     */
    public String toString(Map<FitValue, Integer> fitValueNumbering) {
        final Map<FitValue, Integer> n = fitValueNumbering;
        return this.name +
        ", d = " + String.format(Locale.US,"%.6g",this.d.getExpected()*1e9) + " nm " +
          (n.containsKey(this.d) ? "[L" + n.get(this.d) + "] " : "") +
          (this.d.getEnabled() ? "(fit)" : "(no fit)") +
        ", p = " + String.format(Locale.US,"%.6g",this.p.getExpected()) + " " +
          (n.containsKey(this.p) ? "[L" + n.get(this.p) + "] " : "") +
          (this.p.getEnabled() ? "(fit)" : "(no fit)") +
        ", r = " + String.format(Locale.US,"%.6g",this.r.getExpected()) + " " +
          (n.containsKey(this.r) ? "[L" + n.get(this.r) + "] " : "") +
          (this.r.getEnabled() ? "(fit)" : "(no fit)") +
        ", wh = " + String.format(Locale.US,"%.6g",this.wh.getExpected()) + " " +
          (n.containsKey(this.wh) ? "[L" + n.get(this.wh) + "] " : "") +
          (this.wh.getEnabled() ? "(fit)" : "(no fit)");
    }

};
