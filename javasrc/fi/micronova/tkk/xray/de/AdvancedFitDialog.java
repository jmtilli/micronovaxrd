package fi.micronova.tkk.xray.de;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for layer settings */
public class AdvancedFitDialog extends JDialog {
    private boolean succesful;
    private JTextField kmField, krField, pmField, crField, lambdaField;
    private AdvancedFitOptions v;

    private void initialize()
    {
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("k_m"),c);
        kmField = new JTextField("0.7",7);
        kmField.setMinimumSize(kmField.getPreferredSize());
        gridPanel.add(kmField,c);
        gridPanel.add(new JLabel("k_r"),c);
        krField = new JTextField("0.85",7);
        krField.setMinimumSize(krField.getPreferredSize());
        gridPanel.add(krField,c);
        gridPanel.add(new JLabel("p_m"),c);
        pmField = new JTextField("0.5",7);
        pmField.setMinimumSize(pmField.getPreferredSize());
        gridPanel.add(pmField,c);
        gridPanel.add(new JLabel("c_r"),c);
        crField = new JTextField("0.5",7);
        crField.setMinimumSize(pmField.getPreferredSize());
        gridPanel.add(crField,c);
        gridPanel.add(new JLabel("lambda"),c);
        lambdaField = new JTextField("1.0",7);
        lambdaField.setMinimumSize(lambdaField.getPreferredSize());
        gridPanel.add(lambdaField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        gridPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,gridPanel.getPreferredSize().height));
        dialog.add(gridPanel);


        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                double deltaPerRho, betaPerRho;
                try {
                    double km, kr, pm, cr, lambda;
                    km = Double.parseDouble(kmField.getText());
                    kr = Double.parseDouble(krField.getText());
                    pm = Double.parseDouble(pmField.getText());
                    cr = Double.parseDouble(crField.getText());
                    lambda = Double.parseDouble(lambdaField.getText());

                    if(km <= 0 || km >= 1 || kr <= 0 || kr >= 1 ||
                       pm <= 0 || pm >= 1 || cr <= 0 || cr >= 1 ||
                       lambda < 0 || lambda > 1)
                        throw new IllegalArgumentException();

                    v.km = km;
                    v.kr = kr;
                    v.pm = pm;
                    v.cr = cr;
                    v.lambda = lambda;

                    succesful = true;
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
                succesful = false;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public AdvancedFitDialog(Frame f)
    {
        super(f,"Advanced fit options",true);
        initialize();
    }
    public AdvancedFitDialog(Dialog d)
    {
        super(d,"Advanced fit options",true);
        initialize();
    }
    public boolean call(AdvancedFitOptions v) {
        this.succesful = false;
        this.v = v;
        kmField.setText(String.format(Locale.US,"%.6g",v.km));
        krField.setText(String.format(Locale.US,"%.6g",v.kr));
        pmField.setText(String.format(Locale.US,"%.6g",v.pm));
        crField.setText(String.format(Locale.US,"%.6g",v.cr));
        lambdaField.setText(String.format(Locale.US,"%.6g",v.lambda));
        setVisible(true);
        return succesful;
    }
}
