package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import java.util.logging.*;
import java.util.zip.*;

import fi.micronova.tkk.xray.chart.*;
import fi.micronova.tkk.xray.chart.ChartFrame;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;
import fi.micronova.tkk.xray.measimport.*;
import fi.micronova.tkk.xray.dialogs.*;
import fi.micronova.tkk.xray.de.*;
import fi.micronova.tkk.xray.xrdde.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import org.xml.sax.SAXException;

import fi.iki.jmtilli.javaxmlfrag.*;



/*
 * TODO:
 * - more tools (add sl?, material info)
 *   - material info: wavelength, reflection(?), Bragg's angle, chi0, chih, chihinv, xyspace, zspace, poisson
 * - documentation (Javadoc, XRRD, short introduction for programmers & users)
 * - testing (both regression and real-life)
 * - performance (DE in Java?)
 */

/* TODO list:
 * - better debug information for red light
 * - better lambda checking (eg. when adding new layers or modifying previous layers)
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
 *
 * other things worth doing:
 * - code DE in Java so that we don't need Octave anymore
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
    private FitterInterface f = null;
    private String measPath = null; /* Path of imported measurement file */
    private final GraphData data;

    private LookupTable table;
    private MatDB db;

    private static final double Cu_K_alpha = 1.5405620e-10; /* This is the default wavelength */
    private Image green, yellow, red;
    private Image icon16, icon32, icon48, icon64, icon128;
    private LayerStack layers, emptyLayers;
    private JList<String> layeredList;
    private double dbMin = -5, dbMax = 50;

    private enum PlotStyle {LIN, LOG, SQRT, MRCHI2};

    private Properties props = new Properties();

    private AdvancedFitOptions opts = new AdvancedFitOptions();

    private final XRDApp xrd;

    private JComboBox<FitnessFunction> funcBox;
    private SpinnerNumberModel pModel, firstAngleModel, lastAngleModel, thresholdModel;


    public FittingErrorFunc func()
    {
        FitnessFunction func = (FitnessFunction)funcBox.getSelectedItem();
        FittingErrorFunc func2;
        double dBthreshold = (Double)thresholdModel.getNumber();
        switch (func)
        {
          case relchi2:
            func2 = new RelChi2FittingErrorFunc(Math.exp(Math.log(10)*dBthreshold/10));
            break;
          case logfitness:
            func2 = new LogFittingErrorFunc((Integer)pModel.getNumber());
            break;
          case rel:
            func2 = new RelFittingErrorFunc();
            break;
          case sqrtfitness:
            func2 = new SqrtFittingErrorFunc((Integer)pModel.getNumber());
            break;
          case chi2:
            func2 = new Chi2FittingErrorFunc();
            break;
          case relchi2transform:
            func2 = new RelChi2TransformFittingErrorFunc(Math.exp(Math.log(10)*dBthreshold/10), (Integer)pModel.getNumber());
            break;
          default:
            throw new IllegalArgumentException();
        }
        return func2;
    }
    public GraphData gd()
    {
        return data;
    }
    public GraphData croppedGd()
    {
        return data.crop((Double)firstAngleModel.getNumber(),
                         (Double)lastAngleModel.getNumber());
    }






    /* these must point always to the same object */
    private LayerPlotter pfit;
    private LayerPlotter p;
    private LayerStack fitLayers;

    private void defaultProp(String key, String value)
    {
        if (props.getProperty(key) == null)
        {
            props.setProperty(key, value);
        }
    }

    private boolean settingBool(String key, boolean default_value)
    {
        try {
            String val = props.getProperty(key);
            if (val == null)
            {
                return default_value;
            }
            return Boolean.parseBoolean(val);
        }
        catch (NumberFormatException ex)
        {
            return default_value;
        }
    }

    private int settingInt(String key, int default_value, int min, int max)
    {
        try {
            String val = props.getProperty(key);
            if (val == null)
            {
                return default_value;
            }
            int num = Integer.parseInt(val);
            if (num < min || num > max)
            {
                return default_value;
            }
            return num;
        }
        catch (NumberFormatException ex)
        {
            return default_value;
        }
    }

    private double settingDouble(String key, double default_value, double min, double max)
    {
        try {
            String val = props.getProperty(key);
            if (val == null)
            {
                return default_value;
            }
            double num = Double.parseDouble(val);
            if (num < min || num > max)
            {
                return default_value;
            }
            return num;
        }
        catch (NumberFormatException ex)
        {
            return default_value;
        }
    }

    private void loadLayers(File f, boolean enable_hint) throws LayerLoadException {
        try {
            FileInputStream fstr = new FileInputStream(f);
            try {
                BufferedInputStream bs = new BufferedInputStream(fstr);
                byte[] bytes = new byte[4];
                InputStream str = bs;
                bs.mark(4);
                bs.read(bytes, 0, 4);
                bs.reset();
                if (bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC&0xFF) &&
                    bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8))
                {
                  GZIPInputStream gz = new GZIPInputStream(bs);
                  str = gz;
                }
                else if (bytes[0] == 'P' && bytes[1] == 'K' &&
                         bytes[2] == 3 && bytes[3] == 4)
                {
                    ZipOneInputStream gz = new ZipOneInputStream(bs);
                    str = new BufferedInputStream(gz);
                }
                DocumentFragment doc_frag =
                    DocumentFragmentHandler.parseWhole(str);
                doc_frag.assertTag("model");
                LayerStack newLayers = new LayerStack(doc_frag, table);
                layers.deepCopyFrom(newLayers);
            }
            finally {
                fstr.close();
            }
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
            throw new LayerLoadException("Invalid physical XML format");
        }
        catch(XMLException ex) {
            throw new LayerLoadException("Invalid logical XML format");
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

    private void maybeSetIconImages(java.util.List<? extends Image> icons)
    {
        try {
            java.lang.reflect.Method m;
            m = this.getClass().getMethod("setIconImages", java.util.List.class);
            m.invoke(this, icons);
        }
        catch(Throwable t) {}
    }

    private static String getDir()
    {
        try {
            String path = XRDApp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File f = new File(path);
            if (!f.isDirectory())
            {
                path = f.getParent();
            }
            return path;
        }
        catch (java.net.URISyntaxException ex)
        {
            return ".";
        }
    }

    public void useSimulationAsMeasurement()
    {
        try {
            GraphData d = data.simulate(layers);
            loadMeasurement(d.alpha_0, d.simul, new ImportOptions(1, 0, 90, 0, 90, 2, false, false)); /* no normalization */
            measPath = null;
            setTitle("XRD");
        }
        catch(SimulationException ex) {
            JOptionPane.showMessageDialog(null, "Simulation error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public void addNoise(double level_db)
    {
        GraphData data2 = data.addNoise(Math.exp(Math.log(10)*level_db/10));
        data.newData(data2.alpha_0, data2.meas, data2.simul, data2.logformat);
        /* ... */
        p.draw();
        pfit.draw();
    }

    public XRDApp() {
        super("XRD");
        this.xrd = this;
        data = new GraphData(null, null, null, false);
    }

    private void loadMeasurement(double[] alpha_0, double[] meas, ImportOptions opts) {
        double max = 0;
        if (opts.divAngleByTwo)
        {
            for(int i=0; i<alpha_0.length; i++) {
                alpha_0[i] /= 2;
            }
        }
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
        if (bound2 == 0)
        {
            return;
        }
        data.newData(new_alpha_0, new_meas, null, false);

        p.draw();
        pfit.draw();
    }





    private boolean construct() {
        /* Load atomic masses and scattering factors */
        try {
            table = SFTables.defaultLookup(getDir());
        }
        catch(Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Can't load atomic databases",
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            db = new MatDB(new File(getDir(), "matdb.xml"),table);
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

        try {
            File f = new File(getDir(), "default.properties");
            if (f.exists())
            {
                FileInputStream pfin = new FileInputStream(f);
                try {
                    props.load(pfin);
                }
                finally {
                    pfin.close();
                }
            }
            defaultProp("autofit.popsize", "-10");
            defaultProp("autofit.iters", "500");
            defaultProp("autofit.firstAngle", "0.00");
            defaultProp("autofit.lastAngle", "90.0");
            defaultProp("autofit.algorithm", "0");
            defaultProp("autofit.fitnessFunc", "0");
            defaultProp("autofit.thresRelF", "20");
            defaultProp("autofit.pNorm", "2");
            defaultProp("autofit.k_m", "0.7");
            defaultProp("autofit.k_r", "0.85");
            defaultProp("autofit.p_m", "0.5");
            defaultProp("autofit.c_r", "0.5");
            defaultProp("autofit.lambda", "1.0");
            defaultProp("autofit.reportPerf", "false");
            defaultProp("plot.dbMin", "-5");
            defaultProp("plot.dbMax", "50");
            opts.km = Double.parseDouble(props.getProperty("autofit.k_m"));
            if (opts.km <= 0 || opts.km >= 1)
            {
                throw new NumberFormatException();
            }
            opts.kr = Double.parseDouble(props.getProperty("autofit.k_r"));
            if (opts.kr <= 0 || opts.kr >= 1)
            {
                throw new NumberFormatException();
            }
            opts.pm = Double.parseDouble(props.getProperty("autofit.p_m"));
            if (opts.pm <= 0 || opts.pm >= 1)
            {
                throw new NumberFormatException();
            }
            opts.cr = Double.parseDouble(props.getProperty("autofit.c_r"));
            if (opts.cr <= 0 || opts.cr >= 1)
            {
                throw new NumberFormatException();
            }
            opts.lambda = Double.parseDouble(props.getProperty("autofit.lambda"));
            if (opts.lambda < 0 || opts.lambda > 1)
            {
                throw new NumberFormatException();
            }
            opts.reportPerf = Boolean.parseBoolean(props.getProperty("autofit.reportPerf"));
            dbMin = Double.parseDouble(props.getProperty("plot.dbMin"));
            dbMax = Double.parseDouble(props.getProperty("plot.dbMax"));
        }
        catch(NumberFormatException ex) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            ex.printStackTrace( printWriter );
            printWriter.flush();
            String stackTrace = writer.toString();
            JOptionPane.showMessageDialog(null,
                "Can't load properties\n" + stackTrace,
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch(IllegalArgumentException ex) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            ex.printStackTrace( printWriter );
            printWriter.flush();
            String stackTrace = writer.toString();
            JOptionPane.showMessageDialog(null,
                "Can't load properties\n" + stackTrace,
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        catch(IOException ex) {
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter( writer );
            ex.printStackTrace( printWriter );
            printWriter.flush();
            String stackTrace = writer.toString();
            JOptionPane.showMessageDialog(null,
                "Can't load properties\n" + stackTrace,
                "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }




        final JTabbedPane tabs = new JTabbedPane();
        JPanel layered, graph;
        final JPanel fit = new JPanel();
        JPanel wlPanel, layeredPanel;
        JPanel layerButtonPanel;
        JTextField wl1, wl2, wl3;
        JScrollPane layeredScroll;
        //JTabbedPane sliderPane = new JTabbedPane();
        JPanel sliderPanel = new JPanel();

        emptyLayers = new LayerStack(Cu_K_alpha, table);
        layers = emptyLayers.deepCopy();

        layeredList = new JList<String>(layers.listModel);
        final XRDApp thisFrame = this;

        /* TODO ScrollbarUpdater */
        //layers.addListDataListener(new ScrollbarUpdater(layers, sliderPane));
        //layers.addListDataListener(new ScrollbarUpdater(layers, sliderPanel));
        new ScrollbarUpdater(this, layers, sliderPanel);

        layered = new JPanel();
        graph = new JPanel();

        layered.setLayout(new BorderLayout());
        graph.setLayout(new BorderLayout());
        fit.setLayout(new BorderLayout());



        final Runnable errTask = new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null,
                    "There was an error with fitting.",
                    "Fitting error", JOptionPane.ERROR_MESSAGE);
            }
        };



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
                    l = new Layer("New layer", new FitValue(0,50e-9,100e-9), new FitValue(0,0.5,1), new FitValue(0,0,1,false), new FitValue(0,1,1,false), db.materials.get(0), db.materials.get(0));
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
                    Layer l = layers.getElementAt(i).deepCopy(
                        new HashMap<FitValue, Integer>(),
                        new HashMap<Integer, FitValue>());
                    layers.add(l,layers.getSize());
                }
            }
        });
        layerButtonPanel.add(b);

        b = new JButton("Link params...");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                if (i2.length < 2)
                {
                    JOptionPane.showMessageDialog(
                      null,
                      "Select more than two layers by holding the CTRL\n" +
                      "button down while clicking the layers on the list\n" +
                      "to use the parameter linking feature",
                      "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LinkDialog d = new LinkDialog(thisFrame, "Unlink parameters");
                d.call();
                if (!d.ok())
                {
                    return;
                }
                layeredList.clearSelection();
                Layer firstLayer = layers.getElementAt(i2[0]);
                for(int i: i2) {
                    Layer oldLayer = layers.getElementAt(i);
                    Layer newLayer = oldLayer.deepCopy(
                        new HashMap<FitValue, Integer>(),
                        new HashMap<Integer, FitValue>());
                    layers.remove(i);
                    if (d.d())
                    {
                        newLayer.setThicknessObject(firstLayer.getThickness());
                    }
                    else
                    {
                        newLayer.setThicknessObject(oldLayer.getThickness());
                    }
                    if (d.p())
                    {
                        newLayer.setCompositionObject(firstLayer.getComposition());
                    }
                    else
                    {
                        newLayer.setCompositionObject(oldLayer.getComposition());
                    }
                    if (d.r())
                    {
                        newLayer.setRelaxationObject(firstLayer.getRelaxation());
                    }
                    else
                    {
                        newLayer.setRelaxationObject(oldLayer.getRelaxation());
                    }
                    if (d.wh())
                    {
                        newLayer.setSuscFactorObject(firstLayer.getSuscFactor());
                    }
                    else
                    {
                        newLayer.setSuscFactorObject(oldLayer.getSuscFactor());
                    }
                    layers.add(newLayer,i);
                }
            }
        });
        layerButtonPanel.add(b);

        b = new JButton("Unlink params...");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int[] i2 = layeredList.getSelectedIndices();
                LinkDialog d = new LinkDialog(thisFrame, "Unlink parameters");
                d.call();
                if (!d.ok())
                {
                    return;
                }
                layeredList.clearSelection();
                for(int i: i2) {
                    Layer oldLayer = layers.getElementAt(i);
                    Layer newLayer = oldLayer.deepCopy(
                        new HashMap<FitValue, Integer>(),
                        new HashMap<Integer, FitValue>());
                    if (!d.d())
                    {
                        newLayer.setThicknessObject(oldLayer.getThickness());
                    }
                    if (!d.p())
                    {
                        newLayer.setCompositionObject(oldLayer.getComposition());
                    }
                    if (!d.r())
                    {
                        newLayer.setRelaxationObject(oldLayer.getRelaxation());
                    }
                    if (!d.wh())
                    {
                        newLayer.setSuscFactorObject(oldLayer.getSuscFactor());
                    }
                    layers.remove(i);
                    layers.add(newLayer,i);
                }
            }
        });
        layerButtonPanel.add(b);


        b = new JButton("Optics...");
        b.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int i = layeredList.getSelectedIndex();
                int[] i2 = layeredList.getSelectedIndices();
                if(i < 0 || i >= layers.getSize() || i2.length != 1)
                    JOptionPane.showMessageDialog(null, "Can't display optical information", "Error", JOptionPane.ERROR_MESSAGE);
                else {
                    InfoDialog d = new InfoDialog(thisFrame);
                    d.call(layers, i);
                    d.dispose();
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
            icon16 = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("micronovaxrd16.png"));
            icon32 = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("micronovaxrd32.png"));
            icon48 = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("micronovaxrd48.png"));
            icon64 = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("micronovaxrd64.png"));
            icon128 = ImageIO.read(fitLight.getClass().getClassLoader().getResourceAsStream("micronovaxrd128.png"));
        }
        catch(IOException ex) {
            JOptionPane.showMessageDialog(null, "can't read png files", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        fitLight.newImage(green);
        northWlPanel.add(wlPanel,BorderLayout.CENTER);
        northWlPanel.add(lightPanel,BorderLayout.EAST);
        fit.add(northWlPanel,BorderLayout.NORTH);

        maybeSetIconImages(Arrays.asList(new Image[]{icon16, icon32, icon48, icon64, icon128}));



        /* -------------------- measurement loading -------------------- */

        /* these must be created before automatic fit creation code */

        final JMenuItem fileLoadMeas = new JMenuItem("Load measurement...");
        final JMenuItem fileLoadEmpty = new JMenuItem("Load empty measurement...");
        final JMenuItem fileLoadAscii = new JMenuItem("Load ASCII export...");
        final JMenuItem fileSwap = new JMenuItem("Use simulation as measurement");
        final JMenuItem fileLayerExport = new JMenuItem("Export layers to text file...");


        /* ------------------- automatic fit -------------------- */
        //final JPlotArea fitPlotArea = new JPlotArea();
        final XChartArea fitPlotArea = new XChartArea();
        fitLayers = emptyLayers.deepCopy();

        pfit = new LayerPlotter(fitPlotArea, fitPlotLight, fitLayers, data, green, yellow, red, dbMin, dbMax);

        fitLayers.addLayerModelListener(new LayerModelAdapter() {
            public void modelPropertyChanged(EventObject ev) {
                final double FWHM_SCALE = 2*Math.sqrt(2*Math.log(2));

                fitWlLabel.setText(String.format(Locale.US,"%.6f",fitLayers.getLambda()*1e9)+" nm");
                fitConvLabel.setText(String.format(Locale.US,"%.6f",fitLayers.getStdDev().getExpected()*FWHM_SCALE*180/Math.PI)+"\u00B0");
                fitNormLabel.setText(String.format(Locale.US,"%.3f",fitLayers.getProd().getExpected())+" dB "+(fitLayers.getProd().getEnabled()?"(fit)":"(no)"));
                fitSumLabel.setText(String.format(Locale.US,"%.3f",fitLayers.getSum().getExpected())+" dB "+(fitLayers.getSum().getEnabled()?"(fit)":"(no)"));
            }
        });
        //fitLayers.invalidate(this);

        GridBagConstraints c = new GridBagConstraints();
        JPanel fitSouth = new JPanel();
        JList<String> fitList = new JList<String>(fitLayers.listModel);
        JScrollPane fitListPane = new JScrollPane(fitList);
        fitListPane.setPreferredSize(new Dimension(400,190));

        fitPlotArea.setPreferredSize(new Dimension(600,400));
        fitPlotArea.setPreferredSize(new Dimension(600,400));
        JPanel fitPlotWrapper = new JPanel();
        fitPlotWrapper.setLayout(new BorderLayout());
        fitPlotWrapper.add(fitPlotArea, BorderLayout.CENTER);
        //fitPlotWrapper.add(new JCenterImageArea("meassimullegend.png", 2),
        //                   BorderLayout.SOUTH);
        fit.add(fitPlotWrapper,BorderLayout.CENTER);
        JPanel plotControls = new JPanel();
        plotControls.setLayout(new GridBagLayout());
        final JButton exportButton = new JButton("Export");
        final JButton importButton = new JButton("Import");
        importButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                fitLayers.deepCopyFrom(layers);
            }
        });

        final JButton startFitButton = new JButton("Start");
        final JButton stopFitButton = new JButton("Stop");
        final JButton advancedButton = new JButton("Opts");
        final SpinnerNumberModel popSizeModel = new SpinnerNumberModel(settingInt("autofit.popsize", -10, -200, 2000),-200,2000,1);
        final SpinnerNumberModel iterationsModel = new SpinnerNumberModel(settingInt("autofit.iters", 500, 1, 10000),1,10000,1);
        pModel = new SpinnerNumberModel(settingInt("autofit.pNorm", 2, 1, 10),1,10,1);
        final SpinnerNumberModel autostopModel = new SpinnerNumberModel(settingInt("autofit.autostopFigures", 6, 2, 10),2,10,1);
        firstAngleModel = new SpinnerNumberModel(settingDouble("autofit.firstAngle", 0, 0, 90),0,90,0.01);
        lastAngleModel = new SpinnerNumberModel(settingDouble("autofit.lastAngle", 90, 0, 90),0,90,0.01);
        thresholdModel = new SpinnerNumberModel(settingDouble("autofit.thresRelF", 20, -500, 500),-500,500,0.1);
        final JComboBox<Algorithm> algoBox = new JComboBox<Algorithm>(Algorithm.values());
        final JCheckBox autostop = new JCheckBox("automatic fit stop with figures");
        algoBox.setSelectedItem(Algorithm.values()[settingInt("autofit.algorithm", 0, 0, Algorithm.values().length)]);
        funcBox = new JComboBox<FitnessFunction>(FitnessFunction.values());
        funcBox.setSelectedItem(FitnessFunction.values()[settingInt("autofit.fitnessFunc", 0, 0, FitnessFunction.values().length)]);
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
                            tabs.setTitleAt(2, "Automatic fit");
                            importButton.setEnabled(true);
                            //exportButton.setEnabled(true);
                            fileLoadMeas.setEnabled(true);
                            fileLoadAscii.setEnabled(true);
                            fileLoadEmpty.setEnabled(true);
                            fileSwap.setEnabled(true);
                            pfit.setAdditionalTitle("");
                            if (msg != null && !msg.equals(""))
                            {
                                JOptionPane.showMessageDialog(null,
                                    msg,
                                    "Fitting performance",
                                    JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    };
                    final Runnable errTask2 = new Runnable() {
                        public void run() {
                            f = null;
                            startFitButton.setEnabled(true);
                            stopFitButton.setEnabled(false);
                            tabs.setTitleAt(2, "Automatic fit");
                            importButton.setEnabled(true);
                            //exportButton.setEnabled(true);
                            fileLoadMeas.setEnabled(true);
                            fileLoadAscii.setEnabled(true);
                            fileLoadEmpty.setEnabled(true);
                            fileSwap.setEnabled(true);
                            pfit.setAdditionalTitle("");
                            pfit.draw();
                            errTask.run();
                        }
                    };
                    LayerTask plotTask = new LayerTask() {
                        public void run(LayerStack s, String msg) {
                            pfit.setAdditionalTitle(msg);
                            fitLayers.deepCopyFrom(s);
                        }
                    };
                    Algorithm algo = (Algorithm)algoBox.getSelectedItem();
                    f = new JavaFitter(xrd, fitLight, data, endTask, plotTask, errTask2, fitLayers,
                                       (Integer)popSizeModel.getNumber(), (Integer)iterationsModel.getNumber(),
                                       (Double)firstAngleModel.getNumber(), (Double)lastAngleModel.getNumber(),
                                       green, yellow, algo, autostop.isSelected(), (Integer)autostopModel.getNumber(), opts);//, nonlinBox.isSelected());
                    startFitButton.setEnabled(false);
                    stopFitButton.setEnabled(true);
                    tabs.setTitleAt(2, "Automatic fit (*)");
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
                catch(Exception ex) {
                    errTask.run();
                }
            }
        });
        advancedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                AdvancedFitDialog diag = new AdvancedFitDialog(thisFrame);
                diag.call(opts);
            }
        });
        exportButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                layers.deepCopyFrom(fitLayers);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        c.ipadx = c.ipady = 1;
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(1,1,1,1);
        c.gridwidth = 1;
        buttonPanel.add(importButton);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        buttonPanel.add(startFitButton);
        c.gridwidth = 1;
        buttonPanel.add(exportButton);
        c.gridwidth = 1;
        stopFitButton.setEnabled(false);
        buttonPanel.add(stopFitButton);

        c.gridwidth = GridBagConstraints.REMAINDER;
        stopFitButton.setEnabled(false);
        buttonPanel.add(advancedButton);
        plotControls.add(buttonPanel, c);


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
        plotControls.add(new JLabel("Thres. rel.f. (dB)"),c);
        c.gridwidth = 1;
        plotControls.add(new JSpinner(thresholdModel),c);

        c.gridwidth = 1;
        plotControls.add(new JLabel("p-norm"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(pModel),c);

        c.gridwidth = 3;
        autostop.setSelected(settingBool("autofit.autostop", true));
        plotControls.add(autostop, c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        plotControls.add(new JSpinner(autostopModel),c);



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
        final XChartArea plotarea = new XChartArea();

        p = new LayerPlotter(plotarea, light, layers, data, green, yellow, red, dbMin, dbMax);

        p.setDbRange(dbMin, dbMax);
        pfit.setDbRange(dbMin, dbMax);

        plotarea.setPreferredSize(new Dimension(600,400));
        graph.add(plotarea,BorderLayout.CENTER);
        //graph.add(plotarea,BorderLayout.CENTER);
        JPanel plotWrapper = new JPanel();
        plotWrapper.setLayout(new BorderLayout());
        plotWrapper.add(plotarea, BorderLayout.CENTER);
        //plotWrapper.add(new JCenterImageArea("meassimullegend.png", 2),
        //                   BorderLayout.SOUTH);
        graph.add(plotWrapper,BorderLayout.CENTER);
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
                if(f != null)
                    f.close();
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
                        FileInputStream fstr = new FileInputStream(chooser.getSelectedFile());
                        try {
                            importdat = PANImport.PANImport(fstr);
                        }
                        finally {
                            fstr.close();
                        }

                        for (int i = 1; i < importdat.arrays.length; i++)
                        {
                            assert(importdat.arrays[i].length == importdat.arrays[0].length);
                            assert(importdat.arrays[i].length > 0);
                        }


                        ImportDialog dialog = new ImportDialog(thisFrame,importdat.arrays[0].length,
                            importdat.arrays[0][0], importdat.arrays[0][importdat.arrays[0].length-1], importdat.valid, false, importdat.isTwoTheta);
                        ImportOptions opts = dialog.call();
                        dialog.dispose();
                        if(opts == null)
                            return;

                        loadMeasurement(importdat.arrays[0], importdat.arrays[opts.meascol-1], opts);

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
                useSimulationAsMeasurement();
            }
        });

        fileMenu.add(fileSwap);
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
                        if (tabs.getSelectedComponent() == fit)
                        {
                            JOptionPane.showMessageDialog(
                                null,
                                "The model was loaded to the manual fit " +
                                "tab.\n\nYou're on the automatic fit tab.\n\n" +
                                "To import " +
                                "the model to the automatic fit tab, press " +
                                "\"Import model\"",
                                "Info", JOptionPane.INFORMATION_MESSAGE);
                        }
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
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be saved is the one " +
                            "on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to save?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
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
                        File f = chooser.getSelectedFile();
                        String fs = f.getPath();
                        FileOutputStream fstr = new FileOutputStream(f);
                        OutputStream str = fstr;
                        if (fs.endsWith(".gz"))
                        {
                            str = new GZIPOutputStream(fstr);
                        }
                        else if (fs.endsWith(".gz"))
                        {
                            String plain = fs.substring(0, fs.length()-4);
                            str = new ZipOneOutputStream(fstr, plain);
                        }
                        try {
                            /*
                            additional_data.put("measPath",measPath == null ? "" : measPath);
                            Fcode.fencode(layers.structExport(additional_data), fstr);
                            */
    
                            DocumentFragment doc = new DocumentFragment("model");
                            doc.setThisRow(layers);
                            doc.unparse(XMLDocumentType.WHOLE, str);
                        }
                        finally {
                            str.flush();
                            if (str != fstr)
                            {
                                str.close();
                            }
                            fstr.close();
                        }
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
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be exported is the " +
                            "one on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to export?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
                if(chooserDirectory != null)
                    chooser.setCurrentDirectory(chooserDirectory);
                if(chooser.showSaveDialog(thisFrame) == JFileChooser.APPROVE_OPTION) {
                    chooserDirectory = chooser.getCurrentDirectory();
                    try {
                        FileOutputStream fstr = new FileOutputStream(chooser.getSelectedFile());
                        try {
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
                        finally {
                            fstr.close();
                        }
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
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be plotted is the one " +
                            "on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to plot?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
                thisFrame.plot(PlotStyle.LIN);
            }
        });
        dataMenu.add(dataPlot);
        dataPlot = new JMenuItem("Logarithmic plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be plotted is the one " +
                            "on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to plot?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
                thisFrame.plot(PlotStyle.LOG);
            }
        });
        dataMenu.add(dataPlot);
        dataPlot = new JMenuItem("Sqrt-plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be plotted is the one " +
                            "on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to plot?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
                thisFrame.plot(PlotStyle.SQRT);
            }
        });
        dataMenu.add(dataPlot);
        dataPlot = new JMenuItem("MRchi2-plot");
        dataPlot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (    tabs.getSelectedComponent() == fit
                    &&  fitLayers != null
                    && !fitLayers.equals(layers))
                {
                    if (JOptionPane.showConfirmDialog(
                            null,
                            "The model that's going to be plotted is the one " +
                            "on the manual fit tab and that's different " +
                            "from the model on the automatic fit tab.\n\n" +
                            "You're on the automatic fit tab. To export " +
                            "the model to the manual fit tab, press " +
                            "\"Export model\".\n\nDo you still want to plot?",
                            "Question", JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE)
                      != JOptionPane.YES_OPTION)
                    {
                      return;
                    }
                }
                thisFrame.plot(PlotStyle.MRCHI2);
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
                    addNoise(level);
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
            File f = new File(getDir(), "default.layers.gz");
            if(f.exists())
                loadLayers(new File(getDir(), "default.layers.gz"),false);
            else
            {
                f = new File(getDir(), "default.layers");
                if(f.exists())
                    loadLayers(new File(getDir(), "default.layers"),false);
            }
        }
        catch(LayerLoadException ex) {
            JOptionPane.showMessageDialog(null, "There was an error in the file default.layers(.gz):\n"+ex.getMessage(), "Error in default.layers(.gz)", JOptionPane.ERROR_MESSAGE);
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

            double dBthreshold = (Double)thresholdModel.getNumber();
            RelChi2TransformFittingErrorFunc func = new RelChi2TransformFittingErrorFunc(Math.exp(Math.log(10)*dBthreshold/10), (Integer)pModel.getNumber());

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
                        if(Double.isInfinite(meas[i]))
                            meas[i] = -100;
                        if(Double.isInfinite(simul[i]))
                            simul[i] = -100;
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
                case MRCHI2:
                    for(int i=0; i<meas.length; i++) {
                        meas[i] = func.transform(meas[i]);
                        simul[i] = func.transform(simul[i]);
                    }
                    ytitle = "MRchi2(reflectivity)";
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
                    new DataArray(1, d.alpha_0), "degrees", yarrays, ytitle, ymin, ymax, "meassimullegend.png").setVisible(true);
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
