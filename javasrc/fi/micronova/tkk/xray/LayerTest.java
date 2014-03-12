package fi.micronova.tkk.xray;
import java.awt.*;
import java.io.*;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.iki.jmtilli.javaxmlfrag.*;


public class LayerTest {
    public static void main(String[] args) {
        try {
            double theta_Bs = 0.60327;
            double theta_min = theta_Bs - 3600*Math.PI/180/3600;
            double theta_max = theta_Bs + 1800*Math.PI/180/3600;
            double[] theta = new double[2000];
            double[] curve;
            for(int i=0; i<theta.length; i++) {
                theta[i] = theta_min + i*(theta_max - theta_min)/(theta.length-1);
            }
            LookupTable table = SFTables.defaultLookup();
            MatDB db = new MatDB(new File("matdb.xml"),table);
            Material si = MaterialImportDispatcher.doImport(
                DocumentFragmentHandler.parseWhole(new FileInputStream(new File("Silicon-400.xml"))),table);
            LayerStack layers = new LayerStack(DocumentFragmentHandler.parseWhole(new FileInputStream(new File("layers.xml"))),table);

            new LayerDialog((Frame)null, layers.getElementAt(0), db, layers.getLambda()).call();

            DocumentFragment doc = new DocumentFragment("model");
            doc.setThisRow(layers);
            doc.unparse(XMLDocumentType.WHOLE, System.out);
        }
        catch(Throwable ex) {
            ex.printStackTrace();
        }
    }

}
