package fi.micronova.tkk.xray.xrdmodel;
import java.util.*;

public class ASF {
    public final List<ASFGaussian> gaussians;
    public final double c;
    public ASF(List<ASFGaussian> gaussians, double c) {
        this.gaussians = Collections.unmodifiableList(new ArrayList<ASFGaussian>(gaussians));
        this.c = c;
    }
    public double calc(double s) { /* in m^(-2) */
        double asf = c;
        for(ASFGaussian gaussian: gaussians) {
            asf += gaussian.a*Math.exp(-gaussian.b*s*s);
        }
        return asf;
    }
};
