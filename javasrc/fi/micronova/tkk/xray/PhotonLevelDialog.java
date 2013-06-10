package fi.micronova.tkk.xray;
import fi.micronova.tkk.xray.dialogs.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for changing wavelength */
public class PhotonLevelDialog extends TextFieldDialog {
    private boolean succesful;
    private double level = 0;

    protected int nFields() {
        return 1;
    }
    protected String getDefault(int i) {
        return ""+(0);
    }
    protected String getLabel(int i) {
        return "Photon level (dB)";
    }
    protected void newValues(String[] v) {
        level = Double.parseDouble(v[0]);
        succesful = true;
    }
    public Double call() {
        this.level = 0;
        this.succesful = false;
        showDialog();
        if(succesful)
            return this.level;
        else
            return null;
    }
    public PhotonLevelDialog(Frame f)
    {
        super(f,"Photon level");
    }
}
