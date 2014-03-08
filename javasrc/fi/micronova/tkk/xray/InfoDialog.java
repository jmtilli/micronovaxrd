package fi.micronova.tkk.xray;
import fi.micronova.tkk.xray.xrdmodel.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





/* A dialog for layer settings */
public class InfoDialog extends JDialog {
    private JTextField wlField;
    private JTextField suscRealField0, suscImagField0;
    private JTextField suscRealFieldH, suscImagFieldH;
    private JTextField suscRealFieldHNeg, suscImagFieldHNeg;
    private JTextField susc0RealField0, susc0ImagField0;
    private JTextField susc0RealFieldH, susc0ImagFieldH;
    private JTextField susc0RealFieldHNeg, susc0ImagFieldHNeg;

    private JTextField xyspaceField, xyspace0Field, zspaceField, zspace0Field,
                       poissonField, millerField, braggField;

    public InfoDialog(JFrame f)
    {
        super(f,"Optical properties",true);
        Container dialog;

        dialog = getContentPane();

        dialog.setLayout(new BoxLayout(dialog,BoxLayout.PAGE_AXIS));
        JPanel gridPanel = new JPanel();
        GridBagConstraints c = new GridBagConstraints();
        gridPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(3,3,3,3);
        c.ipadx = c.ipady = 1;



        c.gridwidth = 1;
        gridPanel.add(new JLabel("wavelength (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        wlField = new JTextField("",12);
        wlField.setEditable(false);
        wlField.setMinimumSize(wlField.getPreferredSize());
        gridPanel.add(wlField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (0 0 0) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscRealField0 = new JTextField("",12);
        suscRealField0.setEditable(false);
        suscRealField0.setMinimumSize(suscRealField0.getPreferredSize());
        gridPanel.add(suscRealField0,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (0 0 0) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscImagField0 = new JTextField("",12);
        suscImagField0.setEditable(false);
        suscImagField0.setMinimumSize(suscImagField0.getPreferredSize());
        gridPanel.add(suscImagField0,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (h k l) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscRealFieldH = new JTextField("",12);
        suscRealFieldH.setEditable(false);
        suscRealFieldH.setMinimumSize(suscRealFieldH.getPreferredSize());
        gridPanel.add(suscRealFieldH,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (h k l) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscImagFieldH = new JTextField("",12);
        suscImagFieldH.setEditable(false);
        suscImagFieldH.setMinimumSize(suscImagFieldH.getPreferredSize());
        gridPanel.add(suscImagFieldH,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (-h -k -l) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscRealFieldHNeg = new JTextField("",12);
        suscRealFieldHNeg.setEditable(false);
        suscRealFieldHNeg.setMinimumSize(suscRealFieldHNeg.getPreferredSize());
        gridPanel.add(suscRealFieldHNeg,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("susceptibility (-h -k -l) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        suscImagFieldHNeg = new JTextField("",12);
        suscImagFieldHNeg.setEditable(false);
        suscImagFieldHNeg.setMinimumSize(suscImagFieldHNeg.getPreferredSize());
        gridPanel.add(suscImagFieldHNeg,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);


        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (0 0 0) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0RealField0 = new JTextField("",12);
        susc0RealField0.setEditable(false);
        susc0RealField0.setMinimumSize(susc0RealField0.getPreferredSize());
        gridPanel.add(susc0RealField0,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (0 0 0) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0ImagField0 = new JTextField("",12);
        susc0ImagField0.setEditable(false);
        susc0ImagField0.setMinimumSize(susc0ImagField0.getPreferredSize());
        gridPanel.add(susc0ImagField0,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (h k l) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0RealFieldH = new JTextField("",12);
        susc0RealFieldH.setEditable(false);
        susc0RealFieldH.setMinimumSize(susc0RealFieldH.getPreferredSize());
        gridPanel.add(susc0RealFieldH,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (h k l) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0ImagFieldH = new JTextField("",12);
        susc0ImagFieldH.setEditable(false);
        susc0ImagFieldH.setMinimumSize(susc0ImagFieldH.getPreferredSize());
        gridPanel.add(susc0ImagFieldH,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (-h -k -l) real"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0RealFieldHNeg = new JTextField("",12);
        susc0RealFieldHNeg.setEditable(false);
        susc0RealFieldHNeg.setMinimumSize(susc0RealFieldHNeg.getPreferredSize());
        gridPanel.add(susc0RealFieldHNeg,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed susceptibility (-h -k -l) imag"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        susc0ImagFieldHNeg = new JTextField("",12);
        susc0ImagFieldHNeg.setEditable(false);
        susc0ImagFieldHNeg.setMinimumSize(susc0ImagFieldHNeg.getPreferredSize());
        gridPanel.add(susc0ImagFieldHNeg,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("xyspace (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        xyspaceField = new JTextField("",12);
        xyspaceField.setEditable(false);
        xyspaceField.setMinimumSize(xyspaceField.getPreferredSize());
        gridPanel.add(xyspaceField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed xyspace (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        xyspace0Field = new JTextField("",12);
        xyspace0Field.setEditable(false);
        xyspace0Field.setMinimumSize(xyspace0Field.getPreferredSize());
        gridPanel.add(xyspace0Field,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("zspace (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        zspaceField = new JTextField("",12);
        zspaceField.setEditable(false);
        zspaceField.setMinimumSize(zspaceField.getPreferredSize());
        gridPanel.add(zspaceField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("relaxed zspace (m)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        zspace0Field = new JTextField("",12);
        zspace0Field.setEditable(false);
        zspace0Field.setMinimumSize(zspace0Field.getPreferredSize());
        gridPanel.add(zspace0Field,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("Poisson's ratio"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        poissonField = new JTextField("",12);
        poissonField.setEditable(false);
        poissonField.setMinimumSize(poissonField.getPreferredSize());
        gridPanel.add(poissonField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("reflection"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        millerField = new JTextField("",12);
        millerField.setEditable(false);
        millerField.setMinimumSize(millerField.getPreferredSize());
        gridPanel.add(millerField,c);
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridPanel.add(new JPanel(),c);

        c.gridwidth = 1;
        gridPanel.add(new JLabel("Bragg's angle (degrees)"),c);
        //c.gridwidth = GridBagConstraints.REMAINDER;
        braggField = new JTextField("",12);
        braggField.setEditable(false);
        braggField.setMinimumSize(braggField.getPreferredSize());
        gridPanel.add(braggField,c);
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
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        dialog.add(btnPanel);
        pack();
    }
    public void call(LayerStack s, int i) {
        double lambda = s.getLambda();
        Layer l = s.getElementAt(i);
        double xyspace = s.getElementAt(s.getSize()-1).   calcXYSpace(0);
        for (int j=s.getSize()-2; j>=i; j--)
        {
            xyspace = s.getElementAt(j).calcXYSpace(xyspace);
        }
        SimpleMaterial sm0 = l.getSimpleMaterial(0);
        SimpleMaterial sm = sm0.getStrainedMaterial(xyspace);
        try {
            Susceptibilities susc = sm.susc(s.getLambda());
            Susceptibilities susc0 = sm0.susc(s.getLambda());
            wlField.setText(String.format(Locale.US,"%.6g",lambda));
            suscRealField0.setText(String.format(Locale.US,"%.6g", susc.chi_0.getReal()));
            suscImagField0.setText(String.format(Locale.US,"%.6g", susc.chi_0.getImag()));
            suscRealFieldH.setText(String.format(Locale.US,"%.6g", susc.chi_h.getReal()));
            suscImagFieldH.setText(String.format(Locale.US,"%.6g", susc.chi_h.getImag()));
            suscRealFieldHNeg.setText(String.format(Locale.US,"%.6g", susc.chi_h_neg.getReal()));
            suscImagFieldHNeg.setText(String.format(Locale.US,"%.6g", susc.chi_h_neg.getImag()));
            susc0RealField0.setText(String.format(Locale.US,"%.6g", susc0.chi_0.getReal()));
            susc0ImagField0.setText(String.format(Locale.US,"%.6g", susc0.chi_0.getImag()));
            susc0RealFieldH.setText(String.format(Locale.US,"%.6g", susc0.chi_h.getReal()));
            susc0ImagFieldH.setText(String.format(Locale.US,"%.6g", susc0.chi_h.getImag()));
            susc0RealFieldHNeg.setText(String.format(Locale.US,"%.6g", susc0.chi_h_neg.getReal()));
            susc0ImagFieldHNeg.setText(String.format(Locale.US,"%.6g", susc0.chi_h_neg.getImag()));
            xyspaceField.setText(String.format(Locale.US,"%.6g", sm.getXYSpace()));
            xyspace0Field.setText(String.format(Locale.US,"%.6g", sm0.getXYSpace()));
            zspaceField.setText(String.format(Locale.US,"%.6g", sm.getZSpace()));
            zspace0Field.setText(String.format(Locale.US,"%.6g", sm0.getZSpace()));
            poissonField.setText(String.format(Locale.US,"%.6g", sm.getPoisson()));
            millerField.setText(sm.getReflection().toString());
            braggField.setText(String.format(Locale.US,"%.6g",
                                   180/Math.PI
                                 * Math.asin(  s.getLambda()
                                             / (2*sm.getZSpace()))));
        }
        catch(UnsupportedWavelength ex)
        {
            JOptionPane.showMessageDialog(null, ex.getMessage(),    "Error", JOptionPane.ERROR_MESSAGE);
        }
        setVisible(true);
    }
}
