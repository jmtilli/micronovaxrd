package fi.micronova.tkk.xray.measimport;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;




public class ImportDialog extends JDialog {
    private JTextField minAngleF, maxAngleF, moduloF,
                       minNormalF, maxNormalF;
    private JCheckBox importSimul, normalizeCheck;
    ImportOptions options;

    public ImportDialog(Frame f, int nmeas, double min, double max, boolean includeSimul, boolean normalize) {
        super(f,"Import options",true);
        init(nmeas, min, max, includeSimul, normalize);
    }
    public ImportDialog(Dialog f, int nmeas, double min, double max, boolean includeSimul, boolean normalize) {
        super(f,"Import options",true);
        init(nmeas, min, max, includeSimul, normalize);
    }

    private void init(int nmeas, double min, double max, boolean includeSimul, boolean normalize)
    {
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JLabel("The file containts "+nmeas+" measurement points between angles "+
                                  String.format(Locale.US,"%.4f",min)
                                  +" and "+
                                  String.format(Locale.US,"%.4f",max)
                                  +"."),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("modulo"),c);
        moduloF = new JTextField("1",7);
        moduloF.setMinimumSize(moduloF.getPreferredSize());
        gridPanel.add(moduloF,c);
        gridPanel.add(new JLabel("angle min"),c);
        minAngleF = new JTextField("0.00",7);
        minAngleF.setMinimumSize(minAngleF.getPreferredSize());
        gridPanel.add(minAngleF,c);
        gridPanel.add(new JLabel("angle max"),c);
        maxAngleF = new JTextField("90.00",7);
        maxAngleF.setMinimumSize(maxAngleF.getPreferredSize());
        gridPanel.add(maxAngleF,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        normalizeCheck = new JCheckBox("Normalization:");
        normalizeCheck.setSelected(normalize);
        gridPanel.add(normalizeCheck,c);
        gridPanel.add(new JLabel("largest value between"),c);
        c.gridwidth = 1;
        gridPanel.add(new JLabel("min"),c);
        minNormalF = new JTextField("0.05",7);
        minNormalF.setMinimumSize(minNormalF.getPreferredSize());
        gridPanel.add(minNormalF,c);
        gridPanel.add(new JLabel("max"),c);
        maxNormalF = new JTextField("90.0",7);
        maxNormalF.setMinimumSize(maxNormalF.getPreferredSize());
        gridPanel.add(maxNormalF,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);

        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE,p.getPreferredSize().height));
        dialog.add(p);

        JPanel btnPanel = new JPanel();
        JButton btn;
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    int modulo = Integer.parseInt(moduloF.getText());
                    double minAngle = Double.parseDouble(minAngleF.getText());
                    double maxAngle = Double.parseDouble(maxAngleF.getText());
                    double minNormal = Double.parseDouble(minNormalF.getText());
                    double maxNormal = Double.parseDouble(maxNormalF.getText());
                    if(minNormal > maxNormal || minAngle > maxAngle || modulo <= 0)
                        throw new IllegalArgumentException();
                    options = new ImportOptions(modulo, minAngle, maxAngle, minNormal, maxNormal, importSimul.isSelected(), normalizeCheck.isSelected());
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid values", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                options = null;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        importSimul = new JCheckBox("Import simulation instead of measurement");
        JPanel importSimulPanel = new JPanel();
        importSimulPanel.add(importSimul);
        importSimulPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,importSimulPanel.getPreferredSize().height));
        if(includeSimul)
            dialog.add(importSimulPanel);
        dialog.add(Box.createVerticalGlue());
        dialog.add(btnPanel);
        pack();
    }
    public ImportOptions call() {
        this.options = null;
        setVisible(true);
        return this.options;
    }
}
