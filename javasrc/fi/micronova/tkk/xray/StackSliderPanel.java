package fi.micronova.tkk.xray;
import java.awt.*;
import javax.swing.*;
import fi.micronova.tkk.xray.xrdmodel.*;

public class StackSliderPanel extends JPanel {
    public StackSliderPanel(XRDApp xrd, LayerStack ls) {
        super();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());
        new SingleScrollbarUpdater(xrd, ls, ls.getSum(), "sum (dB)", 1, false).addToGridBag(this);
        new SingleScrollbarUpdater(xrd, ls, ls.getProd(), "norm (dB)", 1, false).addToGridBag(this);
        new SingleScrollbarUpdater(xrd, ls, ls.getOffset(), "offset (\u00B0)", 180/Math.PI, false).addToGridBag(this);
        new SingleScrollbarUpdater(xrd, ls, ls.getStdDev(), "FWHM (\u00B0)", 180/Math.PI * (2*Math.sqrt(2*Math.log(2))), true).addToGridBag(this);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        add(new JPanel(), c);
    }
}
