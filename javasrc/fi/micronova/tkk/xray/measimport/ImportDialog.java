package fi.micronova.tkk.xray.measimport;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;




public class ImportDialog extends JDialog {
    private JTextField minAngleF, maxAngleF, moduloF,
                       minNormalF, maxNormalF,
                       meascolF;
    private JCheckBox importSimul, normalizeCheck;
    ImportOptions options;

    public ImportDialog(Frame f, int nmeas, double min, double max, boolean[] valid, boolean normalize) {
        super(f,"Import options",true);
        init(nmeas, min, max, valid, normalize);
    }
    public ImportDialog(Dialog f, int nmeas, double min, double max, boolean[] valid, boolean normalize) {
        super(f,"Import options",true);
        init(nmeas, min, max, valid, normalize);
    }

    private void init(int nmeas, double min, double max, final boolean[] valid, boolean normalize)
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
        c.fill = GridBagConstraints.HORIZONTAL;
        gridPanel.add(new JLabel("The file containts "+nmeas+" measurement points between angles "+
                                  String.format(Locale.US,"%.4f",min)
                                  +" and "+
                                  String.format(Locale.US,"%.4f",max)
                                  +"."),c);
        if (valid.length > 0)
        {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridPanel.add(new JLabel("The file contains " + valid.length + " columns"), c);
        }

        c.fill = GridBagConstraints.NONE;
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
            class NotValidException extends Exception {}
            public void actionPerformed(ActionEvent ev) {
                try {
                    int modulo = Integer.parseInt(moduloF.getText());
                    double minAngle = Double.parseDouble(minAngleF.getText());
                    double maxAngle = Double.parseDouble(maxAngleF.getText());
                    double minNormal = Double.parseDouble(minNormalF.getText());
                    double maxNormal = Double.parseDouble(maxNormalF.getText());
                    int meascol = Integer.parseInt(meascolF.getText());
                    if(minNormal > maxNormal || minAngle > maxAngle || modulo <= 0 || meascol <= 1 || meascol > valid.length)
                        throw new IllegalArgumentException();
                    if (!valid[meascol-1])
                        throw new NotValidException();
                    options = new ImportOptions(modulo, minAngle, maxAngle, minNormal, maxNormal, meascol, normalizeCheck.isSelected());
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(NotValidException e) {
                    JOptionPane.showMessageDialog(null, "The selected column does not contain only numeric data", "Error", JOptionPane.ERROR_MESSAGE);
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
        //importSimul = new JCheckBox("Import simulation instead of measurement");
        meascolF = new JTextField("123456");
        meascolF.setMinimumSize(meascolF.getPreferredSize());
        meascolF.setPreferredSize(meascolF.getPreferredSize());
        if (valid.length == 3 && valid[0] && valid[1] && valid[2])
        {
            meascolF.setText("3");
        }
        else
        {
            meascolF.setText("2");
        }

        JPanel importSimulPanel = new JPanel();
        //importSimulPanel.add(importSimul);
        importSimulPanel.add(new JLabel("Measurement column (counts)"));
        importSimulPanel.add(meascolF);
        importSimulPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,importSimulPanel.getPreferredSize().height));
        if(valid.length > 2)
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
