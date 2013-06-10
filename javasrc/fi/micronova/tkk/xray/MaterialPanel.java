package fi.micronova.tkk.xray;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import fi.micronova.tkk.xray.xrdmodel.*;
import fi.micronova.tkk.xray.util.*;

public class MaterialPanel extends JPanel {
    private Material mat;

    public MaterialDialog getMaterialDialog(SimpleMaterial smat, MatDB db, final double lambda) {
        Dialog dialog;
        dialog = DialogUtil.getOwnerDialog(this);
        if(dialog != null) {
            return new MaterialDialog(dialog, smat, db, lambda);
        } else {
            Frame frame = DialogUtil.getOwnerFrame(this);
            return new MaterialDialog(frame, smat, db, lambda);
        }
    }
    public MixtureDialog getMixtureDialog(List<Mixture.Constituent> constituents, MatDB db, final double lambda) {
        Dialog dialog;
        dialog = DialogUtil.getOwnerDialog(this);
        if(dialog != null) {
            return new MixtureDialog(dialog, constituents, lambda, db);
        } else {
            Frame frame = DialogUtil.getOwnerFrame(this);
            return new MixtureDialog(frame, constituents, lambda, db);
        }
    }

    public MaterialPanel(Material newmat, final double lambda, final MatDB db) {
        super();

        JButton simpleButton = new JButton("Set simple material...");
        JButton mixButton = new JButton("Set mixture...");
        mat = newmat;
        final JTextField matLabel = new JTextField(mat.toString());
        matLabel.setEditable(false);
        matLabel.setColumns(20);

        setLayout(new FlowLayout(FlowLayout.CENTER));
        add(matLabel);
        simpleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                SimpleMaterial smat = null;
                if(mat instanceof SimpleMaterial) {
                    smat = (SimpleMaterial)mat;
                }
                MaterialDialog dialog = getMaterialDialog(smat, db, lambda);
                smat = dialog.call();
                dialog.dispose();
                if(smat != null)
                    mat = smat;
                matLabel.setText(mat.toString());
            }
        });
        add(simpleButton);
        mixButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                List<Mixture.Constituent> mixMaterials = null;
                Mixture mix;
                if(mat instanceof Mixture) {
                    mixMaterials = ((Mixture)mat).materials;
                } else {
                    mixMaterials = new ArrayList<Mixture.Constituent>();
                }
                MixtureDialog dialog = getMixtureDialog(mixMaterials, db, lambda);
                mix = dialog.call();
                dialog.dispose();

                if(mix != null)
                    mat = mix;
                matLabel.setText(mat.toString());
            }
        });
        add(mixButton);
    }
    public Material getMaterial() {
        return mat;
    }
}
