package fi.micronova.tkk.xray;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.chart.*;

import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;




/** Plotting thread.
 *
 * <p>
 *
 * This class implements all the actual plotting code. It plots both
 * measurement and simulation data in a logarithmic scale from -70 dB to 0 dB.
 * This is an abstract class; the source of the data is not specified.
 *
 * <p>
 *
 * LayerPlotter is a working implementation of this abstract class that gets
 * the simulation data from a simulation of LayerStack and plots it with the
 * specified measurement data. See the documentation of LayerPlotter for more
 * information.
 *
 */

abstract public class Plotter {
    private Thread t;
    private JChartArea area;
    private JPlotArea light;
    private Image green, yellow, red;
    private boolean cont,closing;
    private volatile String additionalTitle;
    private double dbMin, dbMax; /* default plot range */

    /** Constructor.
     *
     * <p>
     *
     * Creates a plotting thread which can be asked to draw a chart in the
     * specified chart area. The plotting thread starts as idle. When idle,
     * it waits for new requests. After getting a request, it draws a chart
     * and waits for new requests when the drawing is completed.
     *
     * @param area The area to draw the chart to.
     * @param light A light, which changes from green to yellow when plotting and back to green again when the thread is idle. May be null if the functionality is not needed.
     * @param green An image of a green light.
     * @param yellow An image of a yellow light.
     */
    public Plotter(JChartArea area, JPlotArea light, Image green, Image yellow, Image red, double dbMin, double dbMax) {
        this.area = area;
        this.light = light;
        this.green = green;
        this.yellow = yellow;
        this.red = red;
        this.additionalTitle = "";
        this.dbMin = dbMin;
        this.dbMax = dbMax;
        t = new Thread(new Runnable() {
            public void run() {
                runThread();
            }
        });
        cont = false;
        closing = false;
        t.start();
    }

    /** Set the plotting range.
     *
     * @param dbMin minimum intensity in decibels
     * @param dbMax maximum intensity in decibels
     */
    public void setDbRange(double dbMin, double dbMax) {
        assert(dbMax >= dbMin);
        this.dbMin = dbMin;
        this.dbMax = dbMax;
        draw();
    }
    /** Get the minimum of plotting range.
     *
     * @return minimum intensity in decibels
     */
    public double getDbMin() {
      return this.dbMin;
    }
    /** Get the maximum of plotting range.
     *
     * @return maximum intensity in decibels
     */
    public double getDbMax() {
      return this.dbMax;
    }

    /** Changes the additional title of the plot.
     *
     * <p>
     *
     * Does not signal a redraw. Redrawing must be signaled separately if
     * needed.
     *
     * @param s The new additional title. Must not be null.
     */
    public void setAdditionalTitle(String s) {
        assert(s != null);
        additionalTitle = s;
    }

    /** A method to get the data.
     *
     * <p>
     *
     * Working implementation classes must implement this method.
     *
     * @return The measurement and simulation data to plot
     */
    abstract protected GraphData getData() throws SimulationException;



    /** Redraws the chart.
     *
     * <p>
     *
     * Implementation classes may redeclare this as public if needed.
     */
    protected void draw() {
        synchronized(this) {
            cont = true;
            notify();
        }
    }

    /** Stops the fitting thread.
     *
     * <p>
     *
     * After stopping the fitting thread may not be started again without
     * creating a new plotter.
     */
    public void close() {
        boolean ok = false;
        synchronized(this) {
            closing = true;
            notify();
        }
        while(!ok) {
            try {
                t.join();
                ok = true;
            }
            catch(InterruptedException e) {}
        }
    }

    /* The real plotting code */
    private void doPlot(GraphData data) {
        if(data == null) {
            area.newChart(null);
            return;
        }

        XYSeries series1 = new XYSeries("Simulated data");
        XYSeries series2 = new XYSeries("Measured data");
        XYSeriesCollection dataset;
        XYPlot xyplot;
        JFreeChart chart;

        assert(data.alpha_0.length == data.meas.length);
        assert(data.alpha_0.length == data.simul.length);
        for(int i=0; i<data.alpha_0.length; i++) {
            series1.add(data.alpha_0[i],data.simul[i]);
            series2.add(data.alpha_0[i],data.meas[i]);
        }
        dataset = new XYSeriesCollection(series1);
        dataset.addSeries(series2);

        chart = ChartFactory.createXYLineChart("XRD "+additionalTitle,"degrees","dB",dataset,PlotOrientation.VERTICAL,true,true,false);
        xyplot = chart.getXYPlot();
        /*xyplot.getDomainAxis().setAutoRange(false);
        xyplot.getDomainAxis().setRange(0,5);*/
        xyplot.getRangeAxis().setAutoRange(false);
        xyplot.getRangeAxis().setRange(dbMin,dbMax);
        chart.setAntiAlias(false); /* this is faster */
        area.newChart(chart);
        if(light != null)
            light.newImage(green);
    }

    /* This methods runs in another thread.
     * It acquires the following locks:
     * - this, when a new set of variables is loaded
     * - other locks acquired by getData
     */
    private void runThread() {
        try {
            for(;;) {
                GraphData tempData = null;

                synchronized(this) {
                    while(!cont && !closing) {
                        wait();
                    }
                    if(closing)
                        break;
                    assert(cont);
                    cont = false;
                    if(light != null)
                        light.newImage(yellow);
                    try {
                        tempData = getData().convertToDB(); /* this is a logarithmic plot */
                    }
                    catch(SimulationException ex) {
                        if(light != null)
                            light.newImage(red);
                    }
                    catch(Exception ex) {
                        if(light != null)
                            light.newImage(red);
                        ex.printStackTrace();
                    }
                }
                doPlot(tempData);
            }
        }
        catch(InterruptedException ex) {}
    }
}
