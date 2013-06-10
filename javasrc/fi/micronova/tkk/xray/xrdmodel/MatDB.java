package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import java.util.*;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.SAXException;

public class MatDB {
    public final List<SimpleMaterial> materials;
    public MatDB(File f, LookupTable lookup) throws IOException, ElementNotFound, ParserConfigurationException, SAXException {
        this(XMLUtil.parse(new FileInputStream(f)).getDocumentElement(), lookup);
    }
    public MatDB(Node n, LookupTable lookup) throws IOException, ElementNotFound {
        List<SimpleMaterial> mattmp = new ArrayList<SimpleMaterial>();
        for(Node n2: XMLUtil.getNamedChildElements(n, "mat")) {
            mattmp.add(new SimpleMaterial(n2, lookup));
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
