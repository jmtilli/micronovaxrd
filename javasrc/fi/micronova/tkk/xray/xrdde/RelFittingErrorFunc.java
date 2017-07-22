package fi.micronova.tkk.xray.xrdde;
public class RelFittingErrorFunc implements FittingErrorFunc {
  public RelFittingErrorFunc()
  {
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
      E += (meas[i]-simul[i])*(meas[i]-simul[i])/(meas[i]*meas[i]);
      count++;
    }
    return Math.sqrt(E*1.0/count);
  }
  public static void main(String[] args)
  {
    /*
       relfitnessfunction([1,2],[3,0],g)
     */
    RelFittingErrorFunc func = new RelFittingErrorFunc();
    double[] simul = {1,2};
    double[] meas = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
