package fi.micronova.tkk.xray.xrdmodel;
public interface LookupTable {
    public Atom lookup(int Z) throws ElementNotFound;
};
