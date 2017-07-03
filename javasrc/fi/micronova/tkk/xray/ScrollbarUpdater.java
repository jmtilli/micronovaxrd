package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import fi.micronova.tkk.xray.xrdmodel.*;


/** Updates the slider GUI and its sliders when the model is modified; handles
 * scrollbar events.
 *
 * <p>
 *
 * Unfortunately this complex piece of Java code is not commented well.
 * Furthermore, it uses lots of difficult Java programming tricks, such as
 * inner classes. You have to try to understand this code without comments.
 *
 */

public class ScrollbarUpdater {
    private LayerStack ls;
    private JTabbedPane sliderPane;
    /*
    private JSlider convolutionSlider;
    private JLabel convolutionLabel;
    private JSlider normalSlider;
    private JLabel normalLabel;
    private JSlider sumSlider;
    private JLabel sumLabel;
    */
    /*
    private static final double maxConv = 0.2;
    private static final double minNorm = -25;
    private static final double maxNorm = 25;
    private static final double minSum = -80;
    private static final double maxSum = -30;
    */
    //private static final double FWHM_SCALE = 2*Math.sqrt(2*Math.log(2));

    /** Constructor.
     *
     * <p>
     *
     * Creates a slider-based interface for fine-tuning a layer model.
     *
     * @param ls the layer stack to create the slider-based user interface for
     * @param sliderPanel a user interface component to create the slider interface in
     * 
     */

    private void addTabs(int i0, int i1) {
        for(int i=i0; i<i1; i++) {
            Layer l = ls.getElementAt(i);
            final SliderPanel sliders = new SliderPanel(l);
            sliderPane.insertTab(sliders.getTitle(),null,sliders,/*l.getName()*//*tip*/null,i+1);
            sliders.addTitleListener(new SliderPanel.TitleListener() {
                public void titleChanged() {
                    /* now we must find the index of the component and change its title, since the index may have been changed */
                    int index = -1;
                    int tabs = sliderPane.getTabCount();
                    /* start from 1 since there's the global panel at 0 */
                    for(int i=1; i<tabs; i++) {
                        if(sliderPane.getComponentAt(i) == sliders)
                            sliderPane.setTitleAt(i, sliders.getTitle());
                    }
                }
            });
        }
    }
    private void removeTabs(int i0, int i1) {
        for(int i=i1-1; i>=i0; i--) {
            SliderPanel p = (SliderPanel)sliderPane.getComponentAt(i+1);
            sliderPane.removeTabAt(i+1);
            p.cleanup(); /* cut listener structure to allow GC */
        }
    }
    public ScrollbarUpdater(LayerStack ls, JComponent sliderPanel) {
        /*
        JPanel convolutionPanel = new JPanel();
        JPanel convLabelPanel = new JPanel();
        JPanel normalPanel = new JPanel();
        JPanel normLabelPanel = new JPanel();
        JPanel sumPanel = new JPanel();
        JPanel sumLabelPanel = new JPanel();
        */

        sliderPanel.setLayout(new BorderLayout(5,5));
        //convLabelPanel.setLayout(new BorderLayout(5,5));
        final ScrollbarUpdater thisUpdater = this;


        this.ls = ls;
        this.sliderPane = new JTabbedPane();

        JPanel global = new StackSliderPanel(ls);
        sliderPane.insertTab("Global",null,global,"Global settings",0);

        /* The code handles duplicates otherwise perfectly, but it shows
         * multiple tabs for the same layer. */
        ls.addLayerModelListener(new LayerModelAdapter() {
            public void layersChanged(ListDataEvent ev) {
                layersRemoved(ev);
                layersAdded(ev);
            }
            public void layersAdded(ListDataEvent ev) {
                int i0 = ev.getIndex0();
                int i1 = ev.getIndex1()+1;
                addTabs(i0, i1);
            }
            public void layersRemoved(ListDataEvent ev) {
                int i0 = ev.getIndex0();
                int i1 = ev.getIndex1()+1;
                removeTabs(i0, i1);
            }
        });
        addTabs(0, ls.getSize());

        /*
        this.convolutionSlider = new JSlider(0,1000);
        this.convolutionLabel = new JLabel("FWHM (\u00B0) = 0.000000");

        this.normalSlider = new JSlider(0,1000);
        this.normalLabel = new JLabel("normalization (dB) = -00.000");
        normalLabel.setPreferredSize(normalLabel.getPreferredSize());

        this.sumSlider = new JSlider(0,1000);
        this.sumLabel = new JLabel("sum term (dB) = -0000.000");
        sumLabel.setPreferredSize(sumLabel.getPreferredSize());

        convolutionPanel.setLayout(new BorderLayout(5,5));
        convolutionPanel.add(convLabelPanel, BorderLayout.WEST);
        convolutionPanel.add(convolutionSlider, BorderLayout.CENTER);
        convLabelPanel.add(new JLabel("convolution: "),BorderLayout.WEST);
        convLabelPanel.add(convolutionLabel, BorderLayout.CENTER);

        normalPanel.setLayout(new BorderLayout(5,5));
        normalPanel.add(normLabelPanel, BorderLayout.WEST);
        normalPanel.add(normalSlider, BorderLayout.CENTER);
        normLabelPanel.add(new JLabel("normalization: "),BorderLayout.WEST);
        normLabelPanel.add(normalLabel, BorderLayout.CENTER);

        sumPanel.setLayout(new BorderLayout(5,5));
        sumPanel.add(sumLabelPanel, BorderLayout.WEST);
        sumPanel.add(sumSlider, BorderLayout.CENTER);
        sumLabelPanel.add(new JLabel("sum term: "),BorderLayout.WEST);
        sumLabelPanel.add(sumLabel, BorderLayout.CENTER);

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel,BoxLayout.Y_AXIS));
        northPanel.add(convolutionPanel);
        northPanel.add(normalPanel);
        northPanel.add(sumPanel);
        */

        //JPanel northPanel = new StackSliderPanel(ls);

        sliderPanel.add(sliderPane, BorderLayout.CENTER);
        //sliderPanel.add(northPanel, BorderLayout.NORTH);
    }
};
