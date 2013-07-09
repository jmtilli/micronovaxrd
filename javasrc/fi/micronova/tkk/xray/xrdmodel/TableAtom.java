package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;
import fi.iki.jmtilli.javacomplex.Complex;
import fi.iki.jmtilli.javacomplex.ComplexUtils;

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
    private double last_wavelength;
    private Complex last_hoenl;
    public Complex hoenl(double lambda) throws UnsupportedWavelength {
        synchronized(this)
        {
            if(lambda == last_wavelength)
            {
                return last_hoenl;
            }
        }

        final double h = 4.13566743e-15; // in eV*s
        final double c = 299792458; // exact
        double E = h*c/lambda;
        Double E_d = new Double(E);
        Double E1, E2;
        double p;
        Complex fE1, fE2, f, result;
        SortedMap<Double,Complex> head, tail;

        head = f12map.headMap(E_d);
        tail = f12map.tailMap(E_d);
        if(head.isEmpty() || tail.isEmpty())
            throw new UnsupportedWavelength("Unsupported wavelength: "+lambda);
        E1 = head.lastKey();
        E2 = tail.firstKey();
        fE1 = f12map.get(E1);
        fE2 = f12map.get(E2);
        p = (E-E1)/(E2-E1);
        f = ComplexUtils.add(ComplexUtils.multiply(1-p, fE1), ComplexUtils.multiply(p, fE2));

        result = ComplexUtils.subtract(f,asfField.calc(0));
        synchronized(this)
        {
            last_wavelength = lambda;
            last_hoenl = result;
        }
        return result;
    }
};
