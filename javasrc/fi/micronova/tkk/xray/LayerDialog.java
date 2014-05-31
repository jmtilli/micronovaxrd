package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import fi.micronova.tkk.xray.xrdmodel.*;





/* A dialog for layer settings */
public class LayerDialog extends JDialog {
    private boolean succesful;
    private JTextField dmin, dval, dmax, pmin, pval, pmax, nameField, rmin, rval, rmax, whmin, whval, whmax;
    private JCheckBox dfit, pfit, rfit, whfit; /* fit enable checkboxes */
    private MaterialPanel mat1, mat2;
    private Layer layer;

    public LayerDialog(Frame f, Layer l, final MatDB db, final double lambda)
    {
        super(f,"Layer properties",true);
        init(l, db, lambda);
    }
    public LayerDialog(Dialog f, Layer l, final MatDB db, final double lambda)
    {
        super(f,"Layer properties",true);
        init(l, db, lambda);
    }
    private void init(Layer l, final MatDB db, final double lambda)
    {
        Container dialog;

        this.layer = l;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("name"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        nameField = new JTextField(l.getName(),25);
        nameField.setMinimumSize(nameField.getPreferredSize());
        gridPanel.add(nameField,c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("thickness (nm)"),c);
        gridPanel.add(new JLabel("min"),c);
        dmin = new JTextField(String.format(Locale.US,"%.6g",l.getThickness().getMin()*1e9),7);
        dmin.setMinimumSize(dmin.getPreferredSize());
        gridPanel.add(dmin,c);
        gridPanel.add(new JLabel("value"),c);
        dval = new JTextField(String.format(Locale.US,"%.6g",l.getThickness().getExpected()*1e9),7);
        dval.setMinimumSize(dval.getPreferredSize());
        gridPanel.add(dval,c);
        gridPanel.add(new JLabel("max"),c);
        dmax = new JTextField(String.format(Locale.US,"%.6g",l.getThickness().getMax()*1e9),7);
        dmax.setMinimumSize(dmax.getPreferredSize());
        gridPanel.add(dmax,c);
        dfit = new JCheckBox("fit");
        dfit.setSelected(l.getThickness().getEnabled());
        gridPanel.add(dfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("composition"),c);
        gridPanel.add(new JLabel("min"),c);
        pmin = new JTextField(String.format(Locale.US,"%.6g",l.getComposition().getMin()),7);
        pmin.setMinimumSize(pmin.getPreferredSize());
        gridPanel.add(pmin,c);
        gridPanel.add(new JLabel("value"),c);
        pval = new JTextField(String.format(Locale.US,"%.6g",l.getComposition().getExpected()),7);
        pval.setMinimumSize(pval.getPreferredSize());
        gridPanel.add(pval,c);
        gridPanel.add(new JLabel("max"),c);
        pmax = new JTextField(String.format(Locale.US,"%.6g",l.getComposition().getMax()),7);
        pmax.setMinimumSize(pmax.getPreferredSize());
        gridPanel.add(pmax,c);
        pfit = new JCheckBox("fit");
        pfit.setSelected(l.getComposition().getEnabled());
        gridPanel.add(pfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("degree of relaxation"),c);
        gridPanel.add(new JLabel("min"),c);
        rmin = new JTextField(String.format(Locale.US,"%.6g",l.getRelaxation().getMin()),7);
        rmin.setMinimumSize(rmin.getPreferredSize());
        gridPanel.add(rmin,c);
        gridPanel.add(new JLabel("value"),c);
        rval = new JTextField(String.format(Locale.US,"%.6g",l.getRelaxation().getExpected()),7);
        rval.setMinimumSize(rval.getPreferredSize());
        gridPanel.add(rval,c);
        gridPanel.add(new JLabel("max"),c);
        rmax = new JTextField(String.format(Locale.US,"%.6g",l.getRelaxation().getMax()),7);
        rmax.setMinimumSize(rmax.getPreferredSize());
        gridPanel.add(rmax,c);
        rfit = new JCheckBox("fit");
        rfit.setSelected(l.getRelaxation().getEnabled());
        gridPanel.add(rfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility factor"),c);
        gridPanel.add(new JLabel("min"),c);
        whmin = new JTextField(String.format(Locale.US,"%.6g",l.getSuscFactor().getMin()),7);
        whmin.setMinimumSize(whmin.getPreferredSize());
        gridPanel.add(whmin,c);
        gridPanel.add(new JLabel("value"),c);
        whval = new JTextField(String.format(Locale.US,"%.6g",l.getSuscFactor().getExpected()),7);
        whval.setMinimumSize(whval.getPreferredSize());
        gridPanel.add(whval,c);
        gridPanel.add(new JLabel("max"),c);
        whmax = new JTextField(String.format(Locale.US,"%.6g",l.getSuscFactor().getMax()),7);
        whmax.setMinimumSize(whmax.getPreferredSize());
        gridPanel.add(whmax,c);
        whfit = new JCheckBox("fit");
        whfit.setSelected(l.getSuscFactor().getEnabled());
        gridPanel.add(whfit,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("material 1"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        mat1 = new MaterialPanel(layer.getMat1(), lambda, db);
        gridPanel.add(mat1,c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("material 2"),c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        mat2 = new MaterialPanel(layer.getMat2(), lambda, db);
        gridPanel.add(mat2,c);



        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE,p.getPreferredSize().height));
        dialog.add(p);

        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    Material material1 = mat1.getMaterial();
                    Material material2 = mat2.getMaterial();
                    String name;
                    FitValue d, p, r, wh;


                    d = new FitValue(Double.parseDouble(dmin.getText())/1e9,
                                     Double.parseDouble(dval.getText())/1e9,
                                     Double.parseDouble(dmax.getText())/1e9,
                                     dfit.isSelected());
                    p = new FitValue(Double.parseDouble(pmin.getText()),
                                     Double.parseDouble(pval.getText()),
                                     Double.parseDouble(pmax.getText()),
                                     pfit.isSelected());
                    r = new FitValue(Double.parseDouble(rmin.getText()),
                                     Double.parseDouble(rval.getText()),
                                     Double.parseDouble(rmax.getText()),
                                     rfit.isSelected());
                    wh = new FitValue(Double.parseDouble(whmin.getText()),
                                      Double.parseDouble(whval.getText()),
                                      Double.parseDouble(whmax.getText()),
                                      whfit.isSelected());
                    name = nameField.getText();
                    /* This must be first, otherwise cancel can return invalid data */
                    layer.newValues(name, d, p, r, wh, material1, material2);
                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid boundary values", "Error", JOptionPane.ERROR_MESSAGE);
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
                succesful = false;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public boolean call() {
        this.succesful = false;
        setVisible(true);
        return succesful;
    }
}
