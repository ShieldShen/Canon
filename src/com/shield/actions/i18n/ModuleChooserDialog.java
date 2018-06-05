package com.shield.actions.i18n;

import com.intellij.openapi.module.Module;

import javax.swing.*;
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
    private JList<Map.Entry<String, Boolean>> list;
    private JCheckBox selectAllBox;
    private JComboBox<String> sourceSelector;
    private Module mDefaultModule;
    private SelectChangeListener mSelectAllChangeListener;
    private String mLastSource;

    public static ModuleChooserDialog create() {
        return new ModuleChooserDialog();
    }

    public ModuleChooserDialog setButtonOK(String text, ModuleSelectedOkListener actionListener) {
        buttonOK.setVisible(true);
        buttonOK.setText(text);
        buttonOK.addActionListener(e -> {
            if (actionListener != null) {
                actionListener.actionPerformed(e, moduleSelector == null ? null : (Module) moduleSelector.getSelectedItem());
            }
            dispose();
        });
        return this;
    }

    public ModuleChooserDialog setModule(Module defModule, List<Module> modules) {
        moduleSelector.removeAllItems();
        for (Module module : modules) {
            moduleSelector.addItem(module);
        }
        moduleSelector.setSelectedItem(defModule);
        mDefaultModule = defModule;
        return this;
    }

    public ModuleChooserDialog setSelectListener(ModuleItemSelectedListener listener) {
        moduleSelector.addActionListener(e -> {
            Module selectedItem = (Module) moduleSelector.getSelectedItem();
            if (selectedItem == null) {
                return;
            }
            if (mDefaultModule != null && mDefaultModule.getName().equals(selectedItem.getName())) {
                return;
            }
            if (listener != null) {
                listener.onItemSelected(selectedItem, ModuleChooserDialog.this);
            }
        });
        return this;
    }

    public ModuleChooserDialog setOnSelectAllChangeListener(SelectChangeListener l) {
        mSelectAllChangeListener = l;
        return this;
    }

    public ModuleChooserDialog setList(Map<String, Boolean> i18n, Boolean selectedAll) {
        Set<Map.Entry<String, Boolean>> entries = i18n.entrySet();
        DefaultListModel<Map.Entry<String, Boolean>> model = new DefaultListModel<>();
        selectAllBox.setSelected(selectedAll);
        list.setModel(model);
        list.setCellRenderer((list, entry, index, isSelected, cellHasFocus) -> {
            JCheckBox jCheckBox = new JCheckBox();
            jCheckBox.setText(entry.getKey());
            jCheckBox.setSelected(entry.getValue());
            return jCheckBox;
        });
        for (Map.Entry<String, Boolean> entry : entries) {
            model.addElement(entry);
        }

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            List<Map.Entry<String, Boolean>> selectedValuesList = list.getSelectedValuesList();
            for (Map.Entry<String, Boolean> entry : selectedValuesList) {
                Boolean lastValue = entry.getValue();
                entry.setValue(!lastValue);
            }
            if (selectAllBox.isSelected()) {
                selectAllBox.setSelected(false);
                selectAllBox.updateUI();
                if (mSelectAllChangeListener != null) mSelectAllChangeListener.onChange(false);
            }
            list.updateUI();
        });
        selectAllBox.addActionListener(e -> {
            boolean selected = selectAllBox.isSelected();
            for (Map.Entry<String, Boolean> entry : entries) {
                entry.setValue(selected);
            }
            if (mSelectAllChangeListener != null) mSelectAllChangeListener.onChange(selected);
            list.updateUI();
            selectAllBox.updateUI();
        });

        for (Map.Entry<String, Boolean> entry : entries) {
            sourceSelector.addItem(entry.getKey());
        }
        sourceSelector.setSelectedIndex(0);
        sourceSelector.addActionListener(e -> {
            String curSource = (String) sourceSelector.getSelectedItem();
            if (mLastSource != null && mLastSource.equals(curSource)) {
                return;
            }
            mLastSource = curSource;
            if (mSelectAllChangeListener != null) mSelectAllChangeListener.onSourceSet(mLastSource);
        });

        if (mSelectAllChangeListener != null) mSelectAllChangeListener.onSourceSet(sourceSelector.getItemAt(0));
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

    public void refreshList() {
        list.updateUI();
    }

    public static void main(String[] args) {
        ModuleChooserDialog dialog = new ModuleChooserDialog();
        dialog.setButtonOK("HHHH", new ModuleSelectedOkListener() {
            @Override
            public void actionPerformed(ActionEvent e, Module module) {
                System.out.print("sssss");
            }
        });

        Map<String, Boolean> i18n = new HashMap<>();
        i18n.put("values-en", true);
        i18n.put("values-cn", true);
        i18n.put("values-it", true);

        dialog.setList(i18n, true);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
