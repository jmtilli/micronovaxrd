package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
public class LayerEvent extends EventObject {
    public final Layer layer;
    public LayerEvent(Layer layer) {
        super(layer);
        this.layer = layer;
    }
}
