package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.util.logging.*;
import org.jfree.data.xy.*;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;

import fi.micronova.tkk.xray.chart.*;
import fi.micronova.tkk.xray.chart.ChartFrame;
import fi.micronova.tkk.xray.octif.*;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.measimport.*;
import fi.micronova.tkk.xray.dialogs.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.xml.sax.SAXException;
import org.w3c.dom.*;



/*
 * TODO:
 * - more tools (add sl?, material info)
 *   - material info: wavelength, reflection(?), Bragg's angle, chi0, chih, chihinv, xyspace, zspace, poisson
 * - documentation (Javadoc, XRRD, short introduction for programmers & users)
 * - testing (both regression and real-life)
 * - performance (eg. Complex number class optimization and Matlab code optimization. DE in Java?)
 */

/* TODO list:
 * - better debug information for red light
 * - better lambda checking (eg. when adding new layers or modifying previous layers)
 *
 * testing:
 * - test this software works with Matlab
 *   + assert must be included
 *   + feval must be used instead of normal function calls
 *
 * coding style:
 * - change public listeners to inner classes
 * - modify dialogs to use TextFieldDialog
 *
 * documentation:
 * - write it!
 *
 * making this software actually useful:
 * - write a short introduction
 *
 * ensuring we get correct results:
 * - regression testing: ensure there are no differences between Octave and Java code
 * - verify that the same sign convention is used by Bartels et al
 * - rewrite Complex number class to use better formulas [and write a complex
 *   buffer class for better efficiency]
 *
 * other things worth doing:
 * - code DE in Java so that we don't need Matlab or Octave anymore
 *
 *
 */


/*
 * Changelog:
 * ...
 * - missing entries
 *
 * 20070705
 * - first prototype that actually works
 *
 * 20070703
 * - XRDApp prototype GUI
 *
 * ...
 * - missing entries
 */




/** The main class implementing the main user interface code.
 *
 * <p>
 *
 * Unfortunately this complex piece of Java code is not commented well.
 * Furthermore, it uses lots of difficult Java programming tricks, such as
 * inner classes. You have to try to understand this code without comments.
 */

public class XRDApp extends JFrame implements ChooserWrapper {
    private File chooserDirectory = null;
    private Fitter f = null;
    private String measPath = null; /* Path of imported measurement file */
    private final GraphData data;

    private LookupTable table;
    private MatDB db;

    private static final double Cu_K_alpha = 1.5405620e-10; /* This is the default wavelength */
    private Image green, yellow, red;
    private LayerStack layers, emptyLayers;
    private JList layeredList;
    private double dbMin = -5, dbMax = 50;

    private enum PlotStyle {LIN, LOG, SQRT};




    /* There are two ways to load mfiles to Octave. The first is to embed them in
     * the .jar file and list them here. The second is to have the files in the
     * working directory (that is, the directory with the jar file and octave_path.txt).
     * Matlab supports only the second method, so we'll use that.
     */
    private static final String[] mfiles = {};
/*
    private static final String[] mfiles = { "GS.m", "calculate_electron_density.m",
        "hsdistr.m", "randommatrix.m", "LocalSearch.m",
        "calculate_mass_density.m", "hsdistr_ordered.m",
        "randomsearch.m", "RotateVectors.m", "computeHessian.m",
        "index.m", "removeRepeatedPoints.m", "RouletteWheel.m",
        "computeNabla.m", "limit2bound.m", "twoPXover.m", "SAmutate.m",
        "crop.m", "lookuptable2.m", "uonerand.m",
        "StochasticOrthogonalSelection.m", "cyclingTwoPXover.m",
        "mdwXover.m", "urandomsearch.m", "XRRimport.m", "fir.m",
        "mix.m", "weightedXover.m", "apply_odd_filter.m",
        "fitnessfunction.m", "myrandperm.m", "wmix.m",
        "gaussian_filter.m", "octave-cell2mat.m", "xrrCurve.m",
        "calculate_beta_coeff.m", "halfXover.m", "onerand.m",
        "xrrGA.m"};
        */



    /* these must point always to the same object */
    private LayerPlotter pfit;
    private LayerPlotter p;
    private LayerStack fitLayers;

    private void loadLayers(File f, boolean enable_hint) throws LayerLoadException {
        try {
            FileInputStream fstr = new FileInputStream(f);
            BufferedInputStream bs = new BufferedInputStream(fstr);
            LayerStack newLayers = new LayerStack(XMLUtil.parse(bs).getDocumentElement(), table);
            layers.deepCopyFrom(newLayers);
        }
        catch(IOException ex) {
            throw new LayerLoadException("I/O error");
        }
        catch(ParserConfigurationException ex) {
            throw new LayerLoadException("No XML parser found");
        }
        catch(InvalidMixtureException ex) {
            throw new LayerLoadException("Invalid mixture in layer model file");
        }
        catch(SAXException ex) {
            throw new LayerLoadException("Invalid XML format");
        }
        catch(ElementNotFound ex) {
            throw new LayerLoadException(ex.getMessage());
        }
        /*
        catch(InvalidStructException ex) {
            throw new LayerLoadException("Invalid high-level file format");
        }
        */
    }

    public XRDApp() {
        super("XRD");
        data = new GraphData(null, null, null, false);
    }

