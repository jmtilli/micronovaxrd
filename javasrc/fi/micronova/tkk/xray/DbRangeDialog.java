package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for plot range */
public class DbRangeDialog extends JDialog {
    private boolean succesful;
    private JTextField minField, maxField;
    private double dbMin, dbMax;
    public DbRangeDialog(JFrame f)
    {
        super(f,"dB range",true);

        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("min"),c);
        minField = new JTextField("1",7);
        minField.setMinimumSize(minField.getPreferredSize());
        gridPanel.add(minField,c);
        gridPanel.add(new JLabel("max"),c);
        maxField = new JTextField("2",7);
        maxField.setMinimumSize(maxField.getPreferredSize());
        gridPanel.add(maxField,c);
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
                    double min,val,max;
                    min = Double.parseDouble(minField.getText());
                    max = Double.parseDouble(maxField.getText());

                    if(min > max)
                        throw new IllegalArgumentException();

                    dbMin = min;
                    dbMax = max;

                    succesful = true;
                    setVisible(false);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid boundary values", "Error", JOptionPane.ERROR_MESSAGE);
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
    public double getDbMin() {
      return this.dbMin;
    }
    public double getDbMax() {
      return this.dbMax;
    }
    public boolean call(double dbMin, double dbMax) {
        this.succesful = false;
        this.dbMin = dbMin;
        this.dbMax = dbMax;
        minField.setText(String.format(Locale.US,"%.6g",this.dbMin));
        maxField.setText(String.format(Locale.US,"%.6g",this.dbMax));
        setVisible(true);
        return succesful;
    }
}
