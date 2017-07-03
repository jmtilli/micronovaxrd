package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.octif.*;
import fi.micronova.tkk.xray.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import org.w3c.dom.*;
import java.io.*;
import fi.micronova.tkk.xray.complex.*;


/** Layer model.
 *
 * <p>
 *
 * The LayerStack contains all the necessary information of the layer model and
 * other experimental parameters to calculate and fit diffraction curves.
 * LayerStack objects contain all individual layers, the wavelength used for
 * the experiment, instrumental resolution and a lookup table for elements.
 *
 * <p>
 *
 * LayerStack objects are mutable and not thread safe. If accessed from
 * multiple threads, access must be synchronized properly. Another thread
 * safety approach is to make a deep copy of LayerStack for each thread and
 * then copy the information back from the thread that has modified its deep
 * copy of the LayerStack object. 
 *
 * <p>
 *
 * The LayerStack implements ListModel in order to support showing its layers
 * in a list. ListDataEvents are reported after a change for any property of
 * the layer stack. ListDataListeners can also be used for other purposes. The
 * user interface of this program makes use of ListDataListeners to show
 * wavelength and instrument resolution in text labels, to plot diffraction
 * curves and to update the positions of sliders.
 *
 * <p>
 *
 * LayerStack supports XML serialization. Of course, listeners are not
 * stored in XML structures.
 *
 */

public class LayerStack implements LayerListener, ValueListener {
    /* MAY CONTAIN DUPLICATES! */
    private ArrayList<Layer> layers;

    /* ListDataListeners are for the list model */
    private final Set<ListDataListener> listListeners = new HashSet<ListDataListener>();
    private final Set<LayerModelListener> modelListeners = new HashSet<LayerModelListener>();

    /* TODO: value listener for lambda */
    private double lambda;
    private FitValue stddev;

    /* in decibels */
    private FitValue prod;
    private FitValue sum;

    /* in radians */
    private FitValue offset;

    private LookupTable table;

    /** Bijection between layers and the number of actual layers.
     * However, it is stored in a map although it is a bijection, since
     * it should be used only to get the layer number for a layer. */
    private Map<Layer,Integer> getNumbering() {
        Map<Layer,Integer> numbering = new HashMap<Layer,Integer>();
        int id = 0;
        for(Layer l: layers) {
            if(!numbering.containsKey(l)) {
                numbering.put(l,id++);
            }
        }
        return Collections.unmodifiableMap(numbering);
    }

