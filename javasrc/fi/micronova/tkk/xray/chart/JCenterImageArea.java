package fi.micronova.tkk.xray.chart;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.io.*;


/** Component for drawing an image */
public class JCenterImageArea extends JPanel {
    private BufferedImage img;
    private int border;
    /** Creates a new component with an empty image */
    public JCenterImageArea(String name, int border) {
        try {
            this.img = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream(name));
            this.border = border;
        }
        catch (IOException e)
        {
            throw new RuntimeException("cannot find image");
        }
    }
    public Dimension getPreferredSize()
    {
        return new Dimension(img.getWidth()+2*border, img.getHeight()+2*border);
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image tmpImg = this.img;
        Graphics2D g2d = (Graphics2D)g;
        int x = (this.getWidth() - tmpImg.getWidth(null)) / 2;
        int y = (this.getHeight() - tmpImg.getHeight(null)) / 2;
        g2d.drawImage(tmpImg, x, y, null);
    }
}
