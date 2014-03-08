package fi.micronova.tkk.xray.dialogs;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;





public class ListDialog extends JDialog {
    /* throws IllegalArgumentException */

    public ListDialog(Dialog d, String title, Collection<?> data)
    {
        super(d,title,true);
        init(data);
    }
    public ListDialog(Frame f, String title, Collection<?> data)
    {
        super(f,title,true);
        init(data);
    }
    private void init(Collection<?> data) {
        Container dialog;
        JList<Object> list;
        JScrollPane scrollPane;
        Dimension d;

        dialog = getContentPane();

        dialog.setLayout(new BorderLayout());

        list = new JList<Object>(new Vector<Object>(data));
        scrollPane = new JScrollPane(list);

        d = scrollPane.getPreferredSize();
        scrollPane.setPreferredSize(new Dimension((d.width>400) ? 400 : (d.width+50), (d.height>300) ? 300 : (d.height+50)));

        dialog.add(scrollPane, BorderLayout.CENTER);

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
        /*
        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        btnPanel.add(btn);
        btnPanel.setMaximumSize(new Dimension(Short.MAX_VALUE,btnPanel.getPreferredSize().height));
        */
        dialog.add(btnPanel, BorderLayout.SOUTH);
        pack();
    }
    public void call() {
        setVisible(true);
    }
}
