package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import fi.micronova.tkk.xray.complex.*;

class TableAtom implements Atom {
    public final int Z;
    public final double mass;
    public final double B;
    public final ASF asfField;
    public final SortedMap<Double,Complex> f12map;

    public TableAtom(int Z, double mass, double B, ASF asfField, SortedMap<Double,Complex> f12map) {
        this.Z = Z;
        this.mass = mass;
        this.B = B;
        this.asfField = asfField;
        this.f12map = Collections.unmodifiableSortedMap(new TreeMap<Double,Complex>(f12map));
    }

    public int getZ() {
        return Z;
    }
    public double bFactor() {
        return B;
    }
    public double getMass() {
        return mass;
    }
    public ASF asf() {
        return asfField;
    }
    public Complex hoenl(double lambda) throws UnsupportedWavelength {
        final double h = 4.13566743e-15; // in eV*s
        final double c = 299792458; // exact
        double E = h*c/lambda;
        double E1, E2;
        double p;
        Complex fE1, fE2, f;
        SortedMap<Double,Complex> head, tail;

        head = f12map.headMap(E);
        tail = f12map.tailMap(E);
        if(head.isEmpty() || tail.isEmpty())
            throw new UnsupportedWavelength("Unsupported wavelength: "+lambda);
        E1 = head.lastKey();
        E2 = tail.firstKey();
        fE1 = f12map.get(E1);
        fE2 = f12map.get(E2);
        p = (E-E1)/(E2-E1);
        f = Complex.add(Complex.mul(1-p, fE1), Complex.mul(p, fE2));

        return Complex.sub(f,asfField.calc(0));
    }
};
