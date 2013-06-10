package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;





/* A dialog for layer settings */
public class MixtureDialog extends JDialog {
    private List<ConstituentPanel> constituents = new ArrayList<ConstituentPanel>();
    private JPanel panels;
    private double lambda;
    private MatDB db;
    private Mixture mix;

    class ConstituentPanel extends JPanel {
        private JTextField compositionField;
        private MaterialPanel matPanel;
        public ConstituentPanel(Mixture.Constituent c) {
            JButton removeButton;
            final ConstituentPanel thisPanel = this;

            setLayout(new FlowLayout());
            add(new JLabel("composition"));
            compositionField = new JTextField(c.p+"");
            add(compositionField);
            matPanel = new MaterialPanel(c.mat, lambda, db);
            add(matPanel);
            removeButton = new JButton("remove");
            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ev) {
                    constituents.remove(constituents.indexOf(thisPanel));
                    panels.remove(thisPanel);
                    panels.revalidate();
                    panels.repaint();
                }
            });
            add(removeButton);
        }
        public Mixture.Constituent getConstituent() throws NumberFormatException {
            double d = Double.parseDouble(compositionField.getText());
            Material mat = matPanel.getMaterial();
            return new Mixture.Constituent(d, mat);
        }
    };
    public Mixture getMixture() throws NumberFormatException, InvalidMixtureException, UnsupportedWavelength {
        List<Mixture.Constituent> constituents = new ArrayList<Mixture.Constituent>();
        Mixture m;
        for(ConstituentPanel c: this.constituents) {
            constituents.add(c.getConstituent());
        }
        m = new Mixture(constituents);
        if(m.materials.isEmpty())
            throw new InvalidMixtureException("Empty mixture");
        m.flatten().susc(lambda); /* test these now so they won't be thrown later */
        return m;
    }

    private void add(Mixture.Constituent c) {
        ConstituentPanel cp = new ConstituentPanel(c);
        constituents.add(cp);
        panels.add(cp);
        panels.revalidate();
        panels.repaint();
    }

    public MixtureDialog(final Frame f, List<Mixture.Constituent> oldMaterials, final double lambda, final MatDB db) {
        super(f,"Mixture",true);
        init(oldMaterials, lambda, db);
    }
    public MixtureDialog(final Dialog f, List<Mixture.Constituent> oldMaterials, final double lambda, final MatDB db) {
        super(f,"Mixture",true);
        init(oldMaterials, lambda, db);
    }

    private void init(List<Mixture.Constituent> oldMaterials, final double lambda, final MatDB db)
    {
        Container dialog;
        JButton addSimpleBtn, addMixBtn;
        final Dialog thisDialog = this;

        this.lambda = lambda;
        this.db = db;
        if(oldMaterials == null) {
            oldMaterials = new ArrayList<Mixture.Constituent>();
        }

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));

        JPanel addBtnPanel = new JPanel();
        addBtnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        addBtnPanel.add(addSimpleBtn = new JButton("Add simple material..."));
        addSimpleBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                MaterialDialog dialog =  new MaterialDialog(thisDialog, null, db, lambda);
                SimpleMaterial mat = dialog.call();
                dialog.dispose();
                if(mat != null) {
                    add(new Mixture.Constituent(0.0, mat));
                }
            }
        });
        addBtnPanel.add(addMixBtn = new JButton("Add mixture..."));
        addMixBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                MixtureDialog dialog = new MixtureDialog(thisDialog, null, lambda, db);
                Mixture mat = dialog.call();
                dialog.dispose();
                if(mat != null) {
                    add(new Mixture.Constituent(0.0, mat));
                }
            }
        });
        dialog.add(addBtnPanel);

        panels = new JPanel() {
            public Dimension getPreferredSize() {
                Dimension p = super.getPreferredSize();
                Dimension m = super.getMinimumSize();
                return new Dimension(Math.max(p.width, m.width), Math.max(p.height, m.height));
            }
        };
        panels.setLayout(new BoxLayout(panels,BoxLayout.PAGE_AXIS));
        for(Mixture.Constituent c: oldMaterials) {
            ConstituentPanel cp = new ConstituentPanel(c);
            panels.add(cp);
            constituents.add(cp);
        }
        panels.setMinimumSize(new Dimension(775, 150));
        JPanel fixer = new JPanel();
        fixer.setLayout(new BorderLayout());
        fixer.add(panels, BorderLayout.NORTH);
        fixer.add(new JPanel(), BorderLayout.CENTER);
        dialog.add(new JScrollPane(fixer));

        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    mix = getMixture();
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(UnsupportedWavelength e) {
                    JOptionPane.showMessageDialog(null, "Unsupported wavelength", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(InvalidMixtureException e) {
                    JOptionPane.showMessageDialog(null, "Invalid mixture", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                mix = null;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public Mixture call() {
        setVisible(true);
        return mix;
    }


    public static void main(String[] args) {
        try {
            LookupTable lookup = SFTables.defaultLookup();
            Mixture m = new Mixture(XMLUtil.parse(new FileInputStream("test.xml")).getDocumentElement(),lookup);
            MatDB db = new MatDB(new File("matdb.xml"),lookup);
            MixtureDialog dlg = new MixtureDialog((Frame)null, m.materials, 1.54056e-10, db);
            m = dlg.call();
            System.out.println(m.materials.size());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}








