package fi.micronova.tkk.xray.chart;
import fi.micronova.tkk.xray.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import org.knowm.xchart.*;
import org.knowm.xchart.style.*;
import org.knowm.xchart.style.markers.*;




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
    public ChartFrame(final ChooserWrapper wrapper, String title, int w, int h, boolean legend, final DataArray xdata, String xtitle, final java.util.List<NamedArray> ydata, String ytitle, double ymin, double ymax, String legendFile)
    {
        super(title);
        Color[] colors = new Color[]{Color.RED, Color.BLUE};

        double[] xar = new double[xdata.array.length];

        XYChart xychart = new XYChartBuilder().width(w).height(h).title(title).xAxisTitle(xtitle).yAxisTitle(ytitle).build();

        xychart.getStyler().setChartBackgroundColor(UIManager.getColor("Panel.background"));
        xychart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        xychart.getStyler().setLegendLayout(Styler.LegendLayout.Horizontal);
        xychart.getStyler().setLegendPadding(3);
        xychart.getStyler().setDefaultSeriesRenderStyle(org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Line);
        xychart.getStyler().setYAxisLabelAlignment(Styler.TextAlignment.Right);
        //xychart.getStyler().setYAxisDecimalPattern("$ #,###.##");
        xychart.getStyler().setPlotMargin(0);
        xychart.getStyler().setPlotContentSize(.99);
        xychart.getStyler().setLegendVisible(legend);
        //xychart.getStyler().setLegendVisible(false);
        xychart.getStyler().setAntiAlias(false);

        int n = xdata.array.length;

        for (int i = 0; i < n; i++)
        {
            xar[i] = xdata.array[i]*xdata.scale;
        }

        for(int j=ydata.size()-1; j>=0; j--) {
            NamedArray y = ydata.get(j);
            double[] yar = new double[y.array.length];
            String name = y.name;
            for(int i=0; i<n; i++) {
                yar[i] = y.array[i]*y.scale;
            }
            if (name == null || name.equals(""))
            {
                name = "data";
            }
            org.knowm.xchart.XYSeries ser = xychart.addSeries(name, xar, yar);
            ser.setLineColor(colors[j]);
            ser.setLineWidth(1);
            ser.setMarker(new None());
        }


        if(ymin != ymax) {
            xychart.getStyler().setYAxisMin(ymin);
            xychart.getStyler().setYAxisMax(ymax);
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

        JPanel bpanel = new JPanel();
        bpanel.setLayout(new BorderLayout());
        XChartArea b = new XChartArea();
        b.newChart(xychart);
        b.setPreferredSize(new Dimension(w, h));
        bpanel.add(b, BorderLayout.CENTER);
        /*
        if (legendFile != null && legend)
        {
            bpanel.add(new JCenterImageArea(legendFile, 2), BorderLayout.SOUTH);
        }
        */
        cp.add(bpanel,c);
        ////cp.add(a,c);
        ////cp.add(new SwingWrapper<XYChart>(xychart).getXChartPanel(), c);
        //XChartPanel<XYChart> chartPanel = new XChartPanel<XYChart>(xychart);
        ////new SwingWrapper<XYChart>(xychart).displayChart();
        //cp.add(chartPanel, c);

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

                        FileOutputStream fstr = new FileOutputStream(file);
                        try {
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
                        finally {
                            fstr.close();
                        }
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
