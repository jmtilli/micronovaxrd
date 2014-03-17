package fi.micronova.tkk.xray.xrdde;
import java.util.concurrent.*;
import java.util.*;
import fi.micronova.tkk.xray.*;
import fi.micronova.tkk.xray.de.*;
import fi.micronova.tkk.xray.xrdmodel.*;
public class XRDFittingCtx {
  private GraphData gd;
  private LayerStack s;
  private FittingErrorFunc func;
  private ExecutorService exec;
  private DECtx.CostFunc cost_func;
  private DECtx de_ctx;
  public XRDFittingCtx(LayerStack new_s, GraphData new_gd,
                       boolean cov_on, boolean traditional_recombination_on,
                       int npop, FittingErrorFunc new_func,
                       ExecutorService exec)
  {
    this.s = new_s.deepCopy();
    this.gd = new_gd;
    this.func = new_func;
    this.cost_func = new DECtx.CostFunc() {
      public double calculate(double[] p) throws Exception
      {
        LayerStack s2 = s.deepCopy();
        s2.setFitValues(p);
        GraphData gd2 = gd.simulate(s2);
        return func.getError(gd2.meas, gd2.simul);
      }
    };
    this.de_ctx = new DECtx(
        this.cost_func,
        this.s.getFitValuesForFitting(FitValue.FitValueType.MIN),
        this.s.getFitValuesForFitting(FitValue.FitValueType.MAX),
        this.s.getFitValuesForFitting(FitValue.FitValueType.EXPECTED),
        cov_on, traditional_recombination_on, npop, exec);
  }
  public void iteration()
  {
    this.de_ctx.iteration();
  }
  public double[] bestIndividual()
  {
    return this.de_ctx.bestIndividual();
  }
  public double[] medianIndividual()
  {
    return this.de_ctx.medianIndividual();
  }
  public double bestFittingError()
  {
    return this.de_ctx.bestFittingError();
  }
  public double medianFittingError()
  {
    return this.de_ctx.medianFittingError();
  }
  public static void main(String[] args) throws Throwable
  {
    SFTables tab = SFTables.defaultLookup();
    Atom atom_Ga = tab.lookup(31);
    Atom atom_As = tab.lookup(33);
    Atom atom_P = tab.lookup(15);
    Atom atom_N = tab.lookup(7);
    List<LatticeAtom> GaP_atoms = Arrays.asList(new LatticeAtom[]{
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.00, 0.00)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.50, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.00, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.50, 0.00)),
      new LatticeAtom(atom_P,  1, new AtomicPosition(0.25, 0.25, 0.75)),
      new LatticeAtom(atom_P,  1, new AtomicPosition(0.25, 0.75, 0.25)),
      new LatticeAtom(atom_P,  1, new AtomicPosition(0.75, 0.25, 0.25)),
      new LatticeAtom(atom_P,  1, new AtomicPosition(0.75, 0.75, 0.75)),
    });
    List<LatticeAtom> GaAs_atoms = Arrays.asList(new LatticeAtom[]{
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.00, 0.00)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.50, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.00, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.50, 0.00)),
      new LatticeAtom(atom_As, 1, new AtomicPosition(0.25, 0.25, 0.75)),
      new LatticeAtom(atom_As, 1, new AtomicPosition(0.25, 0.75, 0.25)),
      new LatticeAtom(atom_As, 1, new AtomicPosition(0.75, 0.25, 0.25)),
      new LatticeAtom(atom_As, 1, new AtomicPosition(0.75, 0.75, 0.75)),
    });
    List<LatticeAtom> GaN_atoms = Arrays.asList(new LatticeAtom[]{
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.00, 0.00)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.00, 0.50, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.00, 0.50)),
      new LatticeAtom(atom_Ga, 1, new AtomicPosition(0.50, 0.50, 0.00)),
      new LatticeAtom(atom_N,  1, new AtomicPosition(0.25, 0.25, 0.75)),
      new LatticeAtom(atom_N,  1, new AtomicPosition(0.25, 0.75, 0.25)),
      new LatticeAtom(atom_N,  1, new AtomicPosition(0.75, 0.25, 0.25)),
      new LatticeAtom(atom_N,  1, new AtomicPosition(0.75, 0.75, 0.75)),
    });
    final double GaAs_V = 1.80668754600768e-28;
    final double GaAs_xy = 5.6532e-10;
    final double GaAs_z = 2.8266e-10;
    final double GaAs_poisson = 0.3117;
    final double GaP_V = 1.61923182837625e-28;
    final double GaP_xy = 5.4505e-10;
    final double GaP_z = 2.72525e-10;
    final double GaP_poisson = 0.3070;
    final double GaN_V = 9.23454080000000e-29;
    final double GaN_xy = 4.52e-10;
    final double GaN_z = 2.26e-10;
    final double GaN_poisson = 0.33;
    final UnitCell GaAs_uc = new UnitCell(GaAs_V, GaAs_atoms);
    final UnitCell GaP_uc = new UnitCell(GaP_V, GaP_atoms);
    final UnitCell GaN_uc = new UnitCell(GaN_V, GaN_atoms);
    final Miller m = new Miller(2, 0, 0);
    final SimpleMaterial GaAs =
      new SimpleMaterial("","",m, GaAs_xy, GaAs_z, GaAs_poisson, GaAs_uc);
    final SimpleMaterial GaP =
      new SimpleMaterial("","",m, GaP_xy, GaP_z, GaP_poisson, GaP_uc);
    final SimpleMaterial GaN =
      new SimpleMaterial("","",m, GaN_xy, GaN_z, GaN_poisson, GaN_uc);
    final Mixture GaAsP = new Mixture(Arrays.asList(new Mixture.Constituent[]{
      new Mixture.Constituent(0.3, GaAs),
      new Mixture.Constituent(0.7, GaP)
    }));
    final Mixture GaAsN = new Mixture(Arrays.asList(new Mixture.Constituent[]{
      new Mixture.Constituent(0.3, GaAs),
      new Mixture.Constituent(0.7, GaN)
    }));
    final Layer substrate = new Layer("",
                                      new FitValue(0,0,0),
                                      new FitValue(0,0,0),
                                      new FitValue(0,0,0),
                                      new FitValue(0,1,1, false),
                                      GaAs, GaAs);
    final Layer layer = new Layer("",
                                  new FitValue(0,30e-9,100e-9), // d
                                  new FitValue(0,0.007,1), // p
                                  new FitValue(0,0,0), // r
                                  new FitValue(0,1,1, false), // wh
                                  GaAsP, GaAsN);
    final double lambda = 1.54056e-10;
    final LayerStack stack = new LayerStack(lambda, tab);
    final double[] alpha_0 = new double[2000];
    stack.add(substrate);
    stack.add(layer);
    stack.getProd().setExpected(70);
    for (int i = 0; i < alpha_0.length; i++)
    {
      alpha_0[i] = ((16.75-15.75)/alpha_0.length*i + 15.75);
    }
    GraphData gd = new GraphData(alpha_0, new double[2000], new double[2000]);
    double[] simul = gd.simulate(stack).simul;
    gd = new GraphData(alpha_0, simul, simul, false);
    int cpus = Runtime.getRuntime().availableProcessors();
    ThreadPoolExecutor exec =
            new ThreadPoolExecutor(cpus, cpus,
                                   1, TimeUnit.SECONDS,
                                   new LinkedBlockingDeque<Runnable>());
    XRDFittingCtx init = new XRDFittingCtx(stack, gd, true, true, 40,
                                           new LogFittingErrorFunc(2),
                                           exec);
    for (int i = 0; i < 100; i++)
    {
      init.iteration();
      System.out.println(init.medianFittingError());
      System.out.println(init.medianIndividual()[3]);
      System.out.println("--");
    }
    exec.shutdown();
  }
};
