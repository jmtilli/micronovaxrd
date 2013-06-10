package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;



/*
class DepthProfileDialog extends DataDialog {
    public DepthProfileDialog(JFrame f, double defaultMin, double defaultMax) {
        super(f, "Depth profile options", "depth", "nm", 1000, defaultMin, defaultMax, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
    }
}
*/




class DataDialog extends JDialog {
    private JTextField minF, maxF, ndataF;
    DataOptions options;
    public DataDialog(JFrame f, String title, String measure, String unit, int defaultN, double defaultMin, double defaultMax, final double validMin, final double validMax)
    {
        super(f,title,true);
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;
        gridPanel.add(new JLabel("number of data points"),c);
        ndataF = new JTextField(""+defaultN,7);
        ndataF.setMinimumSize(ndataF.getPreferredSize());
        gridPanel.add(ndataF,c);
        gridPanel.add(new JLabel(measure+" min" + ((unit!=null)?(" ("+unit+")"):"")),c);
        minF = new JTextField(String.format(Locale.US,"%.4f",defaultMin),7);
        minF.setMinimumSize(minF.getPreferredSize());
        gridPanel.add(minF,c);
        gridPanel.add(new JLabel(measure+" max" + ((unit!=null)?(" ("+unit+")"):"")),c);
        maxF = new JTextField(String.format(Locale.US,"%.4f",defaultMax),7); 
        maxF.setMinimumSize(maxF.getPreferredSize());
        gridPanel.add(maxF,c);
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
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    int ndata = Integer.parseInt(ndataF.getText());
                    double min = Double.parseDouble(minF.getText());
                    double max = Double.parseDouble(maxF.getText());
                    if(min > max || ndata < 2 || min < validMin || max > validMax)
                        throw new IllegalArgumentException();
                    options = new DataOptions(ndata, min, max);
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
        dialog.add(btnPanel);
        pack();
    }
    public DataOptions call() {
        this.options = null;
        setVisible(true);
        return this.options;
    }
}
