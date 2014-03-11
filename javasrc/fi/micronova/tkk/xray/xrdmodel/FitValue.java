package fi.micronova.tkk.xray.xrdmodel;
/* This class stores a value to fit and its range */


import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;


/* Not immutable! */


/** Fitting value and its range.
 *
 * This class stores a value to fit, and its allowed range. It is not immutable
 * and not thread safe.
 *
 */
public class FitValue implements XMLRowable {
    private double min, expected, max;
    private boolean enable; /* whether to fit the value or not */
    private final boolean supported; /* whether fitting is actually supported. not included in XML serialization */
    private final List<ValueListener> listeners = new ArrayList<ValueListener>();

    public static enum FitValueType {
      MIN, EXPECTED, MAX
    };

    public double getValue(FitValueType type)
    {
      if (type == null)
      {
        throw new NullPointerException();
      }
      switch (type)
      {
        case MIN:
          return min;
        case EXPECTED:
          return expected;
        case MAX:
          return max;
        default:
          throw new Error("not reached");
      }
    }
    public double getValueForFitting(FitValueType type)
    {
      return getValue(enable ? type : FitValueType.EXPECTED);
    }

    public boolean equals(Object o)
    {
      FitValue that;
      if (this == o)
      {
        return true;
      }
      if (o == null || !(o instanceof FitValue))
      {
        return false;
      }
      that = (FitValue)o;
      if (   this.min       != that.min
          || this.expected  != that.expected
          || this.max       != that.max
          || this.enable    != that.enable
          || this.supported != that.supported)
      {
        return false;
      }
      return true;
    }

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
    public FitValue(DocumentFragment frag)
    {
        this(frag, true);
    }
    public FitValue(DocumentFragment frag, boolean supported)
    {
        min = frag.getAttrDoubleNotNull("min");
        expected = frag.getAttrDoubleNotNull("expected");
        max = frag.getAttrDoubleNotNull("max");
        enable = frag.getAttrIntNotNull("enable") != 0;
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
    public void toXMLRow(DocumentFragment f)
    {
        f.setAttrDouble("min", min);
        f.setAttrDouble("expected", expected);
        f.setAttrDouble("max", max);
        f.setAttrInt("enable", enable?1:0);
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
