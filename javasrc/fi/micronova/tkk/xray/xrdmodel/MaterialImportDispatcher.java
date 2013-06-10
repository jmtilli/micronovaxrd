package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import org.w3c.dom.*;

public class MaterialImportDispatcher {
    private static interface Importer {
        public Material doImport(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException;
    }
    private static Map<String,Importer> map;
    static {
        map = new HashMap<String,Importer>();
        map.put("mixture", new Importer() {public Material doImport(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {return new Mixture(n,table);}});
        map.put("mat", new Importer() {public Material doImport(Node n, LookupTable table) throws ElementNotFound {return new SimpleMaterial(n,table);}});
    }
    public static Material doImport(Node n, LookupTable table) throws ElementNotFound, InvalidMixtureException {
        Importer imp;
        Material result;
        imp = map.get(n.getNodeName());
        if(imp != null)
            result = imp.doImport(n,table);
        else
            result = null;
        return result;
    }
};
