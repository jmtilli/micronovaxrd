package fi.micronova.tkk.xray.de;

public class AdvancedFitOptions {
  double km;
  double kr;
  double pm;
  double cr;
  double lambda;
  public AdvancedFitOptions() {
    this.km = 0.7;
    this.kr = 0.5*(this.km + 1);
    this.pm = 0.5;
    this.cr = 0.5;
    this.lambda = 1.0;
  }
};
