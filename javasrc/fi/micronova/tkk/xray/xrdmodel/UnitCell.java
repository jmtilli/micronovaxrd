package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.util.*;
import java.util.*;
import fi.iki.jmtilli.javafastcomplex.Complex;
import fi.iki.jmtilli.javafastcomplex.ComplexUtils;
import fi.iki.jmtilli.javafastcomplex.ComplexBuffer;
import fi.iki.jmtilli.javaxmlfrag.*;

/* immutable */
public class UnitCell implements XMLRowable {
    public final double V;
    public final List<LatticeAtom> atoms;

    public boolean equals(Object o2) {
        if(o2 == null)
            return false;
        if(this == o2)
            return true;
        try {
            UnitCell c2 = (UnitCell)o2;
            return c2.V == V && c2.atoms.equals(atoms);
        }
        catch(ClassCastException ex) {
            return false;
        }
    }

    public int getNAtoms() {
        return atoms.size();
    }
    public double getV() {
        return V;
    }
    public UnitCell() {
        this(0.0);
    }
    public UnitCell(double V) {
        this.V = V;
        this.atoms = Collections.unmodifiableList(new ArrayList<LatticeAtom>());
    }
    public UnitCell(UnitCell cell) {
        this(cell.V, cell.atoms);
    }
    public UnitCell(double V, List<? extends LatticeAtom> atoms) {
        this.V = V;
        this.atoms = Collections.unmodifiableList(new ArrayList<LatticeAtom>(atoms));
    }
    public UnitCell distort(double V) {
        return new UnitCell(V, this.atoms);
    }
    public Complex suscFast(Miller h, double s, double lambda) throws UnsupportedWavelength { /* structure factor */
        ComplexBuffer sf = new ComplexBuffer();
        ComplexBuffer c = new ComplexBuffer();
        ComplexBuffer expterm = new ComplexBuffer();
        final double r_e = 2.817940325e-15;
        double T = -r_e*lambda*lambda/(Math.PI*V);
        for(LatticeAtom atom: atoms) {
            double asf = atom.atom.asf().calc(s);
            Complex hoenl = atom.atom.hoenl(lambda);
            c.set(asf).addInPlace(hoenl).multiplyInPlace(atom.occupation);
            double B = atom.atom.bFactor();
            expterm.set(0, -2*Math.PI).multiplyInPlace(atom.pos.dot(h));
            expterm.subtractInPlace(B*s*s).expInPlace();
            c.multiplyInPlace(expterm);
            sf.addInPlace(c);
        }
        sf.multiplyInPlace(T);
        return sf.get();
    }
    /* In theory, s could be calculated from m, but since we don't store
     * unit cell vectors, we have to pass s as an argument. */
    public Complex susc(Miller h, double s, double lambda) throws UnsupportedWavelength { /* structure factor */
        Complex sf = Complex.ZERO;
        final double r_e = 2.817940325e-15;
        double T = -r_e*lambda*lambda/(Math.PI*V);
        for(LatticeAtom atom: atoms) {
            double asf = atom.atom.asf().calc(s);
            Complex hoenl = atom.atom.hoenl(lambda);
            Complex c = ComplexUtils.multiply(atom.occupation, (ComplexUtils.add(asf,hoenl)));
            double B = atom.atom.bFactor();
            Complex expterm = ComplexUtils.exp(ComplexUtils.subtract(ComplexUtils.multiply(new Complex(0,-2*Math.PI),atom.pos.dot(h)),B*s*s));
            sf = ComplexUtils.add(sf, ComplexUtils.multiply(c, expterm));
        }
        return ComplexUtils.multiply(T,sf);
    }
    public UnitCell getProportion(double p) {
        List<LatticeAtom> atoms2 = new ArrayList<LatticeAtom>();
        for(LatticeAtom atom: atoms) {
            atoms2.add(new LatticeAtom(atom.atom, atom.occupation*p, atom.pos));
        }
        return new UnitCell(this.V, atoms2);
    }
    public UnitCell append(UnitCell unitcell2) {
        List<LatticeAtom> atoms = new ArrayList<LatticeAtom>();
        UnitCell result;

        for(LatticeAtom atom: this.atoms) {
            atoms.add(atom);
        }
        for(LatticeAtom atom: unitcell2.atoms) {
            atoms.add(atom);
        }
        return new UnitCell(this.V, atoms);
    }
    public UnitCell(DocumentFragment f, LookupTable table)
      throws ElementNotFound
    {
        List<LatticeAtom> atoms = new ArrayList<LatticeAtom>();
        V = f.getAttrDoubleNotNull("V");
        for(DocumentFragment f2: f.getMulti("atom")) {
            int Z;
            double occupation;
            Atom atom;
            Z = f2.getAttrIntNotNull("Z");
            occupation = f2.getAttrDoubleNotNull("occupation");
            atom = table.lookup(Z);
            for(DocumentFragment f3: f2.getMulti("pos")) {
                atoms.add(new LatticeAtom(atom, occupation,
                                          new AtomicPosition(f3)));
            }
        }
        this.atoms = Collections.unmodifiableList(atoms);
    }
    public void toXMLRow(DocumentFragment f) {
        f.setAttrDouble("V", this.V);
        for (LatticeAtom latticeatom: atoms) {
            DocumentFragment atom = f.add("atom");
            atom.setAttrInt("Z", latticeatom.atom.getZ());
            atom.setAttrDouble("occupation", latticeatom.occupation);
            atom.setRow("pos", latticeatom.pos);
        }
    }
};