    private void loadMeasurement(double[] alpha_0, double[] meas, ImportOptions opts) {
        double max = 0;
        for(int i=0; i<meas.length; i++) {
            if(alpha_0[i] >= opts.minNormal
                    && alpha_0[i] <= opts.maxNormal
                    && meas[i] > max)
                max = meas[i];
        }
        if(max > 0 && opts.normalize) {
            for(int i=0; i<meas.length; i++) {
                meas[i] /= max;
            }
        }
        int bound1 = 0, bound2 = 0;
        for(int i=0; i<meas.length; i++) {
            if(alpha_0[i] >= opts.minAngle && alpha_0[i] <= opts.maxAngle) {
                alpha_0[bound1] = alpha_0[i];
                meas[bound1] = meas[i];
                bound1++;
            }
        }
        for(int i=0; i<bound1; i++) {
            if(i % opts.modulo == 0 /* && meas[i] > 0 */ ) { /* measurement points must be uniformly spaced */
                alpha_0[bound2] = alpha_0[i];
                meas[bound2] = meas[i];
                bound2++;
            }
        }
        double[] new_alpha_0, new_meas;
        new_meas = new double[bound2];
        new_alpha_0 = new double[bound2];
        System.arraycopy(meas, 0, new_meas, 0, bound2);
        System.arraycopy(alpha_0, 0, new_alpha_0, 0, bound2);
        data.newData(new_alpha_0, new_meas, null, false);

        p.draw();
        pfit.draw();
    }




    /** Start an Octave or Matlab instance.
     *
     * <p>
     *
     * We try to read a file named 'octave_path.txt'. If it is found, we try to
     * start Octave using the command in that file. If not, we try to start
     * Matlab using the default command. On windows, Matlab can't be started
     * with another command than the default command (which is stored in
     * the registry during Matlab installation). On Unix systems, it would be
     * possible to start Matlab using another command than the default command
     * ('matlab'), but on the other hand, it's easy to write a shell script
     * named 'matlab' to start it so I didn't bother adding the option to
     * adjust the command to start Matlab.
     */
    public static Oct startOctave() throws OctException {
        Oct oct;

        String path = null;
        try {
            FileInputStream rawopf = new FileInputStream("octave_path.txt");
            InputStreamReader rawopr = new InputStreamReader(rawopf);
            BufferedReader octpathf = new BufferedReader(rawopr);
            path = octpathf.readLine();
            if(path == null)
                throw new NullPointerException();
        }
        catch (IOException ex) {
            //throw new OctException("Can't read octave_path.txt");
            //return null;
        }
        catch (NullPointerException ex) {
            //JOptionPane.showMessageDialog(null, "Can't read octave_path.txt", "Error", JOptionPane.ERROR_MESSAGE);
            //throw new OctException("Can't read octave_path.txt");
            //return null;
        }

        if(path == null) {
            try {
                boolean debug = new File("matlab_debug.txt").exists();
                /* Matlab */
                oct = new OctMatlab(debug);
                /* not supported by Matlab */
                for(String mfile: mfiles) {
                    throw new OctException("Function definitions on stdin not supported by Matlab");
                }
                oct.sync();
            }
            catch(OctException ex) {
                throw new OctException(
                        "\n\nCan't start Matlab.\n\n"+
                        "Since a file named octave_path.txt didn't exist or was empty,\n"+
                        "Matlab is used instead of Octave. Matlab is started with the\n"+
                        "default command. On Unix systems, it's 'matlab'. On Windows,\n"+
                        "the default command is stored in registry automatically during\n"+
                        "Matlab installation. If you use Unix, ensure that Matlab starts\n"+
                        "properly with the command 'matlab'. If you use Windows, try\n"+
                        "reinstalling Matlab.\n\n"+
                        "The error message was:\n\n"+
                        ex.getMessage());
            }
        } else {
            try {
                oct = new OctOctave(path);
                for(String mfile: mfiles) {
                    oct.source(oct.getClass().getClassLoader().getResourceAsStream(mfile));
                    /*
                    oct.sync();
                    System.out.println(mfile);
                    */
                }
                oct.sync();
                /* Octave */
            }
            catch(OctException ex) {
                throw new OctException(
                        "\n\nCan't start Octave.\n\n"+
                        "Since a file named octave_path.txt wasn't empty Octave is used,\n"+
                        "instead of Matlab. Octave is started with the command in\n"+
                        "octave_path.txt, which is currently:\n\n"+
                        path+"\n\n"+
                        "On Unix systems, the command should be generally 'octave -q'.\n"+
                        "On Windows systems, Octave installation is more difficult.\n"+
                        "Precise instructions to install Octave are in the file\n"+
                        "README-1st.txt.\n"+
                        "The error message was:\n\n"+
                        ex.getMessage());
            }
        }


        try {
            if(oct == null) {
                    oct = new OctOctave(path);

                    for(String mfile: mfiles) {
                        oct.source(oct.getClass().getClassLoader().getResourceAsStream(mfile));
                        /*
                        oct.sync();
                        System.out.println(mfile);
                        */
                    }
            }

            oct.sync();
        }
        catch (OctException ex) {
            throw new OctException("Can't start Octave: "+ex.getMessage());
        }

        return oct;
    }



