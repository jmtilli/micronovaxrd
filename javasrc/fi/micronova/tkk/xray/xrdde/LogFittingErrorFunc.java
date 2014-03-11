package fi.micronova.tkk.xray.xrdde;
public class LogFittingErrorFunc implements FittingErrorFunc {
  private int p;
  public LogFittingErrorFunc(int p)
  {
    if (p <= 0)
    {
      throw new IllegalArgumentException();
    }
    this.p = p;
  }
  public double pnorm(double[] x, int p)
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
      if (meas[i] <= 0 || simul[i] <= 0)
      {
        continue;
      }
      a = 10*Math.log(meas[i])/Math.log(10);
      b = 10*Math.log(simul[i])/Math.log(10);
      x[i] = a-b;
      count++;
    }
    return pnorm(x, p) / Math.exp(Math.log(count)*1.0/p);
  }
  public static void main(String[] args)
  {
    /*
       g.pnorm=2;logfitnessfunction([1,2],[3,0],g)
     */
    LogFittingErrorFunc func = new LogFittingErrorFunc(2);
    double[] meas = {1,2};
    double[] simul = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
