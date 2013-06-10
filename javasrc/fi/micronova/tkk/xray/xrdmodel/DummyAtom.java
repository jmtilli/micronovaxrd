package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import fi.micronova.tkk.xray.complex.*;

class DummyAtom implements Atom {
    private final int Z;
    public DummyAtom(int Z) {
        this.Z = Z;
    }
    public int getZ() {
        return Z;
    }
    public double getMass() {
        return Z;
    }
    public double bFactor() {
        return Z; /* XXX */
    }
    public ASF asf() {
        ASFGaussian g = new ASFGaussian(0,0);
        List<ASFGaussian> l = new ArrayList<ASFGaussian>();
        l.add(g);
        return new ASF(l, Z); /* XXX */
    }
    public Complex hoenl(double lambda) {
        return new Complex(Z,lambda); /* XXX */
    }
};
