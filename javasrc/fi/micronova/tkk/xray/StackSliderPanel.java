package fi.micronova.tkk.xray;
import java.awt.*;
import javax.swing.*;
import fi.micronova.tkk.xray.xrdmodel.*;

public class StackSliderPanel extends JPanel {
    public StackSliderPanel(LayerStack ls) {
        super();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());
        new SingleScrollbarUpdater(ls.getSum(), "sum (dB)", 1).addToGridBag(this);
        new SingleScrollbarUpdater(ls.getProd(), "norm (dB)", 1).addToGridBag(this);
        new SingleScrollbarUpdater(ls.getOffset(), "offset (degrees)", 180/Math.PI).addToGridBag(this);
        new SingleScrollbarUpdater(ls.getStdDev(), "FWHM (degrees)", 180/Math.PI * (2*Math.sqrt(2*Math.log(2)))).addToGridBag(this);
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        add(new JPanel(), c);
    }
}
