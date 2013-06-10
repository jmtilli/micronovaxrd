package fi.micronova.tkk.xray.util;

import java.awt.*;

public class DialogUtil {
    public static Dialog getOwnerDialog(Component c) {
        while(!(c instanceof Dialog) && c != null)
            c = c.getParent();
        return (Dialog)c;
    }
    public static Frame getOwnerFrame(Component c) {
        while(!(c instanceof Frame) && c != null)
            c = c.getParent();
        return (Frame)c;
    }
}
