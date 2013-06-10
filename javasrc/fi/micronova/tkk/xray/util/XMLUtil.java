package fi.micronova.tkk.xray.util;
import org.w3c.dom.*;
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.SAXException;

public class XMLUtil {
    public static Document parse(InputStream is)
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory bf;
        DocumentBuilder b;
        Document d;
        bf = DocumentBuilderFactory.newInstance();
        b = bf.newDocumentBuilder();
        return b.parse(is);
    }
    public static Document newDocument()
        throws ParserConfigurationException
    {
        DocumentBuilderFactory bf;
        DocumentBuilder b;
        bf = DocumentBuilderFactory.newInstance();
        b = bf.newDocumentBuilder();
        return b.newDocument();
    }
    public static void unparse(Document doc, OutputStream os)
        throws TransformerConfigurationException, TransformerException
    {
        TransformerFactory tf;
        Transformer t;
        tf = TransformerFactory.newInstance();
        t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(os));
    }
    public static List<Node> getNamedChildElements(Node n, String name) {
        List<Node> result = new ArrayList<Node>();
        for(Node n2=n.getFirstChild(); n2!=null; n2=n2.getNextSibling()) {
            if(n2.getNodeType() == Node.ELEMENT_NODE &&
               n2.getNodeName().equals(name))
            {
                result.add(n2);
            }
        }
        return result;
    }
    public static List<Node> getChildElements(Node n) {
        List<Node> result = new ArrayList<Node>();
        for(Node n2=n.getFirstChild(); n2!=null; n2=n2.getNextSibling()) {
            if(n2.getNodeType() == Node.ELEMENT_NODE)
            {
                result.add(n2);
            }
        }
        return result;
    }
};
