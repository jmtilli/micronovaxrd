package fi.micronova.tkk.xray.complex;
/* Beware! This is slow! */
/* TODO: finish */
/* XXX: infinity and NaN */
import java.util.*;

public class Complex {
    public final double real;
    public final double imag;
    public Complex(double real, double imag) {
        this.real = real;
        this.imag = imag;
    }
    public Complex(double real) {
        this(real, 0);
    }
    public Complex() {
        this(0, 0);
    }
    public Complex(Complex c) {
        this(c.real, c.imag);
    }
    public static Complex add(Complex v1, Complex v2) {
        return new Complex(v1.real+v2.real, v1.imag+v2.imag);
    }
    public static Complex sub(Complex v1, Complex v2) {
        return new Complex(v1.real-v2.real, v1.imag-v2.imag);
    }
    public static Complex mul(Complex v1, Complex v2) {
        return new Complex(v1.real*v2.real-v1.imag*v2.imag, v1.real*v2.imag+v2.real*v1.imag);
    }
    public static Complex add(Complex c, double d) {
        return new Complex(c.real+d, c.imag);
    }
    public static Complex sub(Complex c, double d) {
        return new Complex(c.real-d, c.imag);
    }
    public static Complex sub(double d, Complex c) {
        return new Complex(d-c.real, -c.imag);
    }
    public static Complex add(double d, Complex c) {
        return Complex.add(c,d);
    }
    public static Complex mul(Complex c, double d) {
        return new Complex(c.real*d, c.imag*d);
    }
    public static Complex mul(double d, Complex c) {
        return Complex.mul(c,d);
    }
    public static double abs(Complex c) {
        return Math.hypot(c.real, c.imag);
    }
    public static double angle(Complex c) {
        return Math.atan2(c.imag, c.real);
    }
    public static Complex conj(Complex c) {
        return new Complex(c.real, -c.imag);
    }
    public static Complex inv(Complex v1) {
        double f = 1/(v1.real*v1.real + v1.imag*v1.imag);
        return new Complex(f*v1.real, -f*v1.imag);
    }

    /* XXX: bad algorithm */
    public static Complex div(Complex v1, Complex v2) {
        double f = 1/(v2.real*v2.real + v2.imag*v2.imag);
        /* TODO: check that this works */
        //assert(false);
        return new Complex(f*(v1.real*v2.real+v1.imag*v2.imag), f*(v1.imag*v2.real - v2.imag*v1.real));
    }
    public static Complex div(Complex c, double d) {
        return new Complex(c.real/d, c.imag/d);
    }
    public int hashCode() {
        return new Double(real).hashCode() ^ new Double(imag).hashCode();
    }
    public boolean equals(Object o2) {
        if(o2 == null)
            return false;
        if(this == o2)
            return true;
        try {
            Complex c2 = (Complex)o2;
            return real == c2.real && imag == c2.imag;
        }
        catch(ClassCastException ex) {
            return false;
        }
    }
    public static Complex exp(Complex c) {
        double e = Math.exp(c.real);
        return new Complex(e*Math.cos(c.imag),e*Math.sin(c.imag));
    }
    /* XXX: is this the principal branch? */
    public static Complex log(Complex c) {
        return new Complex(Math.log(Complex.abs(c)), Complex.angle(c));
    }
    /* XXX: is this the principal branch? */
    public static Complex sqrt(Complex c) {
        double f=Math.sqrt(Complex.abs(c));
        double theta=Complex.angle(c)/2;
        return new Complex(f*Math.cos(theta),f*Math.sin(theta));
    }
    public static Complex sum(Collection<Complex> it) {
        double r = 0, i = 0;
        for(Complex c: it) {
            r += c.real;
            i += c.imag;
        }
        return new Complex(r,i);
    }
    public String toString() {
        double real = this.real;
        double imag = this.imag;

        /* avoid negative zero */
        if(real == 0.0)
            real = 0.0;
        if(imag == 0.0)
            imag = 0.0;

        if(imag >= 0)
            return real + " + " + imag + "i";
        else
            return real + " - " + (-imag) + "i";
    }
};
