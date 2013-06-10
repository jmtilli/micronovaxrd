package fi.micronova.tkk.xray.chart;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import fi.micronova.tkk.xray.util.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;




/** Dialog for showing multiple datasets in a chart and saving them to an ASCII
 * file.
 *
 * This class contains the common code for showing data in a chart dialog. The
 * data can be scaled to the desired units. The class also contains
 * functionality to save the data to a text file (without the same unit
 * conversion as when showing the data on a chart).
 *
 * The dialog is not modal, so the program can be used without having to close
 * the dialog.
 */

/* All arrays must be of the same length. There must be at least 1 array in ydata. */
public class ChartFrame extends JFrame {
    /**
     * All the arrays in xdata and ydata must be of the same length.
     *
     * @param wrapper a wrapper for showing the save dialog
     * @param title the title of the chart
     * @param w initial width
     * @param h initial height
     * @param legend if true, legend for each dataset is shown
     * @param xdata the common x coordinates for all the datasets
     * @param xtitle x-axis title
     * @param ydata the y coordinates for different datasets, and the names of the datasets
     * @param ytitle y-axis title
     * @param ymin minimum value of y-axis. this is ignored if ymin == ymax
     * @param ymax maximum value of y-axis. this is ignored if ymin == ymax
     */
    public ChartFrame(final ChooserWrapper wrapper, String title, int w, int h, boolean legend, final DataArray xdata, String xtitle, final java.util.List<NamedArray> ydata, String ytitle, double ymin, double ymax)
    {
        super(title);

        XYSeries series;
        XYSeriesCollection dataset;
        XYPlot xyplot;
        JFreeChart chart;
        int n = xdata.array.length;

        dataset = new XYSeriesCollection();
        for(int j=0; j<ydata.size(); j++) {
            NamedArray y = ydata.get(j);
            series = new XYSeries(y.name);
            for(int i=0; i<n; i++) {
                series.add(xdata.scale*xdata.array[i], y.scale*y.array[i]);
            }
            dataset.addSeries(series);
        }


        chart = ChartFactory.createXYLineChart(title,xtitle,ytitle,dataset,PlotOrientation.VERTICAL,legend,true,false);
        chart.setAntiAlias(false); 

        xyplot = chart.getXYPlot();
        if(ymin != ymax) {
            xyplot.getRangeAxis().setAutoRange(false);
            xyplot.getRangeAxis().setRange(ymin,ymax);
        }

        Container cp;

        cp = getContentPane();

        GridBagConstraints c = new GridBagConstraints();
        cp.setLayout(new GridBagLayout());
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1;
        c.ipadx = c.ipady = 2;
        c.insets = new Insets(2, 2, 2, 2);

        JChartArea a = new JChartArea();
        a.newChart(chart);
        a.setPreferredSize(new Dimension(w, h));
        cp.add(a,c);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        JPanel btnPanel = new JPanel();
        JButton btn;
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
                dispose();
            }
        });
        btnPanel.add(btn);
        cp.setPreferredSize(cp.getLayout().preferredLayoutSize(cp));
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        btnPanel.setMinimumSize(btnPanel.getPreferredSize());

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem fileExport = new JMenuItem("Export...");
        JMenuItem fileClose = new JMenuItem("Close");
        fileMenu.add(fileExport);
        fileMenu.add(fileClose);
        menu.add(fileMenu);


        fileClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
                dispose();
            }
        });
        final JFrame thisFrame = this;
        fileExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                File file = wrapper.showFileDialog(thisFrame,true);
                if(file != null) {
                    try {
                        double[][] cols;
                        cols = new double[ydata.size()+1][];
                        cols[0] = xdata.array;
                        for(int i=0; i<ydata.size(); i++)
                          cols[i+1] = ydata.get(i).array;

                        OutputStream fstr = new FileOutputStream(file);
                        Writer rw = new OutputStreamWriter(fstr);
                        BufferedWriter bw = new BufferedWriter(rw);
                        PrintWriter w = new PrintWriter(bw);
                        for(int i=0; i<cols[0].length; i++) {
                            for(int j=0; j<cols.length; j++) {
                                w.print(cols[j][i]);
                                if(j != cols.length-1)
                                    w.print(" ");
                            }
                            w.println();
                        }
                        if(w.checkError())
                          throw new IOException();
                    }
                    catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        setJMenuBar(menu);


        c.weighty = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        cp.add(btn,c);
        pack();
    }

    /** Show the window.
     */
    public void call() {
        setVisible(true);
    }



}
