package fi.micronova.tkk.xray.xrdmodel;
/* TODO: implement a real lookup table */
public class DummyLookupTable implements LookupTable {
    public Atom lookup(int Z) {
        return new DummyAtom(Z);
    }
};
