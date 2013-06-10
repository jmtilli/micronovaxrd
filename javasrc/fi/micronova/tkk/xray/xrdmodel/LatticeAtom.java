package fi.micronova.tkk.xray.xrdmodel;
/* immutable */
public class LatticeAtom {
    public final Atom atom;
    public final double occupation;
    public final AtomicPosition pos;
    public LatticeAtom(Atom atom, double occupation, AtomicPosition pos) {
        this.atom = atom;
        this.occupation = occupation;
        this.pos = pos;
    }

    public boolean equals(Object o2) {
        if(o2 == null)
            return false;
        if(this == o2)
            return true;
        try {
            LatticeAtom a2 = (LatticeAtom)o2;
            return a2.atom.equals(atom) &&
                a2.occupation == occupation &&
                a2.pos.equals(pos);
        }
        catch(ClassCastException ex) {
            return false;
        }
    }
};
