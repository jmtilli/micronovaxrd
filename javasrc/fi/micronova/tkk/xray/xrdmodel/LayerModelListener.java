package fi.micronova.tkk.xray.xrdmodel;
import javax.swing.event.*;
import java.util.*;

public interface LayerModelListener {
    /* These are for changes which are due to adding/removing layers. */
    public void layersChanged(ListDataEvent ev);
    public void layersAdded(ListDataEvent ev);
    public void layersRemoved(ListDataEvent ev);
    public void modelPropertyChanged(EventObject ev);

    /* Always called when something happens which might affect
     * the simulation. */
    public void simulationChanged(EventObject ev);
};
