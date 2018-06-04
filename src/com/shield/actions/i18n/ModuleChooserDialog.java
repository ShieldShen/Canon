package com.shield.actions.i18n;

import com.intellij.openapi.module.Module;

import javax.swing.*;
import javax.swing.plaf.ButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModuleChooserDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JComboBox<Module> moduleSelector;
    private JList<Map.Entry<String,Boolean>> list;
    private JCheckBox selectAll;

    public static ModuleChooserDialog create() {
        return new ModuleChooserDialog();
    }

    public ModuleChooserDialog setButtonOK(String text, ModuleSelectedOkListener actionListener) {
        buttonOK.setVisible(true);
        buttonOK.setText(text);
        actionListener.setComboBox(moduleSelector);
        buttonOK.addActionListener(actionListener);
        return this;
    }

    public ModuleChooserDialog setModule(Module defModule, List<Module> modules) {
        for (Module module : modules) {
            moduleSelector.addItem(module);
        }
        moduleSelector.setSelectedItem(defModule);
        return this;
    }

    public ModuleChooserDialog setList(Map<String, Boolean> i18n) {
        Set<Map.Entry<String, Boolean>> entries = i18n.entrySet();
        DefaultListModel<Map.Entry<String, Boolean>> model = new DefaultListModel<>();
        list.setModel(model);
        list.setCellRenderer(new ListCellRenderer<Map.Entry<String, Boolean>>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends Map.Entry<String, Boolean>> list, Map.Entry<String, Boolean> entry, int index, boolean isSelected, boolean cellHasFocus) {
                JCheckBox jCheckBox = new JCheckBox();
                jCheckBox.setText(entry.getKey());
                jCheckBox.setSelected(entry.getValue());
                return jCheckBox;
            }
        });
        for (Map.Entry<String, Boolean> entry : entries) {
            model.addElement(entry);
        }

        return this;
    }

    private ModuleChooserDialog() {
        setContentPane(contentPane);
        setTitle("选择需要同步的Module");
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int Swing1x = getWidth();
        int Swing1y = getHeight();
        setBounds((screenSize.width - Swing1x) / 2, (screenSize.height - Swing1y) / 2 - 100, Swing1x, Swing1y);
        buttonOK.setVisible(false);

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void main(String[] args) {
        ModuleChooserDialog dialog = new ModuleChooserDialog();
        dialog.setButtonOK("HHHH", new ModuleSelectedOkListener() {
            @Override
            public void actionPerformed(ActionEvent e, Module module) {
                System.out.print("sssss");
            }
        });

        Map<String,Boolean> i18n = new HashMap<>();
        i18n.put("values-en", true);
        i18n.put("values-cn", true);
        i18n.put("values-it", true);

        dialog.setList(i18n);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
