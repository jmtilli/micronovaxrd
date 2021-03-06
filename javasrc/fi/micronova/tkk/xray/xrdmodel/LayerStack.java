package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.xrdde.*;
import fi.micronova.tkk.xray.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import fi.iki.jmtilli.javafastcomplex.Complex;
import fi.iki.jmtilli.javafastcomplex.ComplexBuffer;
import fi.iki.jmtilli.javafastcomplex.ComplexBufferArray;
import fi.iki.jmtilli.javafastcomplex.ComplexNumber;
import fi.iki.jmtilli.javafastcomplex.ComplexUtils;
import fi.iki.jmtilli.javaxmlfrag.*;


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

public class LayerStack implements LayerListener, ValueListener, XMLRowable {
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

    public double[] getFitValuesForFitting(FitValue.FitValueType type)
    {
      double[] result = new double[3+4*layers.size()];
      result[0] = prod.getValueForFitting(type);
      result[1] = sum.getValueForFitting(type);
      result[2] = offset.getValueForFitting(type);
      for (int i = 0; i < layers.size(); i++)
      {
        Layer l = layers.get(i);
        result[3+0*layers.size()+i] =
          l.getThickness().getValueForFitting(type);
        result[3+1*layers.size()+i] =
          l.getComposition().getValueForFitting(type);
        result[3+2*layers.size()+i] =
          l.getRelaxation().getValueForFitting(type);
        result[3+3*layers.size()+i] =
          l.getSuscFactor().getValueForFitting(type);
      }
      return result;
    }
    public void setFitValues(double[] values)
    {
      if (values.length != 3+4*layers.size())
      {
        throw new IllegalArgumentException();
      }
      this.prod.setExpected(values[0]);
      this.sum.setExpected(values[1]);
      this.offset.setExpected(values[2]);
      /*
         If there are duplicate layers, the last value takes precedence.
       */
      for (int i = 0; i < layers.size(); i++)
      {
        Layer l = layers.get(i);
        l.getThickness().setExpected(values[3+0*layers.size()+i]);
        l.getComposition().setExpected(values[3+1*layers.size()+i]);
        l.getRelaxation().setExpected(values[3+2*layers.size()+i]);
        l.getSuscFactor().setExpected(values[3+3*layers.size()+i]);
      }
    }

    public boolean equals(Object o)
    {
      LayerStack that;
      if (this == o)
      {
        return true;
      }
      if (o == null || !(o instanceof LayerStack))
      {
        return false;
      }
      that = (LayerStack)o;
      // compare wavelengths and table references
      if (   this.lambda != that.lambda
          || this.table != that.table)
      {
        return false;
      }

      // compare fit values
      if (   !this.stddev.equals(that.stddev)
          || !this.prod.equals(that.prod)
          || !this.sum.equals(that.sum)
          || !this.offset.equals(that.offset))
      {
        return false;
      }

      // compare layers: size, numbering, contents
      if (this.layers.size() != that.layers.size())
      {
        return false;
      }
      Map<FitValue,Integer> this_numbering = this.getFitValueNumbering();
      Map<FitValue,Integer> that_numbering = that.getFitValueNumbering();
      for (int i = 0; i < this.layers.size(); i++)
      {
        Layer this_layer = this.layers.get(i);
        Layer that_layer = that.layers.get(i);
        if (!Layer.layerDeepEquals(this_layer, this_numbering,
                                   that_layer, that_numbering))
        {
          return false;
        }
      }
      return true;
    }

    public int getFittedValueCount() {
        Map<FitValue,Integer> counts = new HashMap<FitValue,Integer>();
        int id = 0;
        FitValue[] vals = {getOffset(), getProd(), getSum()};
        for (FitValue val: vals)
        {
            if (val.getEnabled() && val.getMin() < val.getMax())
            {
                if (counts.containsKey(val))
                {
                    counts.put(val, counts.get(val)+1);
                }
                else
                {
                    counts.put(val, 1);
                }
            }
        }
        for (Layer l: layers)
        {
            vals = new FitValue[]{l.getThickness(), l.getComposition(),
                                  l.getRelaxation(), l.getSuscFactor()};
            for (FitValue val: vals)
            {
                if (val.getEnabled() && val.getMin() < val.getMax())
                {
                    if (counts.containsKey(val))
                    {
                        counts.put(val, counts.get(val)+1);
                    }
                    else
                    {
                        counts.put(val, 1);
                    }
                }
            }
        }
        return counts.size();
    }



