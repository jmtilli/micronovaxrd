package fi.micronova.tkk.xray;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import fi.micronova.tkk.xray.xrdmodel.*;





/* A dialog for layer settings */
public class MaterialDialog extends JDialog {
    private double lambda;
    private JComboBox<SimpleMaterial> materials;
    private SimpleMaterial mat;

    public MaterialDialog(Frame f, SimpleMaterial oldMat, MatDB db, final double lambda)
    {
        super(f,"Material selector",true);
        init(oldMat, db, lambda);
    }
    public MaterialDialog(Dialog f, SimpleMaterial oldMat, MatDB db, final double lambda)
    {
        super(f,"Material selector",true);
        init(oldMat, db, lambda);
    }

    private void init(SimpleMaterial oldMat, MatDB db, final double lambda)
    {
        materials = new JComboBox<SimpleMaterial>(new Vector<SimpleMaterial>(db.materials));
        materials.setSelectedItem(oldMat);

        this.lambda = lambda;

        Container dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));

        dialog.add(materials);

        JPanel btnPanel = new JPanel();
        JButton btn;
        dialog.add(Box.createVerticalGlue());
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                try {
                    mat = (SimpleMaterial)materials.getSelectedItem();
                    mat.susc(lambda);
                    setVisible(false);
                }
                catch(UnsupportedWavelength e) {
                    JOptionPane.showMessageDialog(null, "Unsupported wavelength", "Error", JOptionPane.ERROR_MESSAGE);
                }
                catch(NullPointerException e) {
                    JOptionPane.showMessageDialog(null, "Material not selected", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btnPanel.add(btn);
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                mat = null;
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public SimpleMaterial call() {
        setVisible(true);
        return mat;
    }


    public static void main(String[] args) {
        try {
            LookupTable lookup = SFTables.defaultLookup();
            MatDB db = new MatDB(new File("matdb.xml"),lookup);
            MaterialDialog dlg = new MaterialDialog((Frame)null, null, db, 1.54056e-10);
            System.out.println(dlg.call());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
