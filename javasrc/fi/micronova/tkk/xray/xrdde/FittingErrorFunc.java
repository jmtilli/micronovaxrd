package fi.micronova.tkk.xray.xrdde;
public interface FittingErrorFunc {
  public double getError(double[] meas, double[] simul);
};
