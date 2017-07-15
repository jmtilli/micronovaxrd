package fi.micronova.tkk.xray.de;

public class AdvancedFitOptions {
  public double km;
  public double kr;
  public double pm;
  public double cr;
  public double lambda;
  public AdvancedFitOptions() {
    this.km = 0.7;
    this.kr = 0.5*(this.km + 1);
    this.pm = 0.5;
    this.cr = 0.5;
    this.lambda = 1.0;
  }
};