    /**
       Return link numbers for FitValues that are linked.

       @return Link numbers for FitValues that are linked.
     */
    private Map<FitValue,Integer> getFitValueNumbering() {
        Map<FitValue,Integer> counts = new HashMap<FitValue,Integer>();
        Map<FitValue,Integer> links = new HashMap<FitValue,Integer>();
        int id = 0;
        for (Layer l: layers)
        {
            FitValue[] vals = {l.getThickness(), l.getComposition(),
                               l.getRelaxation(), l.getSuscFactor()};
            for (FitValue val: vals)
            {
                if (counts.containsKey(val))
                {
                    counts.put(val, counts.get(val)+1);
                }
                else
                {
                    counts.put(val, 1);
                }
            }
        }
        for (Layer l: layers)
        {
            FitValue[] vals = {l.getThickness(), l.getComposition(),
                               l.getRelaxation(), l.getSuscFactor()};
            for (FitValue val: vals)
            {
                if (counts.get(val) > 1 && !links.containsKey(val))
                {
                    links.put(val, ++id);
                }
            }
        }
        return links;
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

    public final ListModel<String> listModel = new ListModel<String>() {
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
         * The caching could be implemented in getFitValueNumbering.
         * */
        public String getElementAt(int i) {
            Map<FitValue,Integer> fitValueNumbering = getFitValueNumbering();
            Layer l = layers.get(i);
            return l.toString(fitValueNumbering);
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

    public class Pair {
        public final LayerStack stack;
        public final FitValue value;
        public Pair(LayerStack stack, FitValue value)
        {
            this.stack = stack;
            this.value = value;
        }
    };
    public Pair deepCopy(FitValue val) {
        LayerStack result = new LayerStack(this.lambda, this.table);
        FitValue val2 = null;
        Map<FitValue, Integer> fitValueNumbering = getFitValueNumbering();
        Map<Integer, FitValue> newFitValues = new HashMap<Integer, FitValue>();
        int size = layers.size();
        for(int i=0; i<size; i++)
        {
            Layer l = layers.get(i);
            Layer l2 = l.deepCopy(fitValueNumbering, newFitValues);
            if (l.getThickness() == val)
            {
                val2 = l2.getThickness();
            }
            if (l.getComposition() == val)
            {
                val2 = l2.getComposition();
            }
            if (l.getRelaxation() == val)
            {
                val2 = l2.getRelaxation();
            }
            if (l.getSuscFactor() == val)
            {
                val2 = l2.getSuscFactor();
            }
            result.layers.add(l2);
            result.layerAdded(l2);
        }
        result.stddev.deepCopyFrom(this.stddev);
        result.offset.deepCopyFrom(this.offset);
        result.prod.deepCopyFrom(this.prod);
        result.sum.deepCopyFrom(this.sum);
        if (this.stddev == val)
        {
            val2 = result.stddev; 
        }
        if (this.prod == val)
        {
            val2 = result.prod; 
        }
        if (this.offset == val)
        {
            val2 = result.offset; 
        }
        if (this.sum == val)
        {
            val2 = result.sum; 
        }
        return new Pair(result, val2);
    }

    public void fittingErrorScan(final FittingErrorFunc func,
                                 final FitValue val, final GraphData gd,
                                 final double[] mids,
                                 final double[] errs)
    {
        final LayerStack ls = this;
        double min = val.getMin(), max = val.getMax();
        int cpus = Runtime.getRuntime().availableProcessors();
        ExecutorService exec =
            new ThreadPoolExecutor(cpus, cpus, 1, TimeUnit.SECONDS,
                                   new LinkedBlockingQueue<Runnable>());
        try
        {
            if (mids.length != errs.length)
            {
                throw new IllegalArgumentException();
            }
            ArrayList<Callable<Void>> list = new ArrayList<Callable<Void>>();
            for (int i = 0; i < mids.length; i++)
            {
                final int finalI = i;
                final double mid = min + (max-min)/(mids.length-1) * i;
                mids[i] = mid;
                list.add(new Callable<Void>() {
                    public Void call() throws Exception
                    {
                        LayerStack.Pair pair = ls.deepCopy(val);
                        LayerStack ls = pair.stack;
                        FitValue val = pair.value;
                        val.setExpected(mid);
                        GraphData gd2 = gd.simulate(ls);
                        double err = func.getError(gd2.meas, gd2.simul);
                        errs[finalI] = err;
                        return null;
                    }
                });
            }
            for (;;)
            {
                try
                {
                    for (Future f: exec.invokeAll(list))
                    {
                      try
                      {
                          f.get();
                      }
                      catch(ExecutionException e)
                      {
                        // XXX: what to do?
                        throw new RuntimeException(e);
                      }
                      catch(CancellationException e)
                      {
                        // XXX: what to do?
                        throw new RuntimeException(e);
                      }
                    }
                    return;
                }
                catch (InterruptedException ex)
                {
                }
            }
        }
        finally
        {
            exec.shutdown();
        }
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
        Map<FitValue, Integer> fitValueNumbering = getFitValueNumbering();
        Map<Integer, FitValue> newFitValues = new HashMap<Integer, FitValue>();
        for(Layer l: layers) {
            Layer l2 = l.deepCopy(fitValueNumbering, newFitValues);
            result.layers.add(l2);
            result.layerAdded(l2);
        }
        result.stddev.deepCopyFrom(this.stddev);
        result.offset.deepCopyFrom(this.offset);
        result.prod.deepCopyFrom(this.prod);
        result.sum.deepCopyFrom(this.sum);
        return result;
    }

    public void toXMLRow(DocumentFragment f)
    {
        DocumentFragment fl;
        Map<FitValue, Integer> fitValueNumbering = getFitValueNumbering();
        Set<FitValue> alreadyAdded = new HashSet<FitValue>();
        f.setAttrDouble("lambda", lambda);
        f.set("sum").setRow("fitvalue", sum);
        f.set("prod").setRow("fitvalue", prod);
        f.set("stddev").setRow("fitvalue", stddev);
        f.set("offset").setRow("fitvalue", offset);
        fl = f.set("layers");

        for(Layer l: this.layers)
            fl.add("layer").setThisRow(l.xmlRowable(fitValueNumbering, alreadyAdded));
    }

    public LayerStack(DocumentFragment f, LookupTable table)
      throws ElementNotFound, InvalidMixtureException
    {
        DocumentFragment layersNode = f.getNotNull("layers");
        this.sum = new FitValue(f.getNotNull("sum").getNotNull("fitvalue"));
        this.prod = new FitValue(f.getNotNull("prod").getNotNull("fitvalue"));
        if (f.get("offset") != null)
        {
            this.offset = new FitValue(f.getNotNull("offset").getNotNull("fitvalue"));
        }
        else
        {
            this.offset = new FitValue(-0.15*Math.PI/180,0,0.15*Math.PI/180,false);
        }
        if (f.get("stddev") != null)
        {
            this.stddev = new FitValue(f.getNotNull("stddev").getNotNull("fitvalue"),
                                       false);
        }
        else
        {
            this.stddev = new FitValue(0,0,0,false,false);
        }
        this.table = table;
        this.lambda = f.getAttrDoubleNotNull("lambda");

        this.sum.addValueListener(this);
        this.prod.addValueListener(this);
        this.stddev.addValueListener(this);
        this.offset.addValueListener(this);
        this.layers = new ArrayList<Layer>();

        Map<String,Layer> layersById = new HashMap<String,Layer>();
        List<Layer> order = new ArrayList<Layer>();
        Map<String, FitValue> fitValueById = new HashMap<String, FitValue>();

        for(DocumentFragment layerNode: layersNode.getMulti("layer")) {
            Layer l = new Layer(layerNode, table, fitValueById);
            if(layerNode.getAttrStringObject("id") != null) {
                String id = layerNode.getAttrStringNotNull("id");
                layersById.put(id, l);
            }
            order.add(l);
        }
        /*
           Support for reading old file format: in the old format, only linkin
           all parameters at once was supported.
         */
        if (f.get("order") != null) {
            DocumentFragment orderNode = f.getNotNull("order");
            order = new ArrayList<Layer>(); /* don't use the default order since the order is explicitly specified */
            for(DocumentFragment refNode: orderNode.getMulti("layerref")) {
                String id = refNode.getAttrStringNotNull("layerid");
                Layer l = layersById.get(id);
                order.add(l);
            }
        }
        for(Layer l: order) {
            Layer l2 = l.getAllLinked();
            this.layers.add(l2);
            layerAdded(l2);
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

        Set<Layer> processed = new HashSet<Layer>();
        for(Layer l: this.layers) {
            temp.layerRemoved(l);
            this.layerAdded(l);
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
        if(l == null)
            throw new NullPointerException();

        //l.updateWlData(this.table, this.lambda);
        this.layers.add(i, l);
        layerAdded(l);
        signalStackChange(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, i, i));
    }
    /** Removes a layer from this stack.
     *
     * @param i the index of the layer to remove
     */
    public void remove(int i) {
        Layer l = this.layers.remove(i);
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


    private static final double SUSCFACTOR_MIN = 1e-7;

    public double[] xrdCurveFaster(double[] theta) throws SimulationException {

        int nlayers = layers.size();
        double[] result = new double[theta.length];
        ComplexBufferArray X = new ComplexBufferArray(theta.length);
        double[] sin_theta_B_ar = new double[nlayers];
        double[] xyspace = new double[nlayers];
        double[] d_ar = new double[nlayers];
        Complex[] chi_0_ar = new Complex[nlayers];
        Complex[] chi_h_ar = new Complex[nlayers];
        Complex[] chi_h_neg_ar = new Complex[nlayers];
        final double stddevs = 4;
        double[] sin_theta_plus_thetaoffset = new double[theta.length];

        double dtheta = theta.length > 1 ? (theta[theta.length-1] - theta[0])/(theta.length-1) : 1;

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
        for (int i = 0; i < theta.length; i++)
        {
            sin_theta_plus_thetaoffset[i] = Math.sin(theta[i] + thetaoffset);
        }

        /*
        Susceptibilities substrateSusc;
        SimpleMaterial substrateMat;
        */

        //double zspace_s;
        //

        /* calculate in-plane lattice constants */
        xyspace[nlayers-1] = layers.get(nlayers-1).calcXYSpace(0);
        for(int i=nlayers-2; i>=0; i--) {
            xyspace[i] = layers.get(i).calcXYSpace(xyspace[i+1]);
        }

        for(int i=0; i<nlayers; i++) {
            Layer layer = layers.get(i);
            SimpleMaterial sm = layer.getSimpleMaterial(xyspace[i]);
            Susceptibilities susc = sm.suscFast(lambda);
            double suscfactor = layer.getSuscFactor().getExpected();
            if (suscfactor <= SUSCFACTOR_MIN)
            {
              suscfactor = SUSCFACTOR_MIN;
            }

            chi_0_ar[i] = susc.chi_0;
            chi_h_ar[i] = susc.chi_h.multiply(suscfactor);
            chi_h_neg_ar[i] = susc.chi_h_neg.multiply(suscfactor);
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
        double theta_Bs = Math.asin(sin_theta_Bs);
        double gamma0_s = Math.sin(theta_Bs - thetaoffset);
        double gammah_s = -Math.sin(theta_Bs + thetaoffset);
        double b_s = gamma0_s/gammah_s;
        double sin_theta_Bs_times_b_s_times_minus4 = -4*sin_theta_Bs*b_s;

        for(int i=0; i<result.length; i++) {
            result[i] = 0;
        }

        ComplexBuffer sqrteta2m1 = new ComplexBuffer();
        ComplexBuffer eta_denom = new ComplexBuffer();
        ComplexBuffer eta_denom_inv_s = new ComplexBuffer();
        ComplexBuffer sqrt_eta_sq_m_1 = new ComplexBuffer();
        ComplexBuffer chi_0_s_mul_2 = new ComplexBuffer();
        ComplexBuffer eta = new ComplexBuffer();

        boolean[] spols = {false,true};
        for(boolean spol: spols) {

            Complex chi_0_s = chi_0_ar[nlayers-1];
            chi_0_s_mul_2.set(chi_0_s).multiplyInPlace(2);
            Complex chi_h_s = chi_h_ar[nlayers-1];
            Complex chi_h_neg_s = chi_h_neg_ar[nlayers-1];

            assert(!Double.isNaN(chi_0_s.getReal()) && !Double.isNaN(chi_0_s.getImag()));
            assert(!Double.isNaN(chi_h_s.getReal()) && !Double.isNaN(chi_h_s.getImag()));
            assert(!Double.isNaN(chi_h_neg_s.getReal()) && !Double.isNaN(chi_h_neg_s.getImag()));

            double C_s = spol ? 1 : (Math.abs(Math.cos(2*theta_Bs)));
            eta_denom_inv_s.set(chi_h_s).multiplyInPlace(chi_h_neg_s);
            eta_denom_inv_s.multiplyInPlace(Math.abs(b_s));
            eta_denom_inv_s.sqrtInPlace();
            eta_denom_inv_s.multiplyInPlace(C_s*2.0);
            eta_denom_inv_s.invertInPlace();

            for(int i=0; i<theta.length; i++) {
                double b_times_alphah = (sin_theta_plus_thetaoffset[i] - sin_theta_Bs)*sin_theta_Bs_times_b_s_times_minus4;

                // eta = (b_s*alphah + 2*chi_0_s) / (2*C_s*sqrt(abs(b_s)*chi_h_s*chi_h_neg_s))
                eta.set(chi_0_s_mul_2).addInPlace(b_times_alphah).multiplyInPlace(eta_denom_inv_s);
                // X = eta +- sqrt(eta^2 - 1)
                sqrt_eta_sq_m_1.set(eta).multiplyInPlace(eta).subtractInPlace(1.0).sqrtInPlace();
                if (eta.getReal() < +0.0)
                {
                    sqrt_eta_sq_m_1.negateInPlace();
                }
                X.set(i, eta).subtractInPlace(i, sqrt_eta_sq_m_1);
            }

            ComplexBuffer sqrt_chi_h_and_neg_mul = new ComplexBuffer();
            ComplexBuffer chi_0_mul_2 = new ComplexBuffer();
            ComplexBuffer eta_divisor_inv = new ComplexBuffer();
            ComplexBuffer T = new ComplexBuffer(), T_mul_minus_I = new ComplexBuffer();
            ComplexBuffer S1S2_divisor = new ComplexBuffer(), S1S2 = new ComplexBuffer();
            ComplexBuffer expterm = new ComplexBuffer();
            ComplexBuffer S1 = new ComplexBuffer(), S2 = new ComplexBuffer();

            for(int j=nlayers-2; j>=0; j--) {
                Complex chi_0 = chi_0_ar[j];
                Complex chi_h = chi_h_ar[j];
                Complex chi_h_neg = chi_h_neg_ar[j];
                chi_0_mul_2.set(chi_0).multiplyInPlace(2);

                sqrt_chi_h_and_neg_mul.set(chi_h);
                sqrt_chi_h_and_neg_mul.multiplyInPlace(chi_h_neg);
                sqrt_chi_h_and_neg_mul.sqrtInPlace();

                double sin_theta_B = sin_theta_B_ar[j];
                double d = d_ar[j];
                double theta_B = Math.asin(sin_theta_B);
                double gamma0 = Math.sin(theta_B - thetaoffset);
                double gammah = -Math.sin(theta_B + thetaoffset);
                double b = gamma0/gammah;
                double sin_theta_B_times_b = sin_theta_B*b;
                double C = spol ? 1 : Math.abs(Math.cos(2*theta_B));
                double C_mul_2_mul_sqrt_abs_b = C*2*Math.sqrt(Math.abs(b));
                eta_divisor_inv.set(C_mul_2_mul_sqrt_abs_b).multiplyInPlace(sqrt_chi_h_and_neg_mul).invertInPlace();
                double multcoeff = Math.PI*C*d/(lambda*Math.sqrt(Math.abs(gamma0*gammah)));
                T.set(sqrt_chi_h_and_neg_mul).multiplyInPlace(multcoeff);
                T_mul_minus_I.set(0, -1).multiplyInPlace(T);
                //assert(!Double.isNaN(d) && !Double.isNaN(sin_theta_B));
                /*
                   This block is 94% of CPU time of this thread even for simple cases.
                   However, if object GC is taken into account, it's currently 88%.
                   Unfortunately, calculation of the square root and the exponential is
                   hard to optimize.
                 */
                for(int i=0; i<theta.length; i++) {
                    /*
                       eta = (-4*(sin(theta)-sin(theta_B))*sin(theta_B)*b + 2*chi0)
                           / (2*C*sqrt(|b|)*sqrt(chih*chihneg))
                       ERROR!
                       Should be:
                       eta = (-(sin(theta)-sin(theta_B))*sin(theta_B)*b + 0.5*chi0*(1-b))
                           / (C*sqrt(|b|)*sqrt(chih*chihneg))
                           <- so 2*chi0:n replaced by (1-b)*chi0
                           Should check formulas of gamma0 and gammah.
                     */
                    double b_times_alphah = -4*(sin_theta_plus_thetaoffset[i] - sin_theta_B)*sin_theta_B_times_b;
                    //assert(!Double.isNaN(b_times_alphah));
                    eta.set(chi_0_mul_2).addInPlace(b_times_alphah).multiplyInPlace(eta_divisor_inv);
                    //eta.set(b_times_alphah).multiplyInPlace(eta_divisor_inv);
                    sqrteta2m1.set(eta).multiplyInPlace(eta).subtractInPlace(1).sqrtInPlace();
                    //sqrteta2m1.set(eta).multiplyInPlace(eta).subtractInPlace(1);

                    /*
                    assert(!Double.isNaN(T.getReal()));
                    assert(!Double.isNaN(T.getImag()));
                    */
                    /*
                     * S1 = (X[i] - eta + sqrt(eta^2-1))*exp(-i*T*sqrt(eta^2-1))
                     * S2 = (X[i] - eta - sqrt(eta^2-1))*exp(i*T*sqrt(eta^2-1))
                     */

                    expterm.set(T_mul_minus_I).multiplyInPlace(sqrteta2m1).expInPlace();
                    //expterm.set(T_mul_minus_I).multiplyInPlace(sqrteta2m1);
                    S1.set(X, i).subtractInPlace(eta).addInPlace(sqrteta2m1).multiplyInPlace(expterm);

                    expterm.invertInPlace(); // expterm <- sqrt(-i*T*sqrt(eta^2-1))
                    S2.set(X, i).subtractInPlace(eta).subtractInPlace(sqrteta2m1).multiplyInPlace(expterm);
                    S1S2_divisor.set(S1).subtractInPlace(S2);
                    S1S2.set(S1).addInPlace(S2).divideInPlace(S1S2_divisor);
                    /*
                    assert(!Double.isNaN(S1.getReal()));
                    assert(!Double.isNaN(S1.getImag()));
                    assert(!Double.isNaN(S2.getReal()));
                    assert(!Double.isNaN(S2.getImag()));
                    assert(!Double.isNaN(S1S2.getReal()));
                    assert(!Double.isNaN(S1S2.getImag()));
                    */
                    /*
                       X <- eta + sqrt(eta^2 - 1)*(S1+S2)/(S1-S2)
                     */
                    X.set(i, sqrteta2m1).multiplyInPlace(i, S1S2).addInPlace(i, eta);
                    /*
                    assert(!Double.isNaN(X[i].getReal()));
                    assert(!Double.isNaN(X[i].getImag()));
                    */
                }
            }
            for(int i=0; i<theta.length; i++) {
                double Xabs = ComplexUtils.abs(X, i);
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

    public double[] xrdCurveFast(double[] theta) throws SimulationException {

        int nlayers = layers.size();
        double[] result = new double[theta.length];
        ComplexBuffer[] X = new ComplexBuffer[theta.length];
        double[] sin_theta_B_ar = new double[nlayers];
        double[] xyspace = new double[nlayers];
        double[] d_ar = new double[nlayers];
        Complex[] chi_0_ar = new Complex[nlayers];
        Complex[] chi_h_ar = new Complex[nlayers];
        Complex[] chi_h_neg_ar = new Complex[nlayers];
        final double stddevs = 4;
        double[] sin_theta_plus_thetaoffset = new double[theta.length];

        double dtheta = theta.length > 1 ? (theta[theta.length-1] - theta[0])/(theta.length-1) : 1;

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
        for (int i = 0; i < theta.length; i++)
        {
            sin_theta_plus_thetaoffset[i] = Math.sin(theta[i] + thetaoffset);
        }

        /*
        Susceptibilities substrateSusc;
        SimpleMaterial substrateMat;
        */

        //double zspace_s;
        //

        /* calculate in-plane lattice constants */
        xyspace[nlayers-1] = layers.get(nlayers-1).calcXYSpace(0);
        for(int i=nlayers-2; i>=0; i--) {
            xyspace[i] = layers.get(i).calcXYSpace(xyspace[i+1]);
        }

        for(int i=0; i<nlayers; i++) {
            Layer layer = layers.get(i);
            SimpleMaterial sm = layer.getSimpleMaterial(xyspace[i]);
            Susceptibilities susc = sm.suscFast(lambda);
            double suscfactor = layer.getSuscFactor().getExpected();
            if (suscfactor <= SUSCFACTOR_MIN)
            {
              suscfactor = SUSCFACTOR_MIN;
            }

            chi_0_ar[i] = susc.chi_0;
            chi_h_ar[i] = susc.chi_h.multiply(suscfactor);
            chi_h_neg_ar[i] = susc.chi_h_neg.multiply(suscfactor);
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
        double theta_Bs = Math.asin(sin_theta_Bs);
        double gamma0_s = Math.sin(theta_Bs - thetaoffset);
        double gammah_s = -Math.sin(theta_Bs + thetaoffset);
        double b_s = gamma0_s/gammah_s;
        double sin_theta_Bs_times_b_s_times_minus4 = -4*sin_theta_Bs*b_s;

        for(int i=0; i<result.length; i++) {
            result[i] = 0;
        }

        ComplexBuffer sqrteta2m1 = new ComplexBuffer();
        ComplexBuffer eta_denom = new ComplexBuffer();
        ComplexBuffer eta_denom_inv_s = new ComplexBuffer();
        ComplexBuffer sqrt_eta_sq_m_1 = new ComplexBuffer();
        ComplexBuffer chi_0_s_mul_2 = new ComplexBuffer();
        ComplexBuffer eta = new ComplexBuffer();

        boolean[] spols = {false,true};
        for(boolean spol: spols) {

            Complex chi_0_s = chi_0_ar[nlayers-1];
            chi_0_s_mul_2.set(chi_0_s).multiplyInPlace(2);
            Complex chi_h_s = chi_h_ar[nlayers-1];
            Complex chi_h_neg_s = chi_h_neg_ar[nlayers-1];

            assert(!Double.isNaN(chi_0_s.getReal()) && !Double.isNaN(chi_0_s.getImag()));
            assert(!Double.isNaN(chi_h_s.getReal()) && !Double.isNaN(chi_h_s.getImag()));
            assert(!Double.isNaN(chi_h_neg_s.getReal()) && !Double.isNaN(chi_h_neg_s.getImag()));

            double C_s = spol ? 1 : (Math.abs(Math.cos(2*theta_Bs)));
            eta_denom_inv_s.set(chi_h_s).multiplyInPlace(chi_h_neg_s);
            eta_denom_inv_s.multiplyInPlace(Math.abs(b_s));
            eta_denom_inv_s.sqrtInPlace();
            eta_denom_inv_s.multiplyInPlace(C_s*2.0);
            eta_denom_inv_s.invertInPlace();

            for(int i=0; i<theta.length; i++) {
                double b_times_alphah = (sin_theta_plus_thetaoffset[i] - sin_theta_Bs)*sin_theta_Bs_times_b_s_times_minus4;

                // eta = (b_s*alphah + 2*chi_0_s) / (2*C_s*sqrt(abs(b_s)*chi_h_s*chi_h_neg_s))
                eta.set(chi_0_s_mul_2).addInPlace(b_times_alphah).multiplyInPlace(eta_denom_inv_s);
                // X = eta +- sqrt(eta^2 - 1)
                sqrt_eta_sq_m_1.set(eta).multiplyInPlace(eta).subtractInPlace(1.0).sqrtInPlace();
                if (eta.getReal() < +0.0)
                {
                    sqrt_eta_sq_m_1.negateInPlace();
                }
                X[i] = new ComplexBuffer(eta).subtractInPlace(sqrt_eta_sq_m_1);
            }

            ComplexBuffer sqrt_chi_h_and_neg_mul = new ComplexBuffer();
            ComplexBuffer chi_0_mul_2 = new ComplexBuffer();
            ComplexBuffer eta_divisor_inv = new ComplexBuffer();
            ComplexBuffer T = new ComplexBuffer(), T_mul_minus_I = new ComplexBuffer();
            ComplexBuffer S1S2_divisor = new ComplexBuffer(), S1S2 = new ComplexBuffer();
            ComplexBuffer expterm = new ComplexBuffer();
            ComplexBuffer S1 = new ComplexBuffer(), S2 = new ComplexBuffer();

            for(int j=nlayers-2; j>=0; j--) {
                Complex chi_0 = chi_0_ar[j];
                Complex chi_h = chi_h_ar[j];
                Complex chi_h_neg = chi_h_neg_ar[j];
                chi_0_mul_2.set(chi_0).multiplyInPlace(2);

                sqrt_chi_h_and_neg_mul.set(chi_h);
                sqrt_chi_h_and_neg_mul.multiplyInPlace(chi_h_neg);
                sqrt_chi_h_and_neg_mul.sqrtInPlace();

                double sin_theta_B = sin_theta_B_ar[j];
                double d = d_ar[j];
                double theta_B = Math.asin(sin_theta_B);
                double gamma0 = Math.sin(theta_B - thetaoffset);
                double gammah = -Math.sin(theta_B + thetaoffset);
                double b = gamma0/gammah;
                double sin_theta_B_times_b = sin_theta_B*b;
                double C = spol ? 1 : Math.abs(Math.cos(2*theta_B));
                double C_mul_2_mul_sqrt_abs_b = C*2*Math.sqrt(Math.abs(b));
                eta_divisor_inv.set(C_mul_2_mul_sqrt_abs_b).multiplyInPlace(sqrt_chi_h_and_neg_mul).invertInPlace();
                double multcoeff = Math.PI*C*d/(lambda*Math.sqrt(Math.abs(gamma0*gammah)));
                T.set(sqrt_chi_h_and_neg_mul).multiplyInPlace(multcoeff);
                T_mul_minus_I.set(0, -1).multiplyInPlace(T);
                //assert(!Double.isNaN(d) && !Double.isNaN(sin_theta_B));
                /*
                   This block is 94% of CPU time of this thread even for simple cases.
                   However, if object GC is taken into account, it's currently 88%.
                   Unfortunately, calculation of the square root and the exponential is
                   hard to optimize.
                 */
                for(int i=0; i<theta.length; i++) {
                    /*
                       eta = (-4*(sin(theta)-sin(theta_B))*sin(theta_B)*b + 2*chi0)
                           / (2*C*sqrt(|b|)*sqrt(chih*chihneg))
                       ERROR!
                       Should be:
                       eta = (-(sin(theta)-sin(theta_B))*sin(theta_B)*b + 0.5*chi0*(1-b))
                           / (C*sqrt(|b|)*sqrt(chih*chihneg))
                           <- so 2*chi0:n replaced by (1-b)*chi0
                           Should check formulas of gamma0 and gammah.
                     */
                    double b_times_alphah = -4*(sin_theta_plus_thetaoffset[i] - sin_theta_B)*sin_theta_B_times_b;
                    //assert(!Double.isNaN(b_times_alphah));
                    eta.set(chi_0_mul_2).addInPlace(b_times_alphah).multiplyInPlace(eta_divisor_inv);
                    //eta.set(b_times_alphah).multiplyInPlace(eta_divisor_inv);
                    sqrteta2m1.set(eta).multiplyInPlace(eta).subtractInPlace(1).sqrtInPlace();
                    //sqrteta2m1.set(eta).multiplyInPlace(eta).subtractInPlace(1);

                    /*
                    assert(!Double.isNaN(T.getReal()));
                    assert(!Double.isNaN(T.getImag()));
                    */
                    /*
                     * S1 = (X[i] - eta + sqrt(eta^2-1))*exp(-i*T*sqrt(eta^2-1))
                     * S2 = (X[i] - eta - sqrt(eta^2-1))*exp(i*T*sqrt(eta^2-1))
                     */

                    expterm.set(T_mul_minus_I).multiplyInPlace(sqrteta2m1).expInPlace();
                    //expterm.set(T_mul_minus_I).multiplyInPlace(sqrteta2m1);
                    S1.set(X[i]).subtractInPlace(eta).addInPlace(sqrteta2m1).multiplyInPlace(expterm);

                    expterm.invertInPlace(); // expterm <- sqrt(-i*T*sqrt(eta^2-1))
                    S2.set(X[i]).subtractInPlace(eta).subtractInPlace(sqrteta2m1).multiplyInPlace(expterm);
                    S1S2_divisor.set(S1).subtractInPlace(S2);
                    S1S2.set(S1).addInPlace(S2).divideInPlace(S1S2_divisor);
                    /*
                    assert(!Double.isNaN(S1.getReal()));
                    assert(!Double.isNaN(S1.getImag()));
                    assert(!Double.isNaN(S2.getReal()));
                    assert(!Double.isNaN(S2.getImag()));
                    assert(!Double.isNaN(S1S2.getReal()));
                    assert(!Double.isNaN(S1S2.getImag()));
                    */
                    /*
                       X <- eta + sqrt(eta^2 - 1)*(S1+S2)/(S1-S2)
                     */
                    X[i].set(sqrteta2m1).multiplyInPlace(S1S2).addInPlace(eta);
                    /*
                    assert(!Double.isNaN(X[i].getReal()));
                    assert(!Double.isNaN(X[i].getImag()));
                    */
                }
            }
            for(int i=0; i<X.length; i++) {
                double Xabs = ComplexUtils.abs(X[i]);
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

    public double[] xrdCurveSlow(double[] theta) throws SimulationException {
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
        double[] sin_theta_plus_thetaoffset = new double[theta.length];

        double dtheta = theta.length > 1 ? (theta[theta.length-1] - theta[0])/(theta.length-1) : 1;

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
        for (int i = 0; i < theta.length; i++)
        {
            sin_theta_plus_thetaoffset[i] = Math.sin(theta[i] + thetaoffset);
        }

        /*
        Susceptibilities substrateSusc;
        SimpleMaterial substrateMat;
        */

        //double zspace_s;
        //

        /* calculate in-plane lattice constants */
        xyspace[nlayers-1] = layers.get(nlayers-1).calcXYSpace(0);
        for(int i=nlayers-2; i>=0; i--) {
            xyspace[i] = layers.get(i).calcXYSpace(xyspace[i+1]);
        }

        for(int i=0; i<nlayers; i++) {
            Layer layer = layers.get(i);
            SimpleMaterial sm = layer.getSimpleMaterial(xyspace[i]);
            Susceptibilities susc = sm.susc(lambda);
            double suscfactor = layer.getSuscFactor().getExpected();
            if (suscfactor <= SUSCFACTOR_MIN)
            {
              suscfactor = SUSCFACTOR_MIN;
            }

            chi_0_ar[i] = susc.chi_0;
            chi_h_ar[i] = susc.chi_h.multiply(suscfactor);
            chi_h_neg_ar[i] = susc.chi_h_neg.multiply(suscfactor);
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
        double theta_Bs = Math.asin(sin_theta_Bs);
        double gamma0_s = Math.sin(theta_Bs - thetaoffset);
        double gammah_s = -Math.sin(theta_Bs + thetaoffset);
        double b_s = gamma0_s/gammah_s;
        double sin_theta_Bs_times_b_s_times_minus4 = -4*sin_theta_Bs*b_s;

        for(int i=0; i<result.length; i++) {
            result[i] = 0;
        }

        Complex sqrteta2m1;
        Complex eta_denom;
        Complex eta_denom_inv_s;
        Complex sqrt_eta_sq_m_1;
        Complex chi_0_s_mul_2;
        Complex eta;

        boolean[] spols = {false,true};
        for(boolean spol: spols) {

            Complex chi_0_s = chi_0_ar[nlayers-1];
            chi_0_s_mul_2 = chi_0_s.multiply(2);
            Complex chi_h_s = chi_h_ar[nlayers-1];
            Complex chi_h_neg_s = chi_h_neg_ar[nlayers-1];

            assert(!Double.isNaN(chi_0_s.getReal()) && !Double.isNaN(chi_0_s.getImag()));
            assert(!Double.isNaN(chi_h_s.getReal()) && !Double.isNaN(chi_h_s.getImag()));
            assert(!Double.isNaN(chi_h_neg_s.getReal()) && !Double.isNaN(chi_h_neg_s.getImag()));

            double C_s = spol ? 1 : (Math.abs(Math.cos(2*theta_Bs)));
            eta_denom_inv_s = chi_h_s.multiply(chi_h_neg_s);
            eta_denom_inv_s = eta_denom_inv_s.multiply(Math.abs(b_s));
            eta_denom_inv_s = eta_denom_inv_s.sqrt();
            eta_denom_inv_s = eta_denom_inv_s.multiply(C_s*2.0);
            eta_denom_inv_s = eta_denom_inv_s.invert();

            for(int i=0; i<theta.length; i++) {
                double b_times_alphah = (sin_theta_plus_thetaoffset[i] - sin_theta_Bs)*sin_theta_Bs_times_b_s_times_minus4;

                // eta = (b_s*alphah + 2*chi_0_s) / (2*C_s*sqrt(abs(b_s)*chi_h_s*chi_h_neg_s))
                eta = chi_0_s_mul_2.add(b_times_alphah).multiply(eta_denom_inv_s);
                // X = eta +- sqrt(eta^2 - 1)
                sqrt_eta_sq_m_1 = eta.multiply(eta).subtract(1.0).sqrt();
                if (eta.getReal() < +0.0)
                {
                    sqrt_eta_sq_m_1 = sqrt_eta_sq_m_1.negate();
                }
                X[i] = eta.subtract(sqrt_eta_sq_m_1);
            }

            Complex sqrt_chi_h_and_neg_mul;
            Complex chi_0_mul_2;
            Complex eta_divisor_inv;
            Complex T, T_mul_minus_I;
            Complex S1S2_divisor, S1S2;
            Complex expterm;
            Complex S1, S2;

            for(int j=nlayers-2; j>=0; j--) {
                Complex chi_0 = chi_0_ar[j];
                Complex chi_h = chi_h_ar[j];
                Complex chi_h_neg = chi_h_neg_ar[j];
                chi_0_mul_2 = chi_0.multiply(2);

                sqrt_chi_h_and_neg_mul = chi_h.multiply(chi_h_neg).sqrt();

                double sin_theta_B = sin_theta_B_ar[j];
                double d = d_ar[j];
                double theta_B = Math.asin(sin_theta_B);
                double gamma0 = Math.sin(theta_B - thetaoffset);
                double gammah = -Math.sin(theta_B + thetaoffset);
                double b = gamma0/gammah;
                double sin_theta_B_times_b = sin_theta_B*b;
                double C = spol ? 1 : Math.abs(Math.cos(2*theta_B));
                double C_mul_2_mul_sqrt_abs_b = C*2*Math.sqrt(Math.abs(b));
                eta_divisor_inv = sqrt_chi_h_and_neg_mul.multiply(C_mul_2_mul_sqrt_abs_b).invert();
                double multcoeff = Math.PI*C*d/(lambda*Math.sqrt(Math.abs(gamma0*gammah)));
                T = sqrt_chi_h_and_neg_mul.multiply(multcoeff);
                T_mul_minus_I = new Complex(0, -1).multiply(T);
                //assert(!Double.isNaN(d) && !Double.isNaN(sin_theta_B));
                /*
                   This block is 94% of CPU time of this thread even for simple cases.
                   However, if object GC is taken into account, it's currently 88%.
                   Unfortunately, calculation of the square root and the exponential is
                   hard to optimize.
                 */
                for(int i=0; i<theta.length; i++) {
                    /*
                       eta = (-4*(sin(theta)-sin(theta_B))*sin(theta_B)*b + 2*chi0)
                           / (2*C*sqrt(|b|)*sqrt(chih*chihneg))
                       ERROR!
                       Should be:
                       eta = (-(sin(theta)-sin(theta_B))*sin(theta_B)*b + 0.5*chi0*(1-b))
                           / (C*sqrt(|b|)*sqrt(chih*chihneg))
                           <- so 2*chi0:n replaced by (1-b)*chi0
                           Should check formulas for gamma0 and gammah.
                     */
                    double b_times_alphah = -4*(sin_theta_plus_thetaoffset[i] - sin_theta_B)*sin_theta_B_times_b;
                    //assert(!Double.isNaN(b_times_alphah));
                    eta = chi_0_mul_2.add(b_times_alphah).multiply(eta_divisor_inv);
                    //eta.set(b_times_alphah).multiplyInPlace(eta_divisor_inv);
                    sqrteta2m1 = eta.multiply(eta).subtract(1.0).sqrt();

                    /*
                    assert(!Double.isNaN(T.getReal()));
                    assert(!Double.isNaN(T.getImag()));
                    */
                    /*
                     * S1 = (X[i] - eta + sqrt(eta^2-1))*exp(-i*T*sqrt(eta^2-1))
                     * S2 = (X[i] - eta - sqrt(eta^2-1))*exp(i*T*sqrt(eta^2-1))
                     */

                    expterm = T_mul_minus_I.multiply(sqrteta2m1).exp();
                    S1 = X[i].subtract(eta).add(sqrteta2m1).multiply(expterm);

                    expterm = expterm.invert(); // expterm <- sqrt(-i*T*sqrt(eta^2-1))
                    S2 = X[i].subtract(eta).subtract(sqrteta2m1).multiply(expterm);
                    S1S2_divisor = S1.subtract(S2);
                    S1S2 = S1.add(S2).divide(S1S2_divisor);
                    /*
                    assert(!Double.isNaN(S1.getReal()));
                    assert(!Double.isNaN(S1.getImag()));
                    assert(!Double.isNaN(S2.getReal()));
                    assert(!Double.isNaN(S2.getImag()));
                    assert(!Double.isNaN(S1S2.getReal()));
                    assert(!Double.isNaN(S1S2.getImag()));
                    */
                    /*
                       X <- eta + sqrt(eta^2 - 1)*(S1+S2)/(S1-S2)
                     */
                    X[i] = sqrteta2m1.multiply(S1S2).add(eta);
                    /*
                    assert(!Double.isNaN(X[i].getReal()));
                    assert(!Double.isNaN(X[i].getImag()));
                    */
                }
            }
            for(int i=0; i<X.length; i++) {
                double Xabs = ComplexUtils.abs(X[i]);
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

        double dtheta = theta.length > 1 ? (theta[theta.length-1] - theta[0])/(theta.length-1) : 1;

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
            double suscfactor = layer.getSuscFactor().getExpected();
            if (suscfactor <= SUSCFACTOR_MIN)
            {
              suscfactor = SUSCFACTOR_MIN;
            }

            chi_0_ar[i] = susc.chi_0;
            chi_h_ar[i] = susc.chi_h.multiply(suscfactor);
            chi_h_neg_ar[i] = susc.chi_h_neg.multiply(suscfactor);
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

            assert(!Double.isNaN(chi_0_s.getReal()) && !Double.isNaN(chi_0_s.getImag()));
            assert(!Double.isNaN(chi_h_s.getReal()) && !Double.isNaN(chi_h_s.getImag()));
            assert(!Double.isNaN(chi_h_neg_s.getReal()) && !Double.isNaN(chi_h_neg_s.getImag()));

            double theta_Bs = Math.asin(sin_theta_Bs);
            double gamma0_s = Math.sin(theta_Bs - thetaoffset);
            double gammah_s = -Math.sin(theta_Bs + thetaoffset);
            double b_s = gamma0_s/gammah_s;
            double C_s = spol ? 1 : Math.abs(Math.cos(2*theta_Bs));

            for(int i=0; i<theta.length; i++) {
                double alphah = -4*(Math.sin(theta[i] + thetaoffset) - sin_theta_Bs)*sin_theta_Bs;


                Complex eta = ComplexUtils.divide(ComplexUtils.add(b_s*alphah,ComplexUtils.multiply(2,chi_0_s)),
                    ComplexUtils.multiply(2*C_s*Math.sqrt(Math.abs(b_s)),ComplexUtils.sqrt(ComplexUtils.multiply(chi_h_s, chi_h_neg_s))));

                X[i] = ComplexUtils.subtract(eta, ComplexUtils.multiply(Math.signum(eta.getReal()), ComplexUtils.sqrt(ComplexUtils.subtract(ComplexUtils.multiply(eta,eta),1))));
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
                    Complex eta = ComplexUtils.divide(ComplexUtils.add(b*alphah,ComplexUtils.multiply(2,chi_0)),
                        ComplexUtils.multiply(2*C*Math.sqrt(Math.abs(b)),ComplexUtils.sqrt(ComplexUtils.multiply(chi_h, chi_h_neg))));
                    Complex sqrteta2m1 = ComplexUtils.sqrt(ComplexUtils.subtract(ComplexUtils.multiply(eta,eta),1));
                    Complex T = ComplexUtils.multiply((Math.PI*C*d)/(lambda*
                                // for small thetaoffset, we could optimize by using Math.abs(sin_theta_B)
                                // it is actually sqrt(sin(theta_B+thetaoffset)*sin(theta_B-thetaoffset)) = sqrt(abs(gamma0*gammah))
                                // but the difference is negligible for small thetaoffset
                                //Math.abs(sin_theta_B)),
                                Math.sqrt(Math.abs(gamma0*gammah))),
                                             ComplexUtils.sqrt(ComplexUtils.multiply(chi_h,chi_h_neg)));

                    /*
                    assert(!Double.isNaN(T.getReal()));
                    assert(!Double.isNaN(T.getImag()));
                    */
                    Complex S1 = ComplexUtils.multiply(ComplexUtils.add(ComplexUtils.subtract(X[i],eta),sqrteta2m1),
                                   ComplexUtils.exp(ComplexUtils.multiply(ComplexUtils.multiply(new Complex(0,-1),T),sqrteta2m1)));
                    Complex S2 = ComplexUtils.multiply(ComplexUtils.subtract(ComplexUtils.subtract(X[i],eta),sqrteta2m1),
                                   ComplexUtils.exp(ComplexUtils.multiply(ComplexUtils.multiply(new Complex(0,1),T),sqrteta2m1)));
                    Complex S1S2 = ComplexUtils.divide(ComplexUtils.add(S1,S2),ComplexUtils.subtract(S1,S2));
                    /*
                    assert(!Double.isNaN(S1.getReal()));
                    assert(!Double.isNaN(S1.getImag()));
                    assert(!Double.isNaN(S2.getReal()));
                    assert(!Double.isNaN(S2.getImag()));
                    assert(!Double.isNaN(S1S2.getReal()));
                    assert(!Double.isNaN(S1S2.getImag()));
                    */
                    X[i] = ComplexUtils.add(eta,ComplexUtils.multiply(sqrteta2m1, S1S2));
                    /*
                    assert(!Double.isNaN(X[i].getReal()));
                    assert(!Double.isNaN(X[i].getImag()));
                    */
                }
            }
            for(int i=0; i<X.length; i++) {
                double Xabs = ComplexUtils.abs(X[i]);
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