    /* Returns a copy of the list of layers. */
    public List<Layer> getLayers() {
        return new ArrayList<Layer>(layers);
    }
    /* Sets the new list of layers. This should be used
     * only for the most complex operations to change
     * the layer model. */
    public void setLayers(List<Layer> newLayers) {
        Set<Layer> oldSet = new HashSet<Layer>();
        Set<Layer> newSet = new HashSet<Layer>();
        int previous_size = layers.size();

        for(Layer l: layers)
            oldSet.add(l);
        for(Layer l: newLayers)
            newSet.add(l);

        for(Layer l: newLayers)
            if(!oldSet.contains(l))
                layerAdded(l);
        for(Layer l: layers)
            if(!newSet.contains(l))
                layerRemoved(l);

        this.layers = new ArrayList<Layer>(newLayers);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, previous_size-1));
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, newLayers.size()-1));
    }

    /* called when all references to l are removed from this stack */
    private void layerRemoved(Layer l) {
        l.removeLayerListener(this);
    }
    /* called when adding a reference to l to this stack when
     * there aren't previous references to l */
    private void layerAdded(Layer l) {
        l.addLayerListener(this);
    }

    public final ListModel listModel = new ListModel() {
        public void addListDataListener(ListDataListener l) {
            listListeners.add(l);
        }
        public void removeListDataListener(ListDataListener l) {
            listListeners.remove(l);
        }
        public int getSize() {
            return layers.size();
        }


        /* XXX: this is slow! Replace with a better (caching) algorithm if necessary */
        /* map from layers to integers in order to make references to
         * same layer distinguishable from identical different layers.
         *
         * The caching could be implemented in getNumbering.
         * */
        public Object getElementAt(int i) {
            Map<Layer,Integer> numbering = getNumbering();
            Layer l = layers.get(i);
            int j = numbering.get(l);
            return "Layer "+(j+1)+" pos " + (i+1) + ": " + l.toString();
        }
    };



    /** Constructor.
     *
     * <p>
     *
     * Creates a layer stack.
     *
     * @param lambda wavelength
     * @param table lookup table
     *
     */
    public LayerStack(double lambda, /*Material substrate,*/ LookupTable table) {
        this.layers = new ArrayList<Layer>();
        //this.substrate = substrate;
        this.lambda = lambda;
        this.table = table;
        this.prod = new FitValue(-100,0,100,false);
        this.sum = new FitValue(-200,-200,100,false);
        this.stddev = new FitValue(0,0,0.01*Math.PI/180,false,false);
        this.offset = new FitValue(-0.15*Math.PI/180,0,0.15*Math.PI/180,false);
        this.sum.addValueListener(this);
        this.prod.addValueListener(this);
        this.stddev.addValueListener(this);
        this.offset.addValueListener(this);
    }

    public void valueChanged(ValueEvent ev) {
        signalPropertyChange();
    }

    private void signalPropertyChange() {
        EventObject ev = new EventObject(this);
        for(LayerModelListener listener: modelListeners) {
            listener.modelPropertyChanged(ev);
            listener.simulationChanged(ev);
        }
    }
    private void signalStackChange(ListDataEvent ev) {
        for(LayerModelListener listener: modelListeners) {
            switch(ev.getType()) {
                case ListDataEvent.CONTENTS_CHANGED:
                    listener.layersChanged(ev);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    listener.layersAdded(ev);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    listener.layersRemoved(ev);
                    break;
            }
            listener.simulationChanged(ev);
        }
        for(ListDataListener listener: listListeners) {
            switch(ev.getType()) {
                case ListDataEvent.CONTENTS_CHANGED:
                    listener.contentsChanged(ev);
                    break;
                case ListDataEvent.INTERVAL_ADDED:
                    listener.intervalAdded(ev);
                    break;
                case ListDataEvent.INTERVAL_REMOVED:
                    listener.intervalRemoved(ev);
                    break;
            }
        }
    }
    /* there may be multiple instances of the same layer */
    /* XXX: should we send one CONTENTS_CHANGED message with the whole range? */
    public void layerPropertyChanged(LayerEvent ev) {
        for(int i=0; i<layers.size(); i++) {
            if(layers.get(i) == ev.layer) {
                ListDataEvent lev = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i, i);
                for(ListDataListener listener: listListeners)
                    listener.contentsChanged(lev);
            }
        }
        for(LayerModelListener listener: modelListeners)
            listener.simulationChanged(new EventObject(this));
    }

    /** Changes the wavelength.
     *
     * <p>
     *
     * The method tries to change the wavelength for this layer stack. If any
     * layer has an element which is not found in the lookup table for the new
     * wavelength, an exception is thrown and the original state is rolled back.
     *
     * @param lambda2 the new wavelength
     *
     * @throws ElementNotFound if the lookup table does not contain information
     * for an element for the given wavelength
     *
     */
    public void changeLambda(double lambda2) throws UnsupportedWavelength {
        LayerStack stack2 = this.deepCopy();
        stack2.lambda = lambda2;
        /* TODO: when susceptibility calculators and Matlab export are ready, code this */

        for(int i=0; i<stack2.getSize(); i++) {
            /* Here we use forcexyspace == 0. If we are able to calculate
             * susceptibility for forcexyspace == 0, we are able to
             * calculate it for forcexyspace != 0 */
            stack2.getElementAt(i).getSimpleMaterial(0).susc(lambda2);
        }

        this.deepCopyFrom(stack2);
        signalPropertyChange();
    }
    /** Instrument resolution.
     *
     * <p>
     *
     * The method returns the instrument resolution. Limited resolution
     * is modeled by a Gaussian function, the standard distribution of
     * which is returned by this method.
     *
     * @return standard deviation of a Gaussian distribution
     */
    public FitValue getStdDev() { return stddev; }

    public FitValue getOffset() { return offset; }

    /** Changes the instrument resolution.
     *
     * <p>
     *
     * The method changes the instrument resolution. Limited resolution
     * is modeled by a Gaussian function, the standard distribution of
     * which is returned by this method.
     *
     * @param stddev standard deviation of the new Gaussian distribution
     */
    public void setStdDev(FitValue stddev) {
        this.stddev.deepCopyFrom(stddev);
    }

    public void setOffset(FitValue offset) {
        this.offset.deepCopyFrom(offset);
    }

    /** Normalization factor.
     *
     * <p>
     *
     * The method returns the normalization factor the simulated curve is multiplied with.
     *
     * @return normalization factor
     */
    public FitValue getProd() {
        return prod;
    }

    /** Changes the normalization factor.
     *
     * <p>
     *
     * The method changes the normalization factor the simulated curve is multiplied with.
     *
     * @param prod new normalization factor
     */
    public void setProd(FitValue prod) {
        /*
        this.prod.removeValueListener(this);
        this.prod = prod;
        prod.addValueListener(this);
        signalPropertyChange();
        */
        this.prod.deepCopyFrom(prod);
    }

    /** Sum term.
     *
     * <p>
     *
     * The method returns the sum term added to the simulated curve
     *
     * @return sum term
     */
    public FitValue getSum() {
        return sum;
    }

    /** Changes the sum term.
     *
     * <p>
     *
     * The method changes the sum term added to the simulated curve
     *
     * @param sum new sum term
     */
    public void setSum(FitValue sum) {
        /*
        this.sum.removeValueListener(this);
        this.sum = sum;
        sum.addValueListener(this);
        signalPropertyChange();
        */
        this.sum.deepCopyFrom(sum);
    }

    /** Deep copy.
     *
     * <p>
     *
     * The method makes a deep copy of this object. Listeners are not copied (but the internal listener structure is).
     *
     * @return a deep copy of this layer stack
     */
    public LayerStack deepCopy() {
        LayerStack result = new LayerStack(this.lambda, /*substrate,*/ this.table);
        Map<Layer,Layer> copies = new HashMap<Layer,Layer>();
        /* we need to copy duplicate layers correctly */
        for(Layer l: layers) {
            if(!copies.containsKey(l)) {
                Layer l2 = l.deepCopy();
                layerAdded(l2);
                copies.put(l, l2);
            }
        }
        for(Layer l: layers) {
            result.layers.add(copies.get(l));
        }
        result.stddev.deepCopyFrom(this.stddev);
        result.offset.deepCopyFrom(this.offset);
        result.prod.deepCopyFrom(this.prod);
        result.sum.deepCopyFrom(this.sum);
        return result;
    }

    private boolean has_duplicates() {
        Set<Layer> set = new HashSet<Layer>();
        for(Layer l: layers) {
            if(set.contains(l))
                return true;
            set.add(l);
        }
        return false;
    }

    /** XML export, TODO: modify to handle duplicate layers. */
    public Element export(Document doc) {
        Element model = doc.createElement("model");
        Element layers = doc.createElement("layers");
        //Element substrate = doc.createElement("substrate");
        Element sumProp = doc.createElement("sum");
        Element prodProp = doc.createElement("prod");
        Element stdProp = doc.createElement("stddev");
        Element offProp = doc.createElement("offset");

        model.appendChild(layers);
        //model.appendChild(substrate);
        //substrate.appendChild(this.substrate.export(doc));

        if(!has_duplicates()) {
            for(Layer l: this.layers)
                layers.appendChild(l.export(doc));
        } else {
            int free_id = 0;
            Element order = doc.createElement("order");
            Map<Layer,String> ids = new HashMap<Layer,String>();
            for(Layer l: this.layers) {
                if(!ids.containsKey(l)) {
                    ids.put(l, "layer"+(free_id++));
                }
            }
            for(Layer l: ids.keySet()) {
                Element layerElement = l.export(doc);
                layerElement.setAttribute("id",ids.get(l));
                layers.appendChild(layerElement);
            }
            for(Layer l: this.layers) {
                Element layerRef = doc.createElement("layerref");
                layerRef.setAttribute("layerid",ids.get(l));
                order.appendChild(layerRef);
            }
            model.appendChild(order);
        }

        model.setAttribute("lambda",""+lambda);
        //model.setAttribute("stddev",""+stddev);
        sumProp.appendChild(sum.export(doc));
        prodProp.appendChild(prod.export(doc));
        stdProp.appendChild(stddev.export(doc));
        offProp.appendChild(offset.export(doc));
        model.appendChild(sumProp);
        model.appendChild(prodProp);
        model.appendChild(stdProp);
        model.appendChild(offProp);
        return model;
    }

    public LayerStack(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
        Node layersNode = XMLUtil.getNamedChildElements(n,"layers").get(0);
        //Node substrateNode = XMLUtil.getNamedChildElements(n,"substrate").get(0);
        Node sumNode = XMLUtil.getNamedChildElements(n,"sum").get(0);
        Node prodNode = XMLUtil.getNamedChildElements(n,"prod").get(0);
        Node stdNode = null; /* not present in old files */
        Node offNode = null; /* not present in old files */
        List<Node> stdElements = XMLUtil.getNamedChildElements(n,"stddev");
        List<Node> offElements = XMLUtil.getNamedChildElements(n,"offset");
        if(!stdElements.isEmpty())
            stdNode = stdElements.get(0);
        if(!offElements.isEmpty())
            offNode = offElements.get(0);
        //Node substrateMat = XMLUtil.getChildElements(substrateNode).get(0);
        this.table = table;
        this.lambda = Double.parseDouble(n.getAttributes().getNamedItem("lambda").getNodeValue());
        //this.stddev = Double.parseDouble(n.getAttributes().getNamedItem("stddev").getNodeValue());
        if(stdNode != null)
            this.stddev = new FitValue(XMLUtil.getNamedChildElements(stdNode,"fitvalue").get(0),false);
        else
            this.stddev = new FitValue(0,0,0,false,false);

        if(offNode != null)
            this.offset = new FitValue(XMLUtil.getNamedChildElements(offNode,"fitvalue").get(0));
        else
            this.offset = new FitValue(-0.15*Math.PI/180,0,0.15*Math.PI/180,false);

        this.sum = new FitValue(XMLUtil.getNamedChildElements(sumNode,"fitvalue").get(0));
        this.prod = new FitValue(XMLUtil.getNamedChildElements(prodNode,"fitvalue").get(0));
        this.sum.addValueListener(this);
        this.prod.addValueListener(this);
        this.stddev.addValueListener(this);
        this.offset.addValueListener(this);
        //this.substrate = MaterialImportDispatcher.doImport(substrateMat,table);
        this.layers = new ArrayList<Layer>();

        Map<String,Layer> layersById = new HashMap<String,Layer>();
        List<Layer> order = new ArrayList<Layer>();

        for(Node layerNode: XMLUtil.getNamedChildElements(layersNode,"layer")) {
            Layer l = new Layer(layerNode, table);
            if(layerNode.getAttributes().getNamedItem("id") != null) {
                String id = layerNode.getAttributes().getNamedItem("id").getNodeValue();
                layersById.put(id, l);
            }
            order.add(l);
        }
        if(!XMLUtil.getNamedChildElements(n,"order").isEmpty()) {
            Node orderNode = XMLUtil.getNamedChildElements(n,"order").get(0);
            order = new ArrayList<Layer>(); /* don't use the default order since the order is explicitly specified */
            for(Node refNode: XMLUtil.getNamedChildElements(orderNode,"layerref")) {
                String id = refNode.getAttributes().getNamedItem("layerid").getNodeValue();
                Layer l = layersById.get(id);
                order.add(l);
            }
        }
        Set<Layer> added = new HashSet<Layer>();
        for(Layer l: order) {
            this.layers.add(l);
            if(!added.contains(l)) {
                added.add(l);
                layerAdded(l);
            }
        }
    }

    /** XML import */

    /*
    public static LayerStack structImport(Object o, LookupTable table) // throws InvalidStructException, ElementNotFound
    {
        Map<?,?> m;
        Object layersO;
        Object obj;
        ArrayList<?> layersL;
        final double Cu_K_alpha = 1.5405600e-10;
        double lambda = Cu_K_alpha;

        if(!(o instanceof Map))
            throw new InvalidStructException();
        m = (Map<?,?>)o;
        obj = m.get("lambda");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            lambda = (Double)obj;
        }


        LayerStack temp = new LayerStack(lambda, table);

        obj = m.get("stddev");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setStdDev((Double)obj);
        }

        obj = m.get("measNormal");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setMeasNormal((Double)obj);
        }

        obj = m.get("measSum");
        if(obj != null) {
            if(!(obj instanceof Double))
                throw new InvalidStructException();
            temp.setMeasSum((Double)obj);
        }


        layersO = m.get("layers");
        if(layersO == null || !(layersO instanceof ArrayList))
            throw new InvalidStructException();
        layersL = (ArrayList<?>)layersO;
        for(Object o2: layersL) {
            temp.layers.add(Layer.structImport(o2, table, lambda));
        }
        return temp;
    }
    */

    /** Deep copy from an object.
     *
     * <p>
     *
     * The method makes a deep copy of the given object to this object. The listeners of this
     * object are not changed in any way.
     *
     * @param s2 the layer stack to make the deep copy from
     */
    public void deepCopyFrom(LayerStack s2) {
        LayerStack temp = s2.deepCopy();

        if(getSize() > 0)
            signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, 0, getSize()-1));
        this.layers = temp.layers;

        /* there may be multiple references to the same layer */
        Set<Layer> processed = new HashSet<Layer>();
        for(Layer l: this.layers) {
            if(!processed.contains(l)) {
                processed.add(l);
                temp.layerRemoved(l);
                this.layerAdded(l);
            }
        }
        this.lambda = temp.lambda;

        /* Disable event temporarily in order not to flood listeners of this
         * LayerStack with events. Other listeners that have directly attached to
         * FitValues are sent events, but that's just what we want. */

        this.prod.removeValueListener(this);
        this.sum.removeValueListener(this);
        this.stddev.removeValueListener(this);
        this.offset.removeValueListener(this);
        this.prod.deepCopyFrom(temp.prod);
        this.sum.deepCopyFrom(temp.sum);
        this.stddev.deepCopyFrom(temp.stddev);
        this.offset.deepCopyFrom(temp.offset);
        this.prod.addValueListener(this);
        this.sum.addValueListener(this);
        this.stddev.addValueListener(this);
        this.offset.addValueListener(this);

        this.table = temp.table;
        signalPropertyChange();
        if(getSize() > 0)
            signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, 0, getSize()-1));
    }

    /** Adds a listener. */
    public void addLayerModelListener(LayerModelListener l) {
        this.modelListeners.add(l);
    }
    /** Removes a listener. */
    public void removeLayerModelListener(LayerModelListener l) {
        this.modelListeners.remove(l);
    }

    /** Adds a layer.
     *
     * <p>
     *
     * The wavelength and lookup table of the layer are set to the properties
     * of this LayerStack. If the new lookup table does not contain all the
     * necessary elements for the new wavelength, an exception is thrown and
     * nothing is changed.
     *
     * <p>
     *
     * After a layer is added to a layer stack, the layer stack should be
     * considered the owner of the layer. See getElementAt for the rules
     * for using layers owned by a layer stack.
     *
     * @param l the layer to add
     *
     * @throws ElementNotFound an element was not found in the lookup table
     *
     * @see #getElementAt
     */

    public void add(Layer l) /*throws ElementNotFound*/ {
        add(l, 0);
    }
    /** Returns the wavelength */
    public double getLambda() {
        return lambda;
    }
    /** Returns the element lookup table */
    public LookupTable getTable() {
        return table;
    }
    /** Inserts a layer to the given position.
     *
     * <p>
     *
     * The wavelength and lookup table of the layer are set
     * to the properties of this LayerStack. If the new lookup
     * table does not contain all the necessary elements for the
     * new wavelength, an exception is thrown and nothing is changed.
     *
     * <p>
     *
     * After a layer is added to a layer stack, the layer stack should be
     * considered the owner of the layer. See getElementAt for the rules
     * for using layers owned by a layer stack.
     *
     * @param l the layer to add
     * @param i the position to insert the layer to
     *
     * @throws ElementNotFound an element was not found in the lookup table
     *
     * @see #getElementAt
     */
    public void add(Layer l, int i) /*throws ElementNotFound*/ {
        boolean multiple_refs = false;
        if(l == null)
            throw new NullPointerException();

        for(Layer l2: this.layers) {
            if(l2 == l)
                multiple_refs = true;
        }
        //l.updateWlData(this.table, this.lambda);
        this.layers.add(i, l);
        if(!multiple_refs)
            layerAdded(l);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i, i));
    }
    /** Removes a layer from this stack.
     *
     * @param i the index of the layer to remove
     */
    public void remove(int i) {
        Layer l = this.layers.remove(i);
        boolean multiple_refs = false;

        for(Layer l2: this.layers) {
            if(l2 == l)
                multiple_refs = true;
        }
        if(!multiple_refs)
            layerRemoved(l);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, i, i));
    }



    /* TODO: implement functions for moving multiple layers */
    /** Moves a layer up.
     *
     * @param i the index of the layer to move
     *
     * @throws IndexOutOfBoundsException the layer at the given index was not found or is the uppermost layer
     */
    public void moveUp(int i) {
        if(i <= 0)
            throw new IndexOutOfBoundsException();
        layers.add(i-1,layers.remove(i));
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i-1, i));
    }

    /* move the range [low,high[ up */
    public void moveUp(int low, int high) {
        if(low <= 0 || low >= high || high > getSize())
            throw new IndexOutOfBoundsException();
        for(int i=low; i<high; i++) {
            layers.add(i-1,layers.remove(i));
        }
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, low-1, high-1));
    }

    /* move the range [low,high[ down */
    public void moveDown(int low, int high) {
        if(low < 0 || low >= high || high >= getSize())
            throw new IndexOutOfBoundsException();
        for(int i=high-1; i>=low; i--) {
            layers.add(i+1,layers.remove(i));
        }
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, low, high));
    }

    /** Moves a layer down.
     *
     * @param i the index of the layer to move
     *
     * @throws IndexOutOfBoundsException the layer at the given index was not found or is the lowermost layer
     */
    public void moveDown(int i) {
        if(i >= getSize()-1)
            throw new IndexOutOfBoundsException();
        layers.add(i+1,layers.remove(i));
        signalStackChange(new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, i, i+1));
    }

    /** Signals a change of the layer stack to all listeners.
     *
     * <p>
     *
     * This method must be called after a layer is modified.
     *
     * @param o the sending object of the event. It can be null to set it to this LayerStack.
     *
     */
    /*
    public void invalidate(Object o) {
        // XXX: the whole method is an ugly hack.
        if(o == null)
            o = this;
        for(ListDataListener listener : this.listeners) {
            if(getSize() > 0)
                listener.contentsChanged(new ListDataEvent(o, ListDataEvent.CONTENTS_CHANGED, 0, getSize()-1));
        }
    }
    */
    /** Returns the number of layers in this stack. */
    public int getSize() {
        return this.layers.size();
    }

    /** Gets a layer at the specified position.
     *
     * <p>
     *
     * The layer returned by this method is owned by this LayerStack object. It may
     * not be added to another LayerStack. The updateWlData method must not be called.
     * After the layer is modified, the invalidate method of this LayerStack must be called.
     * 
     * @return the layer at the specified position
     *
     * @param i position of the layer
     *
     * @throws IndexOutOfBoundsException if the layer was not found
     *
     * @see #invalidate
     */
    public Layer getElementAt(int i) {
        return this.layers.get(i);
    }
    /** Returns a string representation of this layer stack.
     *
     * @return string representations of individual layers separated by newlines.
     */
    public String toString() {
        String result = "Layer model\n";
        int size = getSize();
        for(int i=0; i<size; i++) {
            result += getElementAt(i).toString() + "\n";
        }
        //result += "substrate: "+substrate.toString()+"\n";
        return result;
    }



    private static final double UNIT_TEST_TOL = 1e-5;
    public void unitTestCase(Oct oct, double[] theta) throws SimulationException, OctException
    {
        double[] result1, result2;

        result1 = octXRDCurve(oct, theta);
        result2 = xrdCurve(theta);
        assert(result1.length == theta.length);
        assert(result2.length == theta.length);
        for(int i=0; i<theta.length; i++) {
            double avg = (result1[i] + result2[i])/2;
            double diff = Math.abs(result1[i] - result2[i]);
            assert(diff <= UNIT_TEST_TOL * avg);
        }
    }

    /* TODO: rewrite */
    public double[] octXRDCurve(Oct oct, double[] theta) throws SimulationException, OctException {
        oct.putRowVector("theta",theta);
        oct.putScalar("lambda",lambda);
        oct.putScalar("stddevrad",stddev.getExpected());
        oct.putScalar("offset",offset.getExpected());
        oct.execute("suscdata = zeros("+layers.size()+",3)");
        oct.execute("zspace = zeros("+layers.size()+",1)");
        oct.execute("prod = "+getProd().getExpected());
        oct.execute("sum = "+getSum().getExpected());
        oct.execute("thetaoffset = "+getOffset().getExpected());
        oct.execute("prod = 10 ^ (prod / 10)");
        oct.execute("sum = 10 ^ (sum / 10)");
        oct.execute("d = zeros("+layers.size()+",1)");
        oct.putScalar("basexyspace",0); /* dummy */

        for(int i=layers.size()-1; i>=0; i--) {
            Layer layer = layers.get(i);
            oct.execute("material1 = "+layer.getMat1().flatten().octaveRepr(lambda));
            oct.execute("material2 = "+layer.getMat2().flatten().octaveRepr(lambda));
            oct.execute("mixture1 = {1.0, material1}");
            oct.execute("mixture2 = {1.0, material2}");
            oct.execute("mixture.first = mixture1");
            oct.execute("mixture.second = mixture2");
            oct.execute("mixture.x = "+layer.getComposition().getExpected());
            if(i==layers.size()-1)
                oct.putScalar("rel",1);
            else
                oct.putScalar("rel",layer.getRelaxation().getExpected());
            oct.putScalar("k",i+1);
            oct.execute("d(k) = "+layer.getThickness().getExpected());
            oct.execute("[suscdata(k,:),basexyspace,zspace(k)] = matsusc_relaxation(mixture, basexyspace, rel)");
        }

        /*
        oct.execute("material1 = "+substrate.flatten().octaveRepr(lambda));
        oct.execute("material2 = "+substrate.flatten().octaveRepr(lambda));
        oct.execute("mixture1 = {1.0, material1}");
        oct.execute("mixture2 = {1.0, material2}");
        oct.execute("mixture.first = mixture1");
        oct.execute("mixture.second = mixture2");
        oct.execute("mixture.x = 0");
        */

        //oct.putScalar("k",layers.size()+1);
        //oct.execute("d(k) = 0");
        //oct.execute("[suscdata(k,:),xyspace(k),zspace(k)] = matsusc(mixture, 0)");
        // suscdata, zspace, d
        oct.execute("meas = XRDCurve(lambda, d, zspace, suscdata, theta, stddevrad, thetaoffset) * prod + sum");
        return oct.getMatrix("meas")[0];
    }

    public double[] xrdCurve(double[] theta) throws SimulationException {
        int nlayers = layers.size();
        double[] result = new double[theta.length];
        Complex[] X = new Complex[theta.length];
        double[] sin_theta_B_ar = new double[nlayers];
        double[] xyspace = new double[nlayers];
        double[] d_ar = new double[nlayers];
        Complex[] chi_0_ar = new Complex[nlayers];
        Complex[] chi_h_ar = new Complex[nlayers];
        Complex[] chi_h_neg_ar = new Complex[nlayers];
        final double stddevs = 4;

        // XXX: can throw if theta.length == 0 or theta.length == 1
        double dtheta = (theta[theta.length-1] - theta[0])/(theta.length-1);

        double thetaoffset = offset.getExpected();
        double[] filter = null;
        /* substrate */

        if(nlayers == 0)
            throw new SimulationException("No layers");

        filter = DataTools.gaussianFilter(dtheta, stddev.getExpected(), stddevs);
        if(filter != null) {
            if(!DataTools.isUniformlySpaced(theta)) {
                filter = null;
            }
        }

        /*
        Susceptibilities substrateSusc;
        SimpleMaterial substrateMat;
        */

        //double zspace_s;

        /* calculate in-plane lattice constants */
        xyspace[nlayers-1] = layers.get(nlayers-1).calcXYSpace(0);
        for(int i=nlayers-2; i>=0; i--) {
            xyspace[i] = layers.get(i).calcXYSpace(xyspace[i+1]);
        }

        for(int i=0; i<nlayers; i++) {
            Layer layer = layers.get(i);
            SimpleMaterial sm = layer.getSimpleMaterial(xyspace[i]);
            Susceptibilities susc = sm.susc(lambda);

            chi_0_ar[i] = susc.chi_0;
            chi_h_ar[i] = susc.chi_h;
            chi_h_neg_ar[i] = susc.chi_h_neg;
            sin_theta_B_ar[i] = lambda/(2*sm.getZSpace());

            d_ar[i] = layer.getThickness().getExpected();
        }
        /*
        substrateMat = substrate.flatten();
        substrateSusc = substrateMat.susc(lambda);
        zspace_s = substrateMat.getZSpace();
        theta_Bs = Math.asin(lambda/(2*zspace_s));
        */

        // we approximate that C = |cos(2*theta)| =~ |cos(2*theta_Bs)|
        double sin_theta_Bs = sin_theta_B_ar[nlayers-1];

        for(int i=0; i<result.length; i++) {
            result[i] = 0;
        }

        boolean[] spols = {false,true};
        for(boolean spol: spols) {
            Complex chi_0_s = chi_0_ar[nlayers-1];
            Complex chi_h_s = chi_h_ar[nlayers-1];
            Complex chi_h_neg_s = chi_h_neg_ar[nlayers-1];

            assert(!Double.isNaN(chi_0_s.real) && !Double.isNaN(chi_0_s.imag));
            assert(!Double.isNaN(chi_h_s.real) && !Double.isNaN(chi_h_s.imag));
            assert(!Double.isNaN(chi_h_neg_s.real) && !Double.isNaN(chi_h_neg_s.imag));

            double theta_Bs = Math.asin(sin_theta_Bs);
            double gamma0_s = Math.sin(theta_Bs - thetaoffset);
            double gammah_s = -Math.sin(theta_Bs + thetaoffset);
            double b_s = gamma0_s/gammah_s;
            double C_s = spol ? 1 : Math.abs(Math.cos(2*theta_Bs));

            for(int i=0; i<theta.length; i++) {
                double alphah = -4*(Math.sin(theta[i] + thetaoffset) - sin_theta_Bs)*sin_theta_Bs;


                Complex eta = Complex.div(Complex.add(b_s*alphah,Complex.mul(2,chi_0_s)),
                    Complex.mul(2*C_s*Math.sqrt(Math.abs(b_s)),Complex.sqrt(Complex.mul(chi_h_s, chi_h_neg_s))));

                X[i] = Complex.sub(eta, Complex.mul(Math.signum(eta.real), Complex.sqrt(Complex.sub(Complex.mul(eta,eta),1))));
            }


            for(int j=nlayers-2; j>=0; j--) {
                Complex chi_0 = chi_0_ar[j];
                Complex chi_h = chi_h_ar[j];
                Complex chi_h_neg = chi_h_neg_ar[j];
                double sin_theta_B = sin_theta_B_ar[j];
                double d = d_ar[j];
                double theta_B = Math.asin(sin_theta_B);
                double gamma0 = Math.sin(theta_B - thetaoffset);
                double gammah = -Math.sin(theta_B + thetaoffset);
                double b = gamma0/gammah;
                double C = spol ? 1 : Math.abs(Math.cos(2*theta_B));
                //assert(!Double.isNaN(d) && !Double.isNaN(sin_theta_B));
                for(int i=0; i<theta.length; i++) {
                    double alphah = -4*(Math.sin(theta[i] + thetaoffset) - sin_theta_B)*sin_theta_B;
                    //assert(!Double.isNaN(alphah));
                    Complex eta = Complex.div(Complex.add(b*alphah,Complex.mul(2,chi_0)),
                        Complex.mul(2*C*Math.sqrt(Math.abs(b)),Complex.sqrt(Complex.mul(chi_h, chi_h_neg))));
                    Complex sqrteta2m1 = Complex.sqrt(Complex.sub(Complex.mul(eta,eta),1));
                    Complex T = Complex.mul((Math.PI*C*d)/(lambda*
                                // for small thetaoffset, we could optimize by using Math.abs(sin_theta_B)
                                // it is actually sqrt(sin(theta_B+thetaoffset)*sin(theta_B-thetaoffset)) = sqrt(abs(gamma0*gammah))
                                // but the difference is negligible for small thetaoffset
                                //Math.abs(sin_theta_B)),
                                Math.sqrt(Math.abs(gamma0*gammah))),
                                             Complex.sqrt(Complex.mul(chi_h,chi_h_neg)));

                    /*
                    assert(!Double.isNaN(T.real));
                    assert(!Double.isNaN(T.imag));
                    */
                    Complex S1 = Complex.mul(Complex.add(Complex.sub(X[i],eta),sqrteta2m1),
                                   Complex.exp(Complex.mul(Complex.mul(new Complex(0,-1),T),sqrteta2m1)));
                    Complex S2 = Complex.mul(Complex.sub(Complex.sub(X[i],eta),sqrteta2m1),
                                   Complex.exp(Complex.mul(Complex.mul(new Complex(0,1),T),sqrteta2m1)));
                    Complex S1S2 = Complex.div(Complex.add(S1,S2),Complex.sub(S1,S2));
                    /*
                    assert(!Double.isNaN(S1.real));
                    assert(!Double.isNaN(S1.imag));
                    assert(!Double.isNaN(S2.real));
                    assert(!Double.isNaN(S2.imag));
                    assert(!Double.isNaN(S1S2.real));
                    assert(!Double.isNaN(S1S2.imag));
                    */
                    X[i] = Complex.add(eta,Complex.mul(sqrteta2m1, S1S2));
                    /*
                    assert(!Double.isNaN(X[i].real));
                    assert(!Double.isNaN(X[i].imag));
                    */
                }
            }
            for(int i=0; i<X.length; i++) {
                double Xabs = Complex.abs(X[i]);
                //assert(!Double.isNaN(Xabs));
                result[i] += Xabs*Xabs/2;
                if(Double.isNaN(result[i]))
                    result[i] = 0;
            }
        }
        if(filter != null)
            result = DataTools.applyOddFilter(filter, result);
        double prod = getProd().getExpected();
        double sum = getSum().getExpected();

        /* both are in decibels */
        prod = Math.exp(prod * Math.log(10)/10);
        sum = Math.exp(sum * Math.log(10)/10);

        for(int i=0; i<result.length; i++) {
            result[i] = result[i] * prod + sum;
        }
        return result;
        /* TODO: stddev */
    }
}
