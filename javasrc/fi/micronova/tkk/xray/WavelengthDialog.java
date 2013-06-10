package fi.micronova.tkk.xray;
import fi.micronova.tkk.xray.dialogs.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for changing wavelength */
public class WavelengthDialog extends TextFieldDialog {
    private boolean succesful;
    private double lambda = 0;

    protected int nFields() {
        return 1;
    }
    protected String getDefault(int i) {
        return String.format(Locale.US,"%.6g",lambda*1e9);
    }
    protected String getLabel(int i) {
        return "wavelength (nm)";
    }
    protected void newValues(String[] v) {
        lambda = Double.parseDouble(v[0])/1e9;
        succesful = true;
    }
    public Double call(double lambda) {
        this.lambda = lambda;
        this.succesful = false;
        showDialog();
        if(succesful)
            return this.lambda;
        else
            return null;
    }
    public WavelengthDialog(Frame f)
    {
        super(f,"Wavelength");
    }
}
