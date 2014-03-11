package fi.micronova.tkk.xray.xrdde;
public class RelChi2FittingErrorFunc implements FittingErrorFunc {
  private double threshold;
  public RelChi2FittingErrorFunc(double threshold)
  {
    if (threshold < 0)
    {
      throw new IllegalArgumentException();
    }
    this.threshold = threshold;
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
      if (meas[i] < threshold)
      {
        E += (meas[i]-simul[i])*(meas[i]-simul[i])/meas[i];
      }
      else
      {
        E += (meas[i]-simul[i])*(meas[i]-simul[i])/(meas[i]*meas[i]/threshold);
      }
      count++;
    }
    return Math.sqrt(E*1.0/count);
  }
  public static void main(String[] args)
  {
    /*
       g.threshold=2;relchi2fitnessfunction([1,2],[3,0],g)
     */
    RelChi2FittingErrorFunc func = new RelChi2FittingErrorFunc(2);
    double[] simul = {1,2};
    double[] meas = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
