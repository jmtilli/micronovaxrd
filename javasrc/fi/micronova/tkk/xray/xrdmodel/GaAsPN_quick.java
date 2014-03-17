package fi.micronova.tkk.xray.xrdmodel;
import java.util.Arrays;
import java.util.List;

public class GaAsPN_quick {
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
                                        new FitValue(0,1,1),
                                        GaAs, GaAs);
      final Layer layer = new Layer("",
                                    new FitValue(0,30e-9,100e-9), // d
                                    new FitValue(0,0.007,1), // p
                                    new FitValue(0,0,1), // r
                                    new FitValue(0,1,1), // wh
                                    GaAsP, GaAsN);
      final double lambda = 1.54056e-10;
      final LayerStack stack = new LayerStack(lambda, tab);
      final double[] theta = new double[2000];
      stack.add(substrate);
      stack.add(layer);
      for (int i = 0; i < theta.length; i++)
      {
        theta[i] = ((16.75-15.75)/theta.length*i + 15.75)*Math.PI/180;
      }
      for (int i = 0; i < 1000; i++)
      {
        double[] curve = stack.xrdCurveFast(theta);
      }
    }
}
