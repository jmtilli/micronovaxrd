package fi.micronova.tkk.xray;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.chart.*;

/** An automatic implementation of Plotter.
 *
 * <p>
 *
 * This class is a fully-working implementation of Plotter, which gets
 * the simulation data by performing a simulation of a LayerStack. After
 * the layer stack is changed, a new simulation and plotting is performed
 * automatically. Redraw may be triggered manually if, for example, the
 * measurement data has changed.
 *
 */
public class LayerPlotter extends Plotter {
    private LayerStack stack;
    private volatile LayerStack tempStack;
    private GraphData data;

    /** Creates an automatic plotting thread
     * 
     * @param area The area to draw the chart to.
     * @param light A light, which changes from green to yellow when plotting and back to green again when the thread is idle. May be null if the functionality is not needed.
     * @param stack The layer stack used in simulation.
     * @param data Contains the both the angles of incidence and the measurement data.
     * @param green An image of a green light.
     * @param yellow An image of a yellow light.
     *
     */
    public LayerPlotter(JChartArea area, JPlotArea light, LayerStack stack, GraphData data, Image green, Image yellow, Image red, double dbMin, double dbMax) {
        super(area, light, green, yellow, red, dbMin, dbMax);
        this.stack = stack;
        this.data = data; /* we must be careful with thread safety */
        this.stack.addLayerModelListener(new LayerModelAdapter() {
            public void simulationChanged(EventObject ev) {
                layerChangeDraw();
            }
        });
        this.tempStack = this.stack.deepCopy();
        draw();
    }

    private void layerChangeDraw() {
        this.tempStack = this.stack.deepCopy();
        draw();
    }

    /** Redraw.
     * <p>
     * This must not be called if the layer stack has been modified since
     * redrawing is done automatically in that case. However, if measurement
     * data has changed, this method must be called.
     */
    public void draw() {
        super.draw();
    }

    /** Performs a simulation of a LayerStack to get simulation data.
     */
    protected GraphData getData() throws SimulationException {
        return data.simulate(this.tempStack).convertToDB(); /* data.simulate(...) is thread safe */
    }
}