    private boolean construct() {
        /* Load atomic masses and scattering factors */
        try {
            table = SFTables.defaultLookup();
        }
        catch(Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Can't load atomic databases",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            db = new MatDB(new File("matdb.xml"),table);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                "Can't load material database",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(db.materials.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "Empty material database",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }



        JTabbedPane tabs;
        JPanel layered, graph, fit;
        JPanel wlPanel, layeredPanel;
        JPanel layerButtonPanel;
        JTextField wl1, wl2, wl3;
        JScrollPane layeredScroll;
        //JTabbedPane sliderPane = new JTabbedPane();
        JPanel sliderPanel = new JPanel();

        emptyLayers = new LayerStack(Cu_K_alpha, table);
        layers = emptyLayers.deepCopy();

        layeredList = new JList(layers.listModel);
        final XRDApp thisFrame = this;

        /* TODO ScrollbarUpdater */
        //layers.addListDataListener(new ScrollbarUpdater(layers, sliderPane));
        //layers.addListDataListener(new ScrollbarUpdater(layers, sliderPanel));
        new ScrollbarUpdater(layers, sliderPanel);

        tabs = new JTabbedPane();

        layered = new JPanel();
        graph = new JPanel();
        fit = new JPanel();

        layered.setLayout(new BorderLayout());
        graph.setLayout(new BorderLayout());
        fit.setLayout(new BorderLayout());



        final Runnable errTask = new Runnable() {
            boolean alreadyRun = false;
            public void run() {
                if(alreadyRun)
                    return;
                alreadyRun = true;
                JOptionPane.showMessageDialog(null,
                    "There was an error with Octave/Matlab.\nPlease save your layer model and restart the program.\n\n"+
                    "If you are using Matlab, try creating a text file named matlab_debug.txt.\n"+
                    "If this file exists, Matlab keeps a log of its commands in the file matcmds.txt,\n"+
                    "which allows you to find out the exact cause of the error.\n"+
                    "Keeping the log slows down Matlab considerably so it should be used only when necessary.\n\n"+
                    "If you are using Octave, this log is always saved to octcmds.txt.",
                    "Octave/Matlab error", JOptionPane.ERROR_MESSAGE);
            }
        };



        /* ------- octave ----------- */

        Oct octtemp;
        try {
                octtemp = startOctave();
        }
        catch(OctException ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Octave error", JOptionPane.ERROR_MESSAGE);
                return false;
        }
        final Oct oct = octtemp;


        /* Empty measurement */
        double[] alpha_0, meas;
        alpha_0 = new double[999];
        meas = new double[999];
        for(int i=0; i<alpha_0.length; i++) {
            alpha_0[i] = (i+1)*90.0/1000;
            meas[i] = 1; /* avoid log(0) = -infinity */
        }
        data.newData(alpha_0, meas, null, false);
        measPath = null;


        /* ----- layer editor -------- */

        layeredPanel = new JPanel();
        layeredPanel.setLayout(new BorderLayout());

        /*
        try {
            layers.add(new Layer("Substrate", new FitValue(0,0,0),
                       new FitValue(2.26e3,2.33e3,2.4e3), new FitValue(0,0,1e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("Si"),0,
                       layers.getTable(), layers.getLambda()));
            layers.add(new Layer("Native oxide", new FitValue(0e-9,0e-9,2.5e-9),
                       new FitValue(1e3,2.1e3,3e3), new FitValue(0,0,1e-9),
                       new ChemicalFormula("Si"),new ChemicalFormula("O"),2.0/3,
                       layers.getTable(),layers.getLambda()));
            layers.add(new Layer("Thin film", new FitValue(40e-9,55e-9,70e-9),
                       new FitValue(1e3,3.4e3,4e3), new FitValue(0,0,1e-9),
                       new ChemicalFormula("Al"),new ChemicalFormula("O"),3/5.0,
                       layers.getTable(),layers.getLambda()));
        }
        catch(ChemicalFormulaException ex) {
            throw new RuntimeException("Doesn't get thrown");
        }
        catch(ElementNotFound ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        */

        layeredScroll = new JScrollPane(layeredList);
        layeredScroll.setPreferredSize(new Dimension(300,150));

        layerButtonPanel = new JPanel();
        layerButtonPanel.setLayout(new GridLayout(0,1));

        JButton btn;

        btn = new JButton("Move up");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                if(i2 == null || i2.length == 0)
                    return;
                Arrays.sort(i2);
                if(i2[i2.length-1]+1 - i2[0] != i2.length || i2[0] <= 0)
                    JOptionPane.showMessageDialog(null, "Can't move layers up. You must select a contiguous set of layers", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    layers.moveUp(i2[0], i2[i2.length-1]+1);
                    layeredList.clearSelection();
                    layeredList.setSelectionInterval(i2[0]-1, i2[i2.length-1]-1);
                }
            };
        });
        layerButtonPanel.add(btn);

        JButton b = new JButton("Add...");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try {
                    Layer l;
                    l = new Layer("New layer", new FitValue(0,50e-9,100e-9), new FitValue(0,0.5,1), new FitValue(0,0,1,false), db.materials.get(0), db.materials.get(0));
                    LayerDialog d = new LayerDialog(thisFrame, l, db, layers.getLambda());
                    if(d.call()) {
                        layers.add(l, 0);
                        // work around an obscure Java bug of "double selections"
                        layeredList.clearSelection();
                        layeredList.setSelectedIndex(layers.getSize()-1);
                    }
                    d.dispose();
                }
                catch(InvalidMixtureException ex) {
                    throw new RuntimeException("never thrown");
                }
            }
        });
        layerButtonPanel.add(b);

        btn = new JButton("Edit...");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = layeredList.getSelectedIndex();
                int[] i2 = layeredList.getSelectedIndices();
                if(i < 0 || i >= layers.getSize() || i2.length != 1)
                    JOptionPane.showMessageDialog(null, "Can't edit layer", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    LayerDialog d = new LayerDialog(thisFrame, layers.getElementAt(i), db, layers.getLambda());
                    d.call();
                    d.dispose();
                    /*
                    if(d.call())
                        layers.invalidate(null);
                        */
                }
            };
        });
        layerButtonPanel.add(btn);

        b = new JButton("Copy");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                for(int i: i2) {
                    Layer l = layers.getElementAt(i).deepCopy();
                    layers.add(l,layers.getSize());
                }
            }
        });
        layerButtonPanel.add(b);

        b = new JButton("Duplicate");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                for(int i: i2) {
                    Layer l = layers.getElementAt(i);
                    layers.add(l,layers.getSize());
                }
            }
        });
        layerButtonPanel.add(b);

        b = new JButton("Separate");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                for(int i: i2) {
                    Layer l = layers.getElementAt(i).deepCopy();
                    layers.remove(i);
                    layers.add(l,i);
                }
                layeredList.clearSelection();
            }
        });
        layerButtonPanel.add(b);

        b = new JButton("Optics...");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                for(int i: i2) {
                    Layer l = layers.getElementAt(i);
                    double xyspace = layers.getElementAt(layers.getSize()-1).calcXYSpace(0);
                    for (int j=layers.getSize()-2; j>=i; j--)
                    {
                        xyspace = layers.getElementAt(j).calcXYSpace(xyspace);
                    }
                    SimpleMaterial sm0 = l.getSimpleMaterial(0);
                    SimpleMaterial sm = sm0.getStrainedMaterial(xyspace);
                    try {
                        Susceptibilities susc = sm.susc(layers.getLambda());
                        Susceptibilities susc0 = sm0.susc(layers.getLambda());
                        System.out.println("susc = " + susc); // FIXME replace with dialog
                        System.out.println("susc0 = " + susc0); // FIXME replace with dialog
                        System.out.println("xyspace = " + sm.getXYSpace() + " m");
                        System.out.println("xyspace0 = " + sm0.getXYSpace() + " m");
                        System.out.println("zspace = " + sm.getZSpace() + " m");
                        System.out.println("zspace0 = " + sm0.getZSpace() + " m");
                        System.out.println("poisson = " + sm.getPoisson());
                        System.out.println("reflection = " + sm.getReflection());
                        System.out.println("bragg = " +
                                               180/Math.PI
                                             * Math.asin(  layers.getLambda()
                                                         / (2*sm.getZSpace())) + " degrees");
                    }
                    catch(UnsupportedWavelength ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        layerButtonPanel.add(b);

        /*
        btn = new JButton("Material information...");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int i = layeredList.getSelectedIndex();
                int[] i2 = layeredList.getSelectedIndices();
                if(i < 0 || i >= layers.getSize() || i2.length != 1)
                    JOptionPane.showMessageDialog(null, "Can't display optical information", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    // TODO: implement this!
                    //InfoDialog d = new InfoDialog(thisFrame);
                    //d.call(layers.getElementAt(i));
                    //d.dispose();
                }
            };
        });
        layerButtonPanel.add(btn);
        */

        btn = new JButton("Delete");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                Arrays.sort(i2);
                for(int k=i2.length-1; k>=0; k--) {
                    int i = i2[k];
                    layers.remove(i);
                }
                layeredList.clearSelection();
            };
        });
        layerButtonPanel.add(btn);

        btn = new JButton("Move down");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                if(i2 == null || i2.length == 0)
                    return;
                Arrays.sort(i2);
                if(i2[i2.length-1]+1 - i2[0] != i2.length || i2[i2.length-1]+1 >= layers.getSize())
                    JOptionPane.showMessageDialog(null, "Can't move layers down. You must select a contiguous set of layers", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    layers.moveDown(i2[0], i2[i2.length-1]+1);
                    layeredList.clearSelection();
                    layeredList.setSelectionInterval(i2[0]+1, i2[i2.length-1]+1);
                }
            };
        });
        layerButtonPanel.add(btn);


        layeredPanel.add(layeredScroll,BorderLayout.CENTER);
        layeredPanel.add(layerButtonPanel,BorderLayout.EAST);
        layered.add(layeredPanel,BorderLayout.CENTER);



        /* -------------- wavelength selector --------------- */

        wlPanel = new JPanel();
        wlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        wlPanel.add(new JLabel("Wavelength"));
        final JLabel editWlLabel = new JLabel("");
        final JButton editWlButton = new JButton("Edit...");
        //wlPanel.add(new JLabel("nm"));
        wlPanel.add(editWlLabel);
        wlPanel.add(editWlButton);

        editWlButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                WavelengthDialog dialog = new WavelengthDialog(thisFrame);
                Double lambda2 = dialog.call(layers.getLambda());
                dialog.dispose();
                if(lambda2 != null) {
                    try {
                        layers.changeLambda(lambda2);
                    }
                    catch(UnsupportedWavelength ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        layers.addLayerModelListener(new LayerModelAdapter() {
            public void modelPropertyChanged(EventObject ev) {
                editWlLabel.setText(String.format(Locale.US,"%.6f",layers.getLambda()*1e9)+" nm");
            }
        });
        editWlLabel.setText(String.format(Locale.US,"%.6f",layers.getLambda()*1e9)+" nm");

        layered.add(wlPanel,BorderLayout.NORTH);


        JPanel northWlPanel = new JPanel();
        northWlPanel.setLayout(new BorderLayout());
        final JPlotArea light = new JPlotArea();
        light.setPreferredSize(new Dimension(32,32));
        light.setMinimumSize(new Dimension(32,32));
        northWlPanel.add(light,BorderLayout.EAST);
        graph.add(northWlPanel,BorderLayout.NORTH);

        northWlPanel = new JPanel();
        northWlPanel.setLayout(new BorderLayout());
        wlPanel = new JPanel();
        wlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        wlPanel.add(new JLabel("Wavelength"));
        final JLabel fitWlLabel = new JLabel("");
        wlPanel.add(fitWlLabel);
        wlPanel.add(new JLabel(" FWHM"));
        final JLabel fitConvLabel = new JLabel("");
        wlPanel.add(fitConvLabel);
        wlPanel.add(new JLabel(" normalization"));
        final JLabel fitNormLabel = new JLabel("");
        wlPanel.add(fitNormLabel);
        wlPanel.add(new JLabel(" sum term"));
        final JLabel fitSumLabel = new JLabel("");
        wlPanel.add(fitSumLabel);

        final JPlotArea fitPlotLight = new JPlotArea();
        final JPlotArea fitLight = new JPlotArea();
        JPanel lightPanel = new JPanel();
        fitPlotLight.setPreferredSize(new Dimension(32,32));
        fitPlotLight.setMinimumSize(new Dimension(32,32));
        fitLight.setPreferredSize(new Dimension(32,32));
        fitLight.setMinimumSize(new Dimension(32,32));
        lightPanel.add(fitPlotLight);
        lightPanel.add(fitLight);
        try {
            green = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("green.png"));
            yellow = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("yellow.png"));
            red = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("red.png"));
        }
        catch(IOException ex) {
            JOptionPane.showMessageDialog(null, "can't read png files", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        fitLight.newImage(green);
        northWlPanel.add(wlPanel,BorderLayout.CENTER);
        northWlPanel.add(lightPanel,BorderLayout.EAST);
        fit.add(northWlPanel,BorderLayout.NORTH);



        /* -------------------- measurement loading -------------------- */

        /* these must be created before automatic fit creation code */

        final JMenuItem fileLoadMeas = new JMenuItem("Load measurement...");
        final JMenuItem fileLoadEmpty = new JMenuItem("Load empty measurement...");
        final JMenuItem fileLoadAscii = new JMenuItem("Load ASCII export...");
        final JMenuItem fileSwap = new JMenuItem("Use simulation as measurement");
        final JMenuItem fileSwapOct = new JMenuItem("Use Octave simulation as measurement");
        final JMenuItem fileLayerExport = new JMenuItem("Export layers to text file...");


        /* ------------------- automatic fit -------------------- */
        //final JPlotArea fitPlotArea = new JPlotArea();
        final JChartArea fitPlotArea = new JChartArea();
        fitLayers = emptyLayers.deepCopy();

        pfit = new LayerPlotter(fitPlotArea, fitPlotLight, fitLayers, data, green, yellow, red, dbMin, dbMax);

        fitLayers.addLayerModelListener(new LayerModelAdapter() {
            public void modelPropertyChanged(EventObject ev) {
                final double FWHM_SCALE = 2*Math.sqrt(2*Math.log(2));

                fitWlLabel.setText(String.format(Locale.US,"%.6f",fitLayers.getLambda()*1e9)+" nm");
                fitConvLabel.setText(String.format(Locale.US,"%.6f",fitLayers.getStdDev().getExpected()*FWHM_SCALE*180/Math.PI)+" degrees");
                fitNormLabel.setText(String.format(Locale.US,"%.3f",fitLayers.getProd().getExpected())+" dB "+(fitLayers.getProd().getEnabled()?"(fit)":"(no fit)"));
                fitSumLabel.setText(String.format(Locale.US,"%.3f",fitLayers.getSum().getExpected())+" dB "+(fitLayers.getSum().getEnabled()?"(fit)":"(no fit)"));
            }
        });
        //fitLayers.invalidate(this);

        GridBagConstraints c = new GridBagConstraints();
        JPanel fitSouth = new JPanel();
        JList fitList = new JList(fitLayers.listModel);
        JScrollPane fitListPane = new JScrollPane(fitList);
        fitListPane.setPreferredSize(new Dimension(400,150));

        fitPlotArea.setPreferredSize(new Dimension(600,400));
        fitPlotArea.setPreferredSize(new Dimension(600,400));
        fit.add(fitPlotArea,BorderLayout.CENTER);
        JPanel plotControls = new JPanel();
        plotControls.setLayout(new GridBagLayout());
        c.ipadx = c.ipady = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(3,3,3,3);
        c.gridwidth = 1;
        final JButton exportButton = new JButton("Export model");
        final JButton importButton = new JButton("Import model");
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                fitLayers.deepCopyFrom(layers);
            }
        });
        plotControls.add(importButton,c);
        //c.gridwidth = GridBagConstraints.REMAINDER;

        final JButton startFitButton = new JButton("Start fit");
        final JButton stopFitButton = new JButton("Stop fit");
        final SpinnerNumberModel popSizeModel = new SpinnerNumberModel(30,20,2000,1);
        final SpinnerNumberModel iterationsModel = new SpinnerNumberModel(100,1,10000,1);
        final SpinnerNumberModel pModel = new SpinnerNumberModel(2,1,10,1);
        final SpinnerNumberModel firstAngleModel = new SpinnerNumberModel(0,0,90,0.01);
        final SpinnerNumberModel lastAngleModel = new SpinnerNumberModel(90,0,90,0.01);
        final SpinnerNumberModel thresholdModel = new SpinnerNumberModel(20,-500,500,0.1);
        final JComboBox algoBox = new JComboBox(Algorithm.values());
        final JComboBox funcBox = new JComboBox(FitnessFunction.values());
        /*
        final JCheckBox nonlinBox = new JCheckBox("Nonlinear fitness space estimation");
        nonlinBox.setSelected(true);
        */

        startFitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    LayerTask endTask = new LayerTask() {
                        public void run(LayerStack s, String msg) {
                            f = null;
                            startFitButton.setEnabled(true);
                            stopFitButton.setEnabled(false);
                            importButton.setEnabled(true);
                            //exportButton.setEnabled(true);
                            fileLoadMeas.setEnabled(true);
                            fileLoadAscii.setEnabled(true);
                            fileLoadEmpty.setEnabled(true);
                            fileSwap.setEnabled(true);
                            fileSwapOct.setEnabled(true);
                            pfit.setAdditionalTitle("");
                        }
                    };
                    final Runnable errTask2 = new Runnable() {
                        public void run() {
                            f = null;
                            stopFitButton.setEnabled(false);
                            errTask.run();
                        }
                    };
                    LayerTask plotTask = new LayerTask() {
                        public void run(LayerStack s, String msg) {
                            pfit.setAdditionalTitle(msg);
                            fitLayers.deepCopyFrom(s);
                        }
                    };
                    f = new Fitter(fitLight, oct, data, endTask, plotTask, errTask2, fitLayers,
                                   (Integer)popSizeModel.getNumber(), (Integer)iterationsModel.getNumber(),
                                   (Double)firstAngleModel.getNumber(), (Double)lastAngleModel.getNumber(),
                                   green, yellow, (Algorithm)algoBox.getSelectedItem(), (FitnessFunction)funcBox.getSelectedItem(), (Double)thresholdModel.getNumber(), (Integer)pModel.getNumber());//, nonlinBox.isSelected());
                    startFitButton.setEnabled(false);
                    stopFitButton.setEnabled(true);
                    stopFitButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ev) {
                            f.closeWithoutWaiting();
                            stopFitButton.setEnabled(false);
                        }
                    });
                    importButton.setEnabled(false);
                    //exportButton.setEnabled(false);
                    fileLoadMeas.setEnabled(false); //
                    fileLoadAscii.setEnabled(false); //
                    fileLoadEmpty.setEnabled(false); //
                    fileSwap.setEnabled(false);
                    fileSwapOct.setEnabled(false);
                }
                catch(OctException ex) {
                    errTask.run();
                }
                catch(NoDataPointsException ex) {
                    JOptionPane.showMessageDialog(null, "Too few data points in the fitting range", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(UnsupportedWavelength ex) {
                    JOptionPane.showMessageDialog(null, "Unsupported wavelength", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(SimulationException ex) {
                    JOptionPane.showMessageDialog(null, "Simulation exception", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        plotControls.add(startFitButton,c);
        c.gridwidth = 1;
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                layers.deepCopyFrom(fitLayers);
            }
        });
        plotControls.add(exportButton,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        stopFitButton.setEnabled(false);
        plotControls.add(stopFitButton,c);


        c.gridwidth = 1;
        plotControls.add(new JLabel("Population size"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(popSizeModel),c);

        c.gridwidth = 1;
        plotControls.add(new JLabel("Iterations"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(iterationsModel),c);

        c.gridwidth = 1;
        plotControls.add(new JLabel("First angle"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(firstAngleModel),c);

        c.gridwidth = 1;
        plotControls.add(new JLabel("Last angle"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(lastAngleModel),c);

        c.gridwidth = 1;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JLabel("Algorithm"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(algoBox,c);

        //c.gridwidth = 1;
        //plotControls.add(new JLabel("Nonlinear"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        //plotControls.add(nonlinBox,c);

        c.gridwidth = 1;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JLabel("Fitness function"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(funcBox,c);


        c.gridwidth = 1;
        plotControls.add(new JLabel("Threshold rel.f. (dB)"),c);
        c.gridwidth = 1;
        plotControls.add(new JSpinner(thresholdModel),c);

        c.gridwidth = 1;
        plotControls.add(new JLabel("p-norm"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(pModel),c);


        //c.gridwidth = 1;
        //plotControls.add(new JLabel("Nonlinear"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        //plotControls.add(nonlinBox,c);

        c.weighty = 1;
        plotControls.add(new JPanel(),c);


        /* fit.add(plotControls,BorderLayout.SOUTH); */
        fitSouth.setLayout(new BorderLayout());
        fitSouth.add(plotControls,BorderLayout.EAST);
        fitSouth.add(fitListPane,BorderLayout.CENTER);
        fit.add(fitSouth,BorderLayout.SOUTH);



        /* ------------------ manual fit -------------- */

        //final JPlotArea plotarea = new JPlotArea();
        final JChartArea plotarea = new JChartArea();

        p = new LayerPlotter(plotarea, light, layers, data, green, yellow, red, dbMin, dbMax);

        plotarea.setPreferredSize(new Dimension(600,400));
        graph.add(plotarea,BorderLayout.CENTER);
        graph.add(sliderPanel,BorderLayout.SOUTH);

        //layers.invalidate(this);



        /* add tabs */

        tabs.addTab("Layer editor", null, layered, "Layer editor");
        tabs.addTab("Manual fit", null, graph, "Manual fitting");
        tabs.addTab("Automatic fit", null, fit, "Automatic fitting");



        /* --------- menu bars -------------- */

        final ActionListener exitAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                p.close();
                pfit.close();
                try {
                    if(f != null)
                        f.close();
                    synchronized(oct) {
                        oct.exit();
                    }
                }
                catch(InterruptedException ex) {}
                thisFrame.dispose();
            }
        };

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                exitAction.actionPerformed(null);
            }
        });


        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenu dataMenu = new JMenu("Data");
        menuBar.add(dataMenu);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        fileLoadMeas.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(chooser.showOpenDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        PANImport.Data importdat;
                        importdat = PANImport.PANImport(new FileInputStream(chooser.getSelectedFile()));

                        assert(importdat.alpha_0.length == importdat.meas.length);
                        assert(importdat.alpha_0.length > 0);

                        ImportDialog dialog = new ImportDialog(thisFrame,importdat.alpha_0.length,
                            importdat.alpha_0[0], importdat.alpha_0[importdat.alpha_0.length-1], false, false);
                        ImportOptions opts = dialog.call();
                        dialog.dispose();
                        if(opts == null)
                            return;

                        loadMeasurement(importdat.alpha_0, importdat.meas, opts);

                        measPath = chooser.getSelectedFile().getAbsolutePath();
                        String fname = chooser.getName(chooser.getSelectedFile());

                        setTitle("XRD ("+fname+")");
                    }
                    catch(ImportException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid file format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(fileLoadMeas);

        fileLoadEmpty.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                double[] alpha_0, meas;
                LoadEmptyDialog dialog = new LoadEmptyDialog(thisFrame);
                DataOptions opts = dialog.call();
                dialog.dispose();
                if(opts == null)
                    return;

                alpha_0 = new double[opts.ndata];
                meas = new double[opts.ndata];
                for(int i=0; i<alpha_0.length; i++) {
                    alpha_0[i] = i*(opts.max-opts.min)/(opts.ndata-1) + opts.min;
                    meas[i] = 1; /* avoid log(0) = -infinity */
                }

                data.newData(alpha_0, meas, null, false);
                measPath = null;
                p.draw();
                pfit.draw();
                setTitle("XRD");
            }
        });
        fileMenu.add(fileLoadEmpty);

        fileSwap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    GraphData d;
                    d = data.simulate(layers);
                    loadMeasurement(d.alpha_0, d.simul, new ImportOptions(1, 0, 90, 0, 90, true, false)); /* no normalization */
                    measPath = null;
                    setTitle("XRD");
                }
                catch(SimulationException ex) {
                    JOptionPane.showMessageDialog(null, "Simulation error", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        fileSwapOct.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                try {
                    GraphData d;
                    synchronized(oct) {
                        d = data.octSimulate(layers, oct);
                    }
                    loadMeasurement(d.alpha_0, d.simul, new ImportOptions(1, 0, 90, 0, 90, true, false)); /* no normalization */
                    measPath = null;
                    setTitle("XRD");
                }
                catch(SimulationException ex) {
                    JOptionPane.showMessageDialog(null, "Simulation error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(OctException ex) {
                    errTask.run();
                }
            }
        });

        fileLoadAscii.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(chooser.showOpenDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        AsciiImport.AsciiData importdat;
                        importdat = AsciiImport.AsciiImport(new FileInputStream(chooser.getSelectedFile()));

                        assert(importdat.alpha_0.length == importdat.meas.length);
                        assert(importdat.alpha_0.length > 0);

                        ImportDialog dialog = new ImportDialog(thisFrame,importdat.alpha_0.length,
                            importdat.alpha_0[0], importdat.alpha_0[importdat.alpha_0.length-1], true, false);
                        ImportOptions opts = dialog.call();
                        dialog.dispose();
                        if(opts == null)
                            return;

                        loadMeasurement(importdat.alpha_0, opts.importSimul ? importdat.simul : importdat.meas, opts);

                        measPath = null;

                        setTitle("XRD");
                    }
                    catch(ImportException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid file format", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        fileMenu.add(fileLoadAscii);
        fileMenu.add(fileSwap);
        fileMenu.add(fileSwapOct);
        fileMenu.addSeparator();


        JMenuItem fileLoadLayers = new JMenuItem("Load layers...");
        fileLoadLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(chooser.showOpenDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        loadLayers(chooser.getSelectedFile(),true);
                    }
                    catch(LayerLoadException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(fileLoadLayers);

        JMenuItem fileSaveLayers = new JMenuItem("Save layers...");
        fileSaveLayers.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(measPath != null) {
                    File layersFile = new File(measPath+".layers");
                    File parentDir = layersFile.getParentFile();
                    if(parentDir != null && parentDir.isDirectory())
                        chooser.setSelectedFile(new File(measPath+".layers"));
                }
                if(chooser.showSaveDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        FileOutputStream fstr = new FileOutputStream(chooser.getSelectedFile());
                        /*
                        additional_data.put("measPath",measPath == null ? "" : measPath);
                        Fcode.fencode(layers.structExport(additional_data), fstr);
                        */

                        Document doc = XMLUtil.newDocument();
                        doc.appendChild(layers.export(doc));
                        XMLUtil.unparse(doc, fstr);
                    }
                    catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    catch(ParserConfigurationException ex) {
                        JOptionPane.showMessageDialog(null, "No XML parser", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    catch(TransformerConfigurationException ex) {
                        JOptionPane.showMessageDialog(null, "No XML transformer", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    catch(TransformerException ex) {
                        JOptionPane.showMessageDialog(null, "XML transform error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(fileSaveLayers);

        fileLayerExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                JFileChooser chooser = new JFileChooser();
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(chooser.showSaveDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        OutputStream fstr = new FileOutputStream(chooser.getSelectedFile());
                        Writer rw = new OutputStreamWriter(fstr);
                        BufferedWriter bw = new BufferedWriter(rw);
                        PrintWriter w = new PrintWriter(bw);
                        ListModel model = layers.listModel;
                        w.println("Layer stack");
                        for(int i=0; i<model.getSize(); i++)
                            w.println(model.getElementAt(i).toString());
                        if(w.checkError())
                          throw new IOException();
                    }
                    catch(IOException ex) {
                        JOptionPane.showMessageDialog(null, "I/O error", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(fileLayerExport);


        fileMenu.addSeparator();


        JMenuItem fileExit = new JMenuItem("Exit");
        fileMenu.add(fileExit);
        fileExit.addActionListener(exitAction);

        JMenuItem dataRange = new JMenuItem("Plot range...");
        dataRange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                DbRangeDialog dbrd = new DbRangeDialog(thisFrame);
                if(dbrd.call(dbMin, dbMax)) {
                    dbMin = dbrd.getDbMin();
                    dbMax = dbrd.getDbMax();
                    p.setDbRange(dbMin, dbMax);
                    pfit.setDbRange(dbMin, dbMax);
                }
                dbrd.dispose();
            }
        });
        dataMenu.add(dataRange);
        dataMenu.addSeparator();

        JMenuItem dataPlot = new JMenuItem("Linear plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                thisFrame.plot(PlotStyle.LIN);
            }
        });
        dataMenu.add(dataPlot);
        dataPlot = new JMenuItem("Logarithmic plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                thisFrame.plot(PlotStyle.LOG);
            }
        });
        dataMenu.add(dataPlot);
        dataPlot = new JMenuItem("Sqrt-plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                thisFrame.plot(PlotStyle.SQRT);
            }
        });
        dataMenu.add(dataPlot);

        dataMenu.addSeparator();

        JMenuItem dataNoise = new JMenuItem("Add noise");
        dataNoise.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                PhotonLevelDialog dialog = new PhotonLevelDialog(thisFrame);
                Double level;
                if((level = dialog.call()) != null) {
                    GraphData data2 = data.addNoise(Math.exp(Math.log(10)*level/10));
                    data.newData(data2.alpha_0, data2.meas, data2.simul, data2.logformat);
                    /* ... */
                    p.draw();
                    pfit.draw();
                }
                dialog.dispose();
            }
        });
        dataMenu.add(dataNoise);

        JMenuItem helpAbout = new JMenuItem("About...");
        helpAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                XRDAboutDialog dialog = new XRDAboutDialog(thisFrame);
                dialog.call();
                dialog.dispose();
            }
        });
        helpMenu.add(helpAbout);

        JMenuItem helpException = new JMenuItem("Exception");
        helpException.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                throw new RuntimeException("foo");
            }
        });
        helpMenu.add(helpException);


        try {
            File f = new File("default.layers");
            if(f.exists())
                loadLayers(new File("default.layers"),false);
        }
        catch(LayerLoadException ex) {
            JOptionPane.showMessageDialog(null, "There was an error in the file default.layers:\n"+ex.getMessage(), "Error in default.layers", JOptionPane.ERROR_MESSAGE);
        }


        this.getContentPane().add(tabs);
        this.setJMenuBar(menuBar);
        this.pack();

        return true;
    }

    private static final Logger log = Logger.getLogger(XRDApp.class.getName());

    private static void handleException(Thread t, Throwable e) {
        log.log(Level.SEVERE,"Uncaught exception",e);
        JOptionPane.showMessageDialog(null,
                "There was an uncaught exception. Debugging information is stored in the\n"+
                "log file, which is usually named xrdlog0.txt. The log file should be\n"+
                "renamed since it will be overwritten the next time this program is started.", "Uncaught exception", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        Logger pkglog = Logger.getLogger(XRDApp.class.getPackage().getName());
        try {
            FileHandler handler = new FileHandler("xrdlog%u.txt");
            handler.setFormatter(new SimpleFormatter());
            pkglog.addHandler(handler);
        }
        catch(IOException ex) {
            log.log(Level.SEVERE,"Can't open log file",ex);
        }
        Thread.setDefaultUncaughtExceptionHandler(
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread t, Throwable e) {
                    handleException(t,e);
                }
            });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                XRDApp frame = new XRDApp();
                if(frame.construct())
                    frame.setVisible(true);
            }
        });
    }

    private void plot(PlotStyle style) {
        GraphData d;
        double[] meas, simul;
        double ymin = 0, ymax = 0;
        String ytitle;

        try {
            d = data.simulate(layers);
            meas = new double[d.meas.length];
            simul = new double[d.simul.length];
            System.arraycopy(d.meas, 0, meas, 0, meas.length);
            System.arraycopy(d.simul, 0, simul, 0, simul.length);

            switch(style) {
                case LOG:
                    for(int i=0; i<meas.length; i++) {
                        meas[i] = 10*Math.log(meas[i])/Math.log(10);
                        simul[i] = 10*Math.log(simul[i])/Math.log(10);
                        if(meas[i] < ymin && !Double.isInfinite(meas[i]))
                            ymin = meas[i];
                        if(simul[i] < ymin && !Double.isInfinite(simul[i]))
                            ymin = simul[i];
                        if(meas[i] > ymax && !Double.isInfinite(meas[i]))
                            ymax = meas[i];
                        if(simul[i] > ymax && !Double.isInfinite(simul[i]))
                            ymax = simul[i];
                    }
                    ytitle = "dB";
                    break;
                case SQRT:
                    for(int i=0; i<meas.length; i++) {
                        meas[i] = Math.sqrt(meas[i]);
                        simul[i] = Math.sqrt(simul[i]);
                    }
                    ytitle = "sqrt(reflectivity)";
                    break;
                default:
                    ytitle = "reflectivity";
                    break;
            }

            ArrayList<NamedArray> yarrays = new ArrayList<NamedArray>();
            yarrays.add(new NamedArray(1, simul, "Simulation"));
            yarrays.add(new NamedArray(1, meas, "Measurement"));

            //xyplot = chart.getXYPlot();
            /*xyplot.getDomainAxis().setAutoRange(false);
            xyplot.getDomainAxis().setRange(0,5);*/
            /*xyplot.getRangeAxis().setAutoRange(false);
            xyplot.getRangeAxis().setRange(-70,0);*/
            //chart.setAntiAlias(false); /* this is faster */
            new ChartFrame(this,"Reflectivity plot", 600, 400, true,
                    new DataArray(1, d.alpha_0), "degrees", yarrays, ytitle, ymin, ymax).setVisible(true);
        }
        catch(UnsupportedWavelength ex) {
            JOptionPane.showMessageDialog(null, "Unsupported wavelength", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch(SimulationException ex) {
            JOptionPane.showMessageDialog(null, "Simulation error", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }



    public File showFileDialog(JFrame owner, boolean save) {
        JFileChooser chooser = new JFileChooser();
        int retval;
        if(chooserDirectory != null)
            chooser.setCurrentDirectory(chooserDirectory);

        if(save)
            retval = chooser.showSaveDialog(owner);
        else
            retval = chooser.showOpenDialog(owner);

        if(retval == JFileChooser.APPROVE_OPTION) {
            chooserDirectory = chooser.getCurrentDirectory();
            return chooser.getSelectedFile();
        }
        else
            return null;
    }
}
