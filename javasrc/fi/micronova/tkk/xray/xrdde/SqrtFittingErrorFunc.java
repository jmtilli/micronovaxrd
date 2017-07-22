package fi.micronova.tkk.xray.xrdde;
public class SqrtFittingErrorFunc implements FittingErrorFunc {
  private int p;
  public SqrtFittingErrorFunc(int p)
  {
    if (p <= 0)
    {
      throw new IllegalArgumentException();
    }
    this.p = p;
  }
  public double getError(double[] meas, double[] simul)
  {
    double sum = 0;
    int count = 0;
    if (meas.length != simul.length)
    {
      throw new IllegalArgumentException();
    }
    if (p == 1)
    {
      for (int i=0; i<meas.length; i++)
      {
        double a,b;
        if (meas[i] < 0 || simul[i] < 0)
        {
          continue;
        }
        a = Math.sqrt(meas[i]);
        b = Math.sqrt(simul[i]);
        sum += Math.abs(a-b);
        count++;
      }
      return sum / count;
    }
    else if (p == 2)
    {
      for (int i=0; i<meas.length; i++)
      {
        double a,b,x;
        if (meas[i] < 0 || simul[i] < 0)
        {
          continue;
        }
        a = Math.sqrt(meas[i]);
        b = Math.sqrt(simul[i]);
        x=a-b;
        sum += x*x;
        count++;
      }
      sum = Math.sqrt(sum);
      return sum / Math.sqrt(count);
    }
    else
    {
      for (int i=0; i<meas.length; i++)
      {
        double a,b;
        if (meas[i] < 0 || simul[i] < 0)
        {
          continue;
        }
        a = Math.sqrt(meas[i]);
        b = Math.sqrt(simul[i]);
        sum += Math.exp(Math.log(Math.abs(a-b))*p);
        count++;
      }
      sum = Math.exp(Math.log(sum)*1.0/p);
      return sum / Math.exp(Math.log(count)*1.0/p);
    }
  }
  public static void main(String[] args)
  {
    /*
       g.pnorm=2;sqrtfitnessfunction([1,2],[3,0],g)
     */
    SqrtFittingErrorFunc func = new SqrtFittingErrorFunc(2);
    double[] meas = {1,2};
    double[] simul = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
