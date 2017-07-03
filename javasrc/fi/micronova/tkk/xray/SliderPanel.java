package fi.micronova.tkk.xray;
import java.awt.*;
import java.util.*;
import javax.swing.*;
import fi.micronova.tkk.xray.xrdmodel.*;

public class SliderPanel extends JPanel {
    private final Layer l;
    private final SingleScrollbarUpdater dUpdater, pUpdater, rUpdater, whUpdater;
    public Layer getLayer() {
        return l;
    }

    public interface TitleListener {
        public void titleChanged();
    };

    public final Set<TitleListener> listeners = new HashSet<TitleListener>();


    public void addTitleListener(TitleListener l) {
        listeners.add(l);
    }
    public void removeTitleListener(TitleListener l) {
        listeners.remove(l);
    }

    /** returns the title of this slider panel to be shown on the tabbed pane */
    public String getTitle() {
        return l.getName();
    }


    private final LayerListener layerListener = new LayerListener() {
        public void layerPropertyChanged(LayerEvent ev) {
            for(TitleListener l: listeners)
                l.titleChanged();
        }
    };

    public SliderPanel(Layer l) {
        super();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(new GridBagLayout());
        dUpdater = new SingleScrollbarUpdater(l.getThickness(), "d (nm)", 1e9, true);
        pUpdater = new SingleScrollbarUpdater(l.getComposition(), "p", 1, true);
        rUpdater = new SingleScrollbarUpdater(l.getRelaxation(), "r", 1, true);
        whUpdater = new SingleScrollbarUpdater(l.getSuscFactor(), "wh", 1, true);
        dUpdater.addToGridBag(this);
        pUpdater.addToGridBag(this);
        rUpdater.addToGridBag(this);
        whUpdater.addToGridBag(this);

        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1;
        add(new JPanel(), c);
        this.l = l;
        l.addLayerListener(layerListener);
    }

    /** must remove listeners manually */
    public void cleanup() {
        dUpdater.cleanup();
        pUpdater.cleanup();
        rUpdater.cleanup();
        whUpdater.cleanup();
        l.removeLayerListener(layerListener);
    }
}
