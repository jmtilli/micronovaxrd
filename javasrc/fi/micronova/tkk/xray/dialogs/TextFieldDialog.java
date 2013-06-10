package fi.micronova.tkk.xray.dialogs;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/*
 * How to subclass:
 *
 * - Write nFields, getLabel and getDefault. getDefault and getLabel are
 *   called when showDialog is called.
 * - Write newValues. The "ownership" of the array values is given to
 *   newValues. Usually newValues should store the values somewhere.
 *   If the values are invalid, it must throw InvalidValues. For
 *   convenience, NumberFormatException and IllegalArgumentException
 *   are also handled.
 * - Write a call method to show the dialog. It should:
 *   * Initialize fields as necessary
 *   * Call showDialog
 *   * When showDialog returns, determine whether newValues was called
 *     and it returned succesfully. It it wasn't called or it threw
 *     InvalidValues always when it was called, the Cancel button was
 *     clicked. Otherwise the OK button was clicked. Usually a boolean
 *     field is used for this.
 *   * If newValues returned succesfully, return the values given to it.
 */

public abstract class TextFieldDialog extends JDialog {
    private JTextField[] fields;
    private JLabel[] labels;
    private int n;

    protected abstract int nFields();
    protected abstract String getLabel(int i);
    protected abstract String getDefault(int i);
    protected abstract void newValues(String[] values) throws InvalidValues;

    protected static class InvalidValues extends Exception {
        public InvalidValues(String s) {
            super(s);
        }
    };
    
    /* throws IllegalArgumentException */

    protected void showDialog() {
        for(int i=0; i<n; i++) {
            fields[i].setText(getDefault(i));
            labels[i].setText(getLabel(i));
        }
        pack();
        setVisible(true);
    }

    public TextFieldDialog(Dialog d, String title)
    {
        super(d,title,true);
        init();
    }
    public TextFieldDialog(Frame f, String title)
    {
        super(f,title,true);
        init();
    }
    private void init() {
        this.n = nFields();

        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;

        c.gridwidth = 1;

        fields = new JTextField[n];
        labels = new JLabel[n];
        for(int i=0; i<n; i++) {
            JLabel label = new JLabel("");
            JTextField field = new JTextField("",12);
            gridPanel.add(label,c);
            field.setMinimumSize(field.getPreferredSize());
            gridPanel.add(field,c);
            fields[i] = field;
            labels[i] = label;
        }

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
                try {
                    String[] s = new String[n];
                    for(int i=0; i<n; i++) {
                        s[i] = fields[i].getText();
                    }
                    newValues(s);
                    setVisible(false);
                }
                catch(InvalidValues e) {
                    JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Number format error", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(null, "Invalid data", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        //pack();
    }
}
