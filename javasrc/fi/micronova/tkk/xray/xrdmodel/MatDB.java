package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;
import fi.iki.jmtilli.javaxmlfrag.*;

public class MatDB {
    public final List<SimpleMaterial> materials;
    public MatDB(File f, LookupTable lookup) throws IOException, ElementNotFound, ParserConfigurationException, SAXException {
        this(parseClose(f), lookup);
    }
    private static DocumentFragment parseClose(File f) throws IOException, ParserConfigurationException, SAXException
    {
        FileInputStream fstr = new FileInputStream(f);
        try {
            DocumentFragment frag = DocumentFragmentHandler.parseWhole(fstr);
            return frag;
        }
        finally {
            fstr.close();
        }
    }

    public MatDB(DocumentFragment f, LookupTable lookup)
      throws IOException, ElementNotFound
    {
        List<SimpleMaterial> mattmp = new ArrayList<SimpleMaterial>();
        for(DocumentFragment f2: f.getMulti("mat")) {
            mattmp.add(new SimpleMaterial(f2, lookup));
        }
        materials = Collections.unmodifiableList(mattmp);
    }
    public static void main(String[] args) {
        try {
            LookupTable lookup = SFTables.defaultLookup();
            MatDB db = new MatDB(new File("matdb.xml"),lookup);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
