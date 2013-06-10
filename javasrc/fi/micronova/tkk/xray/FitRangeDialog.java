package fi.micronova.tkk.xray;
import fi.micronova.tkk.xray.dialogs.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for changing wavelength */
public class FitRangeDialog extends TextFieldDialog {
    private boolean succesful;
    private FitValue v;
    private double scale;
    private final String[] labels = {"min","val","max"};

    protected int nFields() {
        return 3;
    }
    protected String getDefault(int i) {
        if(v == null)
            return "0";
        switch(i) {
            case 0:
                return String.format(Locale.US,"%.6g",v.getMin()*scale);
            case 1:
                return String.format(Locale.US,"%.6g",v.getExpected()*scale);
            case 2:
                return String.format(Locale.US,"%.6g",v.getMax()*scale);
            default:
                return null;
        }
    }
    protected String getLabel(int i) {
        return labels[i];
    }
    protected void newValues(String[] values) {
        double min, val, max;
        min = Double.parseDouble(values[0])/scale;
        val = Double.parseDouble(values[1])/scale;
        max = Double.parseDouble(values[2])/scale;
        if(min > val || val > max || min > max)
            throw new IllegalArgumentException();
        v.setValues(min,val,max,v.getEnabled());
        succesful = true;
    }
    public boolean call(FitValue v, double scale) {
        this.scale = scale;
        this.succesful = false;
        this.v = v;
        showDialog();
        return succesful;
    }
    public FitRangeDialog(Frame f, String propertyName)
    {
        super(f,propertyName);
        this.labels[0] = propertyName + " min";
    }
    public FitRangeDialog(Dialog d, String propertyName)
    {
        super(d,propertyName);
        this.labels[0] = propertyName + " min";
    }
}
