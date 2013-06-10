package fi.micronova.tkk.xray.util;
import javax.swing.*;
import java.io.*;
public interface ChooserWrapper {
    public File showFileDialog(JFrame owner, boolean save);
};

