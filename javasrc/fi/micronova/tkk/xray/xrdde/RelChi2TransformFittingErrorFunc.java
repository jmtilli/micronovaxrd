package fi.micronova.tkk.xray.xrdde;

public class RelChi2TransformFittingErrorFunc implements FittingErrorFunc {
  private double threshold;
  private double A;
  private double B;
  private int p;
  public RelChi2TransformFittingErrorFunc(double threshold, int p)
  {
    if (threshold <= 0)
    {
      throw new IllegalArgumentException();
    }
    this.threshold = threshold;
    this.A = Math.sqrt(threshold)/2;
    this.B = Math.sqrt(threshold)-A*Math.log(threshold);
    this.p = p;
  }
  public double transform(double datapoint)
  {
    if (datapoint <= threshold)
    {
      return Math.sqrt(datapoint);
    }
    else
    {
      return A*Math.log(datapoint)+B;
    }
  }
  private static double pnorm(double[] x, int p)
  {
    double sum = 0;
    if (p <= 0)
    {
      throw new IllegalArgumentException();
    }
    for (int i=0; i<x.length; i++)
    {
      sum += Math.exp(Math.log(Math.abs(x[i]))*p);
    }
    sum = Math.exp(Math.log(sum)*1.0/p);
    return sum;
  }
  public double getError(double[] meas, double[] simul)
  {
    double E = 0;
    double[] x = new double[meas.length];
    int count = 0;
    if (meas.length != simul.length)
    {
      throw new IllegalArgumentException();
    }
    for (int i=0; i<meas.length; i++)
    {
      double a,b;
      a = transform(meas[i]);
      b = transform(simul[i]);
      x[i] = a - b;
      count++;
    }
    return pnorm(x, p) / Math.exp(Math.log(count)*1.0/p);
  }
  public static void main(String[] args)
  {
    /*
       g.threshold=2;relchi2fitnessfunction([1,2],[3,0],g)
     */
    RelChi2TransformFittingErrorFunc func = new RelChi2TransformFittingErrorFunc(2, 2);
    double[] simul = {1,2};
    double[] meas = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
