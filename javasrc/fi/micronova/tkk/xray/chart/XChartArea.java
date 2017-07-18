package fi.micronova.tkk.xray.chart;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import org.knowm.xchart.*;


/** Component for drawing a chart.
 *
 * This component draws a chart created by JFreeChart
 *
 */
public class XChartArea extends JPlotArea {
    private XYChart chart;
    private int width;
    private int height;
    private static final byte[] r = {(byte)255,(byte)0,(byte)0,(byte)20,(byte)39,(byte)59,(byte)78,(byte)98,(byte)118,(byte)137,(byte)157,(byte)177,(byte)196,(byte)216,(byte)235,(byte)255};
    private static final byte[] g = {0,(byte)0,(byte)0,(byte)20,(byte)39,(byte)59,(byte)78,(byte)98,(byte)118,(byte)137,(byte)157,(byte)177,(byte)196,(byte)216,(byte)235,(byte)255};
    private static final byte[] b = {0,(byte)255,(byte)0,(byte)20,(byte)39,(byte)59,(byte)78,(byte)98,(byte)118,(byte)137,(byte)157,(byte)177,(byte)196,(byte)216,(byte)235,(byte)255};
    private static final IndexColorModel icm =
        new IndexColorModel(4, 16, r, g, b);

    private void updateImage() {
        synchronized(this) {
            if(chart != null && width > 0 && height > 0) {
                BufferedImage buf;
                buf = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
                chart.paint(buf.createGraphics(), width, height);
                newImage(buf);
            } else {
                newImage(null);
            }
        }
    }

    /** Create a new component with an empty chart */
    public XChartArea() {
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
    public void newChart(XYChart chart) {
        synchronized(this) {
            this.chart = chart;
            updateImage();
        }
    }
}
