package com.shield.actions.i18n;

import com.intellij.openapi.module.Module;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class ModuleSelectedOkListener implements ActionListener {

    private JComboBox<Module> mComboBox;

    void setComboBox(JComboBox<Module> comboBox) {
        mComboBox = comboBox;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed(e, mComboBox == null ? null : (Module) mComboBox.getSelectedItem());
    }

    abstract void actionPerformed(ActionEvent event, Module module);
}