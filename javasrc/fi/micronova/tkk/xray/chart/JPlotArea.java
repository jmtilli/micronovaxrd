package fi.micronova.tkk.xray.chart;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/** Component for drawing an image */
public class JPlotArea extends JPanel {
    private volatile Image img;
    /** Creates a new component with an empty image */
    public JPlotArea() {
        this.img = null;
    }
    /** Changes the image
     * @param img The new image, which may be null
     */
    public void newImage(Image img) {
        this.img = img;
        repaint();
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image tmpImg = this.img;
        if(tmpImg != null)
            g.drawImage(tmpImg,0,0,null);
    }
}
