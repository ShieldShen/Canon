package com.shield.actions.i18n

import com.intellij.configurationStore.serialize
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.sun.xml.internal.fastinfoset.dom.DOMDocumentParser
import com.sun.xml.internal.fastinfoset.dom.DOMDocumentSerializer
import org.jetbrains.annotations.SystemIndependent
import java.awt.event.ActionEvent
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

class AndroidI18nSyncStrings : AnAction() {

    private val userConfigFileName: String = "userConfig.xml"

    private val defaultModuleKey: String = "DefaultModule"

    private val resConfigFileName: String = "i18nSynConfig.xml"

    override fun actionPerformed(event: AnActionEvent?) {
        val project: Project? = event?.project


        val modules = ModuleManager.getInstance(project!!).modules

        val defaultModule: Module? = getDefaultModule(event, project, modules)


        val i18n = getI18n(project, defaultModule)

        val dialog = ModuleChooserDialog.create()
                .setButtonOK("确定", object : ModuleSelectedOkListener() {
                    override fun actionPerformed(e: ActionEvent?, module: Module?) {
                        startSyncModule(module)
                    }
                }).setModule(defaultModule, modules.asList())
                .setList(i18n)
        dialog.pack()
        dialog.isVisible = true
    }

    private fun getDefaultModule(event: AnActionEvent?, project: Project, modules: Array<Module>): Module? {

        val userConfigFile = project.baseDir.findChild("userConfig")
        if (userConfigFile != null) {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(userConfigFile.path))
            val nodeList = document.getElementsByTagName(defaultModuleKey)

            for(i in 0..nodeList.length){
                val item = nodeList.item(i)
            }

        }
        val curFile: PsiFile? = event?.getData(CommonDataKeys.PSI_FILE)


        if (curFile == null) null
        else ModuleUtilCore.findModuleForPsiElement(curFile)

        return null
    }

    private fun getI18n(project: Project?, defaultModule: Module?): MutableMap<String, Boolean> {
        val i18n: MutableMap<String, Boolean>

        val baseDir = project?.baseDir
        val configFile = baseDir?.findChild(resConfigFileName)
        i18n = if (configFile == null && defaultModule != null) {
            loadConfigFromLocal(defaultModule)
        } else {
            mutableMapOf()
        }

        return i18n
    }

    private fun loadConfigFromLocal(module: Module): MutableMap<String, Boolean> {
        val i18n = mutableMapOf<String, Boolean>()
        val root = LocalFileSystem.getInstance().findFileByIoFile(File(module.moduleFilePath))

        val resDir = findRes(root?.parent)


        resDir?.children?.filter {
            println("scan " + it.name)
            it != null && it.isDirectory && it.name.startsWith("value")
        }?.forEach {
            i18n.put(it.name, true)
        }
        return i18n
    }

    private fun findRes(root: VirtualFile?): VirtualFile? {
        root?.children?.filter {
            it != null && it.isDirectory && !it.name.contains("build")
        }?.forEach {
            return if ("res" == it.name) {
                it
            } else {
                findRes(it)
            }
        }
        return null
    }

    fun startSyncModule(module: Module?) {
        println("jhhhhhhhh")
    }
}