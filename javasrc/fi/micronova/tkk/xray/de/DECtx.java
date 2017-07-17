package fi.micronova.tkk.xray.de;
import Jama.Matrix;
import java.util.*;
import java.util.concurrent.*;
public class DECtx {
  public static interface CostFunc {
    public double calculate(double[] p) throws Exception;
  };
  private double km;
  private double kr;
  private double cr;
  private double pm;
  private double lambda;
  private int cycle;
  private CostFunc func;
  private boolean cov_on;
  private boolean traditional_recombination_on;
  private int nparam;
  private int npop;
  private double[] p_min;
  private double[] p_max;
  private double[] dp;
  private PopulationIndividual[] pop;
  private ExecutorService executor_service;
  private boolean reportPerf;

  public boolean reportPerf()
  {
    return reportPerf;
  }


  public static double columnMean(Matrix m, int column)
  {
    double d = 0.0;
    double multiplier = 1.0/(m.getRowDimension());
    for (int i = 0; i < m.getRowDimension(); i++)
    {
      d += multiplier * m.get(i, column);
    }
    return d;
  }
  public static Matrix cov(Matrix m)
  {
    Matrix m2 = m.copy();
    for (int i = 0; i < m.getColumnDimension(); i++)
    {
      double mean = columnMean(m, i);
      for (int j = 0; j < m.getRowDimension(); j++)
      {
        m2.set(j, i, m2.get(j, i)-mean);
      }
    }
    Matrix res = m2.transpose().times(m2).times(1.0/(m.getRowDimension()-1));
    // ensure the matrix is symmetric:
    for (int i = 0; i < res.getRowDimension(); i++)
    {
      for (int j = i+1; j < res.getColumnDimension(); j++)
      {
        res.set(i, j, res.get(j, i));
      }
    }
    return res;
  }


  private class PopulationIndividual
    implements Callable<Void>, Comparable<PopulationIndividual>
  {
    private double[] p;
    private double E;
    public Void call() throws Exception
    {
      this.E = func.calculate(this.p);
      return null;
    }
    public PopulationIndividual deepCopy()
    {
      PopulationIndividual i = new PopulationIndividual(p);
      i.E = this.E;
      return i;
    }
    private PopulationIndividual(double[] p)
    {
      this.p = new double[p.length];
      for (int i=0; i<p.length; i++)
      {
        this.p[i] = p[i];
      }
    }
    private PopulationIndividual()
    {
      this.p = new double[nparam];
      for (int j=0; j<nparam; j++)
      {
        this.p[j] = p_min[j] + dp[j]*Math.random();
      }
    }
    private int doubleCmp(double a, double b)
    {
      if (a > b)
      {
        return 1;
      }
      if (a < b)
      {
        return -1;
      }
      return 0;
    }
    public int compareTo(PopulationIndividual o)
    {
      return doubleCmp(this.E, o.E);
    }
  };

  private void calculateFittingErrors()
  {
    calculateFittingErrors(pop);
  }
  private void calculateFittingErrors(PopulationIndividual[] pop)
  {
    for (;;)
    {
      try {
        ArrayList<Callable<Void>> list = new ArrayList<Callable<Void>>();
        list.addAll(Arrays.asList(pop));
        for (Future f: executor_service.invokeAll(list))
        {
          try {
            f.get();
          }
          catch(ExecutionException e)
          {
            // XXX: what to do?
            throw new RuntimeException(e);
          }
          catch(CancellationException e)
          {
            // XXX: what to do?
            throw new RuntimeException(e);
          }
        }
        return;
      }
      catch (InterruptedException e)
      {
      }
    }
  }

  private double[][] getRawPop(PopulationIndividual[] pop)
  {
    double[][] result = new double[npop][];
    for (int i=0; i<npop; i++)
    {
      result[i] = pop[i].p;
    }
    return result;
  }
  private double[][] getRawPop()
  {
    return getRawPop(pop);
  }
  private double[][] getPop()
  {
    double[][] result = new double[npop][];
    for (int i=0; i<npop; i++)
    {
      result[i] = new double[nparam];
      for (int j=0; j<nparam; j++)
      {
        result[i][j] = pop[i].p[j];
      }
    }
    return result;
  }

