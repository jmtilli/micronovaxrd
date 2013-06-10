package fi.micronova.tkk.xray.xrdmodel;
/* This class stores a value to fit and its range */


import java.util.*;
import org.w3c.dom.*;


/* Not immutable! */


/** Fitting value and its range.
 *
 * This class stores a value to fit, and its allowed range. It is not immutable
 * and not thread safe.
 *
 */
public class FitValue {
    private double min, expected, max;
    private boolean enable; /* whether to fit the value or not */
    private final boolean supported; /* whether fitting is actually supported. not included in XML serialization */
    private final List<ValueListener> listeners = new ArrayList<ValueListener>();

    public boolean isSupported() {
        return supported;
    }

    public void addValueListener(ValueListener l) {
        listeners.add(l);
    }
    public void removeValueListener(ValueListener l) {
        listeners.remove(l);
    }
    private void signalChange() {
        ValueEvent ev = new ValueEvent(this);
        for(ValueListener listener: listeners)
            listener.valueChanged(ev);
    }
    public FitValue(Node n) {
        enable = Integer.parseInt(n.getAttributes().getNamedItem("enable").getNodeValue()) != 0;
        min = Double.parseDouble(n.getAttributes().getNamedItem("min").getNodeValue());
        expected = Double.parseDouble(n.getAttributes().getNamedItem("expected").getNodeValue());
        max = Double.parseDouble(n.getAttributes().getNamedItem("max").getNodeValue());
        supported = true;
    }
    public FitValue(Node n, boolean supported) {
        min = Double.parseDouble(n.getAttributes().getNamedItem("min").getNodeValue());
        expected = Double.parseDouble(n.getAttributes().getNamedItem("expected").getNodeValue());
        max = Double.parseDouble(n.getAttributes().getNamedItem("max").getNodeValue());
        if(supported)
            enable = Integer.parseInt(n.getAttributes().getNamedItem("enable").getNodeValue()) != 0;
        else
            enable = false;
        this.supported = supported;
    }
    public FitValue(double min, double expected, double max)
    {
        this(min, expected, max, true, true);
    }
    public FitValue(double min, double expected, double max, boolean enable)
    {
        this(min, expected, max, enable, true);
    }
    public FitValue(double min, double expected, double max, boolean enable, boolean supported)
    {
        this.supported = supported;
        setValues(min, expected, max, enable);
    }
    /** Set values
     *
     * @param min new minimum value
     * @param expected new expected value
     * @param max new maximum value
     * @throws IllegalArgumentException if min &gt; max or expected not in [min,max]
     */
    public void setValues(double min, double expected, double max, boolean enabled)
    {
        if(expected < min || expected > max || min > max)
            throw new IllegalArgumentException("Invalid boundary values");
        this.expected = expected;
        this.min = min;
        this.max = max;
        this.enable = supported ? enabled : false;
        signalChange();
    }
    /** Enables or disables fitting of this value
     */
    public void setEnabled(boolean enable)
    {
        this.enable = supported ? enable : false;
        signalChange();
    }
    /** Make a copy of this object
     */
    public FitValue deepCopy() {
        return new FitValue(min, expected, max, enable, supported);
    }
    public void deepCopyFrom(FitValue v2) {
        setValues(v2.min, v2.expected, v2.max, v2.enable);
    }
    /** Make a fencodeable data structure of this object
     */
    public Element export(Document doc) {
        Element fitValue = doc.createElement("fitvalue");
        fitValue.setAttribute("min",""+min);
        fitValue.setAttribute("expected",""+expected);
        fitValue.setAttribute("max",""+max);
        fitValue.setAttribute("enable",enable?"1":"0");
        return fitValue;
    }
    public double getMin() { return this.min; };
    public double getMax() { return this.max; };
    public double getExpected() { return this.expected; };
    public boolean getEnabled() { return this.enable; }

    /** Set the expected value.
     *
     * The new expected value is set to newExpected unless newExpected &lt; min
     * or newExpected &gt; max in which case the expected value is set to min
     * or max, respectively.
     *
     * @param newExpected the new expected value
     */
    public void setExpected(double newExpected) {
        if(newExpected < this.min)
            newExpected = this.min;
        if(newExpected > this.max)
            newExpected = this.max;
        this.expected = newExpected;
        signalChange();
    }
};
