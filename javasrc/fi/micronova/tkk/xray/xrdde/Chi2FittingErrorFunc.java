package fi.micronova.tkk.xray.xrdde;
public class Chi2FittingErrorFunc implements FittingErrorFunc {
  private int p;
  public Chi2FittingErrorFunc(int p)
  {
    if (p <= 0)
    {
      throw new IllegalArgumentException();
    }
    this.p = p;
  }
  public double getError(double[] meas, double[] simul)
  {
    double E = 0;
    int count = 0;
    if (meas.length != simul.length)
    {
      throw new IllegalArgumentException();
    }
    for (int i=0; i<meas.length; i++)
    {
      if (meas[i] <= 0 || simul[i] <= 0)
      {
        continue;
      }
      E += (meas[i]-simul[i])*(meas[i]-simul[i])/meas[i];
      count++;
    }
    return Math.sqrt(E/count);
  }
  public static void main(String[] args)
  {
    /*
       chi2fitnessfunction([3,0],[1,2])
     */
    Chi2FittingErrorFunc func = new Chi2FittingErrorFunc(2);
    double[] meas = {1,2};
    double[] simul = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