  private void sortAll()
  {
    Arrays.sort(pop);
  }

  public DECtx(CostFunc func, double[] p_min, double[] p_max,
               double[] p, boolean cov_on, boolean traditional_recombination_on,
               int npop, ExecutorService executor_service,
               AdvancedFitOptions opts)
  {
    this.km = opts.km;
    this.kr = opts.kr;
    this.pm = opts.pm;
    this.cr = opts.cr;
    this.lambda = opts.lambda;
    this.reportPerf = opts.reportPerf;
    this.cycle = 0;
    this.traditional_recombination_on = traditional_recombination_on;
    this.func = func;
    this.cov_on = cov_on;
    this.npop = npop;
    this.nparam = p_min.length;
    this.executor_service = executor_service;
    if (p_max.length != nparam || p.length != nparam)
    {
      throw new IllegalArgumentException();
    }
    if (npop < 1)
    {
      throw new IllegalArgumentException();
    }
    this.p_min = new double[nparam];
    for (int i=0; i<nparam; i++)
    {
      this.p_min[i] = p_min[i];
    }
    this.p_max = new double[nparam];
    this.dp = new double[nparam];
    for (int i=0; i<nparam; i++)
    {
      this.p_max[i] = p_max[i];
      this.dp[i] = this.p_max[i] - this.p_min[i];
    }
    this.pop = new PopulationIndividual[npop];
    this.pop[0] = new PopulationIndividual(p);
    for (int i=1; i<npop; i++)
    {
      this.pop[i] = new PopulationIndividual();
    }
    calculateFittingErrors();
    sortAll();
  }
  public void iteration()
  {
    Random r = new Random();
    Matrix T = null;
    double[][] mm = new double[npop][];
    double[] b = pop[0].p;
    PopulationIndividual[] pop2 = new PopulationIndividual[npop];
    for (int i=0; i<npop; i++)
    {
      double[] pa = pop[r.nextInt(npop)].p;
      double[] pb = pop[r.nextInt(npop)].p;
      double[] pc = pop[r.nextInt(npop)].p;
      boolean mutate;
      mm[i] = new double[nparam];
      // calculate the individual to mutate
      for (int j=0; j<nparam; j++)
      {
        mm[i][j] = pc[j] + (b[j] - pc[j])*lambda;
      }
      // mutate the best individual by sampled differences scaled
      // by the mutation constant
      if (traditional_recombination_on)
      {
        mutate = true;
      }
      else
      {
        mutate = Math.random() < pm;
      }
      if (mutate)
      {
        for (int j=0; j<nparam; j++)
        {
          mm[i][j] = mm[i][j] + km*(pa[j] - pb[j]);
        }
      }
      else
      {
        for (int j=0; j<nparam; j++)
        {
          mm[i][j] = mm[i][j] + kr*(pa[j] + pb[j] - 2*mm[i][j]);
        }
      }
    }
    if (cov_on)
    {
      for (int i=0; i<npop; i++)
      {
        double[] p = pop[i].p;
        /*
           Normalize parameters to range [0,1]. Due to the simplicity of DE,
           normalization is not normally necessary. In order to calculate the
           covariance matrix accurately, the parameters must not differ by too
           many orders of magnitude, so in this case we have to normalize the
           parameters.
         */
        for (int j=0; j<nparam; j++)
        {
          if (dp[j] != 0)
          {
            p[j] = (p[j]-p_min[j]) / dp[j];
            mm[i][j] = (mm[i][j]-p_min[j]) / dp[j];
          }
          else
          {
            p[j] = 0;
            mm[i][j] = 0;
          }
        }
      }
      Matrix popm = new Matrix(getRawPop());
      Matrix mmm = new Matrix(mm);
      Matrix covm = cov(popm);
      T = covm.eig().getV();
      popm = popm.times(T);
      mmm = mmm.times(T);
      for (int i=0; i<npop; i++)
      {
        pop[i].p = popm.getArray()[i];
        mm[i] = mmm.getArray()[i];
      }
    }
    /*
       The crossover operator chooses each parameter randomly from the old
       population and bm. The crossover constant cr is used as weight.
       Higher crossover constant means higher probability of getting a parameter
       from the mutated individual.
     */
    if (traditional_recombination_on)
    {
      for (int i=0; i<npop; i++)
      {
        double[] p = pop[i].p;
        pop2[i] = new PopulationIndividual(p);
        double[] p2 = pop2[i].p;
        for (int j=0; j<nparam; j++)
        {
          if (r.nextDouble() < cr)
          {
            p2[j] = mm[i][j];
          }
        }
      }
    }
    else
    {
      for (int i=0; i<npop; i++)
      {
        double[] p = pop[i].p;
        pop2[i] = new PopulationIndividual(p);
        double[] p2 = pop2[i].p;
        for (int j=0; j<nparam; j++)
        {
          p2[j] = mm[i][j];
        }
      }
    }
    if (cov_on)
    {
      // Rotate the coordinates back and denormalize them
      Matrix popm = new Matrix(getRawPop());
      Matrix pop2m = new Matrix(getRawPop(pop2));
      popm = popm.times(T.transpose());
      pop2m = pop2m.times(T.transpose());
      for (int i=0; i<npop; i++)
      {
        pop[i].p = popm.getArray()[i];
        pop2[i].p = pop2m.getArray()[i];
        double[] p = pop[i].p;
        double[] p2 = pop2[i].p;
        for (int j=0; j<nparam; j++)
        {
          p[j] = p_min[j] + p[j]*dp[j];
          p2[j] = p_min[j] + p2[j]*dp[j];
        }
      }
    }
    // replace all the parameters that are outside the limits by random values
    for (int i=0; i<npop; i++)
    {
      double[] p2 = pop2[i].p;
      for (int j=0; j<nparam; j++)
      {
        if (p2[j] < p_min[j] || p2[j] > p_max[j])
        {
          p2[j] = p_min[j] + dp[j]*r.nextDouble();
        }
      }
    }
    // evaluate fitnesses for new trial population
    calculateFittingErrors(pop2);
    // new population: each individual is compared to it's child and the best
    // is selected
    for (int i=0; i<npop; i++)
    {
      if (pop2[i].E < pop[i].E)
      {
        pop[i] = pop2[i];
      }
    }
    // sort by fitness
    sortAll();
    cycle += 1;
  }
  private PopulationIndividual worst()
  {
    return pop[npop-1];
  }
  private PopulationIndividual median()
  {
    return pop[npop/2];
  }
  private PopulationIndividual best()
  {
    return pop[0];
  }
  public double worstFittingError()
  {
    return worst().E;
  }
  public double medianFittingError()
  {
    return median().E;
  }
  public double bestFittingError()
  {
    return best().E;
  }
  public double[] medianIndividual()
  {
    double[] p = median().p;
    double[] result = new double[nparam];
    for (int i=0; i<nparam; i++)
    {
      result[i] = p[i];
    }
    return result;
  }
  public double[] bestIndividual()
  {
    double[] p = best().p;
    double[] result = new double[nparam];
    for (int i=0; i<nparam; i++)
    {
      result[i] = p[i];
    }
    return result;
  }
  public static void main(String[] args)
  {
    double[] p_min = {-10, -10};
    double[] p_max = {10, 10};
    double[] p = {10, 10};
    int cpus = Runtime.getRuntime().availableProcessors();
    ThreadPoolExecutor exec =
      new ThreadPoolExecutor(cpus, cpus,
                             1, TimeUnit.SECONDS,
                             new LinkedBlockingQueue<Runnable>());
    CostFunc func = new CostFunc() {
      public double calculate(double[] p)
      {
        try {
          Thread.sleep(10);
        }
        catch(InterruptedException e) {}
        if (p.length != 2)
        {
          throw new IllegalArgumentException();
        }
        return p[0]*p[0] + p[1]*p[1];
      }
    };
    DECtx ctx = new DECtx(func, p_min, p_max, p, true, true, 20, exec,
                          new AdvancedFitOptions());
    for (int i=0; i<100; i++)
    {
      ctx.iteration();
      System.out.println("best " + ctx.bestFittingError() +
                         ", median " + ctx.medianFittingError());
    }
    exec.shutdown();
  }
};
