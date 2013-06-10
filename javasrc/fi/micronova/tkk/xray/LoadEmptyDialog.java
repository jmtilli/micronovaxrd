package fi.micronova.tkk.xray;
import javax.swing.*;
/* Dialog for importing empty measurements */
class LoadEmptyDialog extends DataDialog {
    public LoadEmptyDialog(JFrame f) {
        super(f, "Empty measurement options", "angle", null, 700, 0, 90, 0, 90);
    }
}
