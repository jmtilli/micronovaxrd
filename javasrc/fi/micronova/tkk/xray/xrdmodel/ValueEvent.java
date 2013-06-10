package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
public class ValueEvent extends EventObject {
    public final FitValue val;
    public ValueEvent(FitValue val) {
        super(val);
        this.val = val;
    }
}
