package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import fi.iki.jmtilli.javaxmlfrag.*;

public class MaterialImportDispatcher {
    private static interface Importer {
        public Material doImport(DocumentFragment f, LookupTable table)
          throws ElementNotFound, InvalidMixtureException;
    }
    private static Map<String,Importer> map;
    static {
        map = new HashMap<String,Importer>();
        map.put("mixture", new Importer() {
            public Material doImport(DocumentFragment f, LookupTable table)
              throws ElementNotFound, InvalidMixtureException
            {
                return new Mixture(f,table);
            }
        });
        map.put("mat", new Importer() {
            public Material doImport(DocumentFragment f, LookupTable table)
              throws ElementNotFound
            {
                return new SimpleMaterial(f,table);
            }
        });
    }
    public static Material doImport(DocumentFragment f, LookupTable table)
      throws ElementNotFound, InvalidMixtureException
    {
        Importer imp;
        Material result;
        imp = map.get(f.getTag());
        if (imp == null)
        {
            return null;
        }
        result = imp.doImport(f,table);
        return result;
    }
};
