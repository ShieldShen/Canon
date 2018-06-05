package com.shield.actions.i18n;

import com.intellij.openapi.module.Module;

public interface ModuleItemSelectedListener {
    void onItemSelected(Module module, ModuleChooserDialog dialog);
}
