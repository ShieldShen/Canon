package com.shield.actions.i18n

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.w3c.dom.Node
import java.awt.event.ActionEvent
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class AndroidI18nSyncStrings : AnAction() {

    private val userConfigFileName: String = "userConfig.xml"

    private val userConfigKey: String = "UserConfig"
    private val defaultModuleKey: String = "DefaultModule"
    private val selectedAllI18n: String = "SelectedAllI18n"
    private val resOptions: String = "resOptions"
    private val resOption: String = "resOption"
    private val moduleName: String = "moduleName"
    private val resItem: String = "resItem"
    private val resName: String = "resName"

    override fun actionPerformed(event: AnActionEvent?) {
        val project: Project? = event?.project


        val modules = ModuleManager.getInstance(project!!).modules
        if (modules.isEmpty()) {
            Messages.showOkCancelDialog("没有发现可用的Module", "进程被终止", null, null, null)
            return
        }

        val userConfigData: UserConfig = getUserConfig(project)

        val defaultModule = getDefaultModule(project, event, modules, userConfigData)

        val i18n = getI18n(project, defaultModule, userConfigData)

        val listener: SelectChangeListener = object : SelectChangeListener {
            override fun onChange(value: Boolean) {
                userConfigData.selectedAllI18n = value
            }

            override fun onSourceSet(value: String?) {
                userConfigData.sourceRes = value
            }
        }

        val chooseDialog: ModuleChooserDialog = ModuleChooserDialog.create()
                .setButtonOK("确定", object : ModuleSelectedOkListener() {
                    override fun actionPerformed(e: ActionEvent?, module: Module?) {
                        saveUserConfig(userConfigData, project)
                        startSyncModule(module, userConfigData, project)
                    }
                }).setModule(defaultModule, modules.asList())
                .setList(i18n, userConfigData.selectedAllI18n)
                .setSelectListener({ module: Module, moduleChooserDialog: ModuleChooserDialog ->
                    val temp = getI18n(project, module, userConfigData)
                    moduleChooserDialog
                            .setModule(module, modules.asList())
                            .setList(temp, userConfigData.selectedAllI18n)
                            .refreshList()
                    userConfigData.defaultModule = module.name
                }).setOnSelectAllChangeListener(listener)
        chooseDialog.pack()
        chooseDialog.isVisible = true
    }

    private fun getDefaultModule(project: Project, event: AnActionEvent?, modules: Array<Module>, userConfigData: UserConfig): Module? {
        if (userConfigData.defaultModule != "") {
            modules.filter {
                userConfigData.defaultModule == it.name
            }.forEach { return it }
        }
        val curFile = event?.getData(CommonDataKeys.PSI_FILE)
        return if (curFile?.virtualFile != null)
            ModuleUtilCore.findModuleForFile(curFile.virtualFile!!, project)
        else
            modules[0]
    }

    private fun getUserConfig(project: Project): UserConfig {
        val userConfigData = UserConfig()
        val userConfigFile = project.baseDir.findChild(userConfigFileName)
        if (userConfigFile != null) {
            val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(File(userConfigFile.path))
            val root = document.documentElement
            val userConfigList = root.childNodes

            (0..userConfigList.length)
                    .map { userConfigList.item(it) }
                    .forEach {
                        when (it?.nodeName) {
                            defaultModuleKey -> userConfigData.defaultModule = it.textContent
                            selectedAllI18n -> userConfigData.selectedAllI18n = it.textContent.toBoolean()
                            resOptions -> {
                                readResOptions(it, userConfigData)
                            }
                        }
                    }
        }

        return userConfigData
    }

    private fun readResOptions(resOptions: Node, userConfigData: UserConfig) {
        val map = mutableMapOf<String, Boolean>()
        val resOptionList = resOptions.childNodes
        (0..resOptionList.length)
                .map { resOptionList.item(it) }
                .forEach {
                    when (it?.nodeName) {
                        resOption -> readSingleResOption(it, map, userConfigData)
                    }
                }

    }

    private fun readSingleResOption(resOption: Node, map: MutableMap<String, Boolean>, userConfigData: UserConfig) {
        val reses = resOption.childNodes
        (0..reses.length).map { reses.item(it) }
                .forEach {
                    when (it?.nodeName) {
                        resItem -> {
                            val name = it.attributes.getNamedItem(resName).nodeValue
                            val value = it.textContent.toBoolean()
                            map.put(name, value)
                        }
                    }
                }
        userConfigData.resOptions.put(resOption.attributes.getNamedItem(moduleName).nodeValue, map)
    }

    private fun getI18n(project: Project?, defaultModule: Module?, userConfigData: UserConfig): MutableMap<String, Boolean> {
        val baseDir = project?.baseDir
        val configFile = baseDir?.findChild(userConfigFileName)
        return if (configFile == null && defaultModule != null) {
            loadResOptionFromLocal(defaultModule, userConfigData)
        } else {
            loadResOptionFromConfig(defaultModule, userConfigData)
        }
    }

    private fun loadResOptionFromConfig(defaultModule: Module?, userConfigData: UserConfig): MutableMap<String, Boolean> {
        return userConfigData.resOptions[defaultModule?.name] ?: loadResOptionFromLocal(defaultModule, userConfigData)
    }

    private fun loadResOptionFromLocal(module: Module?, userConfigData: UserConfig): MutableMap<String, Boolean> {
        val i18n = mutableMapOf<String, Boolean>()
        if (module == null) return i18n
        val root = LocalFileSystem.getInstance().findFileByIoFile(File(module.moduleFilePath))

        val resDir = findRes(root?.parent)
        userConfigData.resOptions.put(module.name, i18n)
        resDir?.children?.filter {
            println("scan " + it.name)
            it != null && it.isDirectory && it.name.startsWith("value")
        }?.forEach {
            i18n.put(it.path, userConfigData.selectedAllI18n)
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

    private fun saveUserConfig(userConfigData: UserConfig, project: Project) {
        val file = File(project.basePath, userConfigFileName)
        if (file.exists()) {
            file.delete()
        }

        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val document = builder.newDocument()
        val root = document.createElement(userConfigKey)


        //DefaultModule
        val def = document.createElement(defaultModuleKey)
        def.textContent = userConfigData.defaultModule
        root.appendChild(def)

        //SelectAll
        val selectAll = document.createElement(selectedAllI18n)
        selectAll.textContent = userConfigData.selectedAllI18n.toString()
        root.appendChild(selectAll)

        //ResOptions
        val resOptionsTag = document.createElement(resOptions)

        userConfigData.resOptions.iterator()
                .forEach {
                    val resOptionTag = document.createElement(resOption)
                    resOptionTag.setAttribute(moduleName, it.key)

                    it.value.iterator().forEach {
                        val resItemTag = document.createElement(resItem)
                        resItemTag.setAttribute(resName, it.key)
                        resItemTag.textContent = it.value.toString()
                        resOptionTag.appendChild(resItemTag)
                    }
                    resOptionsTag.appendChild(resOptionTag)
                }

        root.appendChild(resOptionsTag)

        document.appendChild(root)

        val transformer = TransformerFactory.newInstance().newTransformer()
        transformer.transform(DOMSource(document), StreamResult(File(project.basePath, userConfigFileName)))
    }

    fun startSyncModule(module: Module?, userConfigData: UserConfig, project: Project) {
        SyncI18nManager().sync(module, userConfigData, project)
    }
}