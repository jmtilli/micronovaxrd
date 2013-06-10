package fi.micronova.tkk.xray.dialogs;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





public class GenericAboutDialog extends JDialog {

    private JTextArea t;

    public GenericAboutDialog(Frame f, String title, String text)
    {
        super(f,title,true);
        init(text);
    }
    public GenericAboutDialog(Dialog d, String title, String text)
    {
        super(d,title,true);
        init(text);
    }

    private void init(String text) {
        Container dialog;

        dialog = getContentPane();

        GridBagConstraints c = new GridBagConstraints();
        dialog.setLayout(new GridBagLayout());
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1;
        c.ipadx = c.ipady = 2;
        c.insets = new Insets(2, 2, 2, 2);

        t = new JTextArea();
        t.setEditable(false);
        t.setOpaque(false);
        t.setBorder(null);
        t.setText(text);
        t.setFont(new Font("monospaced", Font.PLAIN, 12));
        /*
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        */
        t.setRows(14);
        t.setMinimumSize(t.getPreferredSize());
        dialog.add(new JScrollPane(t),c);
        t.setCaretPosition(0);


        /*
        JPanel p = new JPanel();
        p.setLayout(new FlowLayout(FlowLayout.CENTER));
        p.setMaximumSize(new Dimension(Short.MAX_VALUE,p.getPreferredSize().height));
        dialog.add(p,c);
        */

        JPanel btnPanel = new JPanel();
        JButton btn;
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        btn = new JButton("OK");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ev) {
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        dialog.setPreferredSize(dialog.getLayout().preferredLayoutSize(dialog));
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        btnPanel.setMinimumSize(btnPanel.getPreferredSize());

        c.weighty = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        dialog.add(btn,c);
        pack();
    }

    public void call() {
        setVisible(true);
    }
}
