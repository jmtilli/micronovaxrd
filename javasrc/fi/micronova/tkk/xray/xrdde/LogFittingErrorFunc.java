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
        if (meas[i] <= 0 || simul[i] <= 0)
        {
          continue;
        }
        a = 10*Math.log(meas[i])/Math.log(10);
        b = 10*Math.log(simul[i])/Math.log(10);
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
        if (meas[i] <= 0 || simul[i] <= 0)
        {
          continue;
        }
        a = 10*Math.log(meas[i])/Math.log(10);
        b = 10*Math.log(simul[i])/Math.log(10);
        x = a-b;
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
        if (meas[i] <= 0 || simul[i] <= 0)
        {
          continue;
        }
        a = 10*Math.log(meas[i])/Math.log(10);
        b = 10*Math.log(simul[i])/Math.log(10);
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
       g.pnorm=2;logfitnessfunction([1,2],[3,0],g)
     */
    LogFittingErrorFunc func = new LogFittingErrorFunc(2);
    double[] meas = {1,2};
    double[] simul = {3,0};
    System.out.println(func.getError(meas, simul));
  }
};
