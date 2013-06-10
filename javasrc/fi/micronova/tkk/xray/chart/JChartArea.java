package fi.micronova.tkk.xray.chart;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import org.jfree.chart.*;

/** Component for drawing a chart.
 *
 * This component draws a chart created by JFreeChart
 *
 */
public class JChartArea extends JPlotArea {
    private JFreeChart chart;
    private int width;
    private int height;

    private void updateImage() {
        synchronized(this) {
            if(chart != null && width > 0 && height > 0) {
                newImage(chart.createBufferedImage(width, height));
            } else {
                newImage(null);
            }
        }
    }

    /** Create a new component with an empty chart */
    public JChartArea() {
        super();
        this.chart = null;
        this.width = this.height = 0;
        this.addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {}
            public void componentMoved(ComponentEvent e) {}
            public void componentShown(ComponentEvent e) {}
            public void componentResized(ComponentEvent e) {
                width = getWidth();
                height = getHeight();
                updateImage();
            }
        });
    }

    /** Change the chart.
     * @param chart the new chart, which may be null
     * */
    public void newChart(JFreeChart chart) {
        synchronized(this) {
            this.chart = chart;
            updateImage();
        }
    }
}
