package fi.micronova.tkk.xray.xrdmodel;
import fi.micronova.tkk.xray.complex.*;
public class Susceptibilities {
    public final Complex chi_0;
    public final Complex chi_h;
    public final Complex chi_h_neg;
    public Susceptibilities(Complex chi_0, Complex chi_h, Complex chi_h_neg) {
        this.chi_0 = chi_0;
        this.chi_h = chi_h;
        this.chi_h_neg = chi_h_neg;
    }
    public String toString() {
        return "Susceptibilities(" + chi_0 + ", " + chi_h + ", " + chi_h_neg+")";
    }
};
