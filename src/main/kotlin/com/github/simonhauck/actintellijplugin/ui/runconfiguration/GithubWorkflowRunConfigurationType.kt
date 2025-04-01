package com.github.simonhauck.actintellijplugin.ui.runconfiguration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.StoredProperty
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class GithubWorkflowRunConfigurationType : ConfigurationTypeBase(
    "simonhauck.act-intellij-plugin.run-workflow",
    "GitHub Workflow Run Configuration",
    "Run configuration for GitHub workflow files",
    AllIcons.FileTypes.Yaml,
) {

    init {
        addFactory(GithubWorkflowRunConfigurationFactory(this))
    }
}

class GithubWorkflowRunConfigurationFactory(type: ConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String {
        return "simonhauck.act-intellij-plugin.run-workflow"
    }

    override fun createTemplateConfiguration(project: Project) =
        DemoRunConfiguration(project, this, "GitHub Workflow Run Configuration")

    override fun getOptionsClass(): Class<out BaseState> {
        return DemoRunConfigurationOptions::class.java
    }
}

class DemoRunConfigurationOptions : RunConfigurationOptions() {

    private val _myScriptName: StoredProperty<String?> = string("").provideDelegate(this, "scriptName")

    public var myScriptName: String
        get() = _myScriptName.getValue(this) ?: ""
        set(value) = _myScriptName.setValue(this, value)
}

class DemoRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    RunConfigurationBase<DemoRunConfigurationOptions>(project, factory, name) {

    private val settings = DemoSettingsEditor()

    override fun getOptions(): DemoRunConfigurationOptions {
        return super.getOptions() as DemoRunConfigurationOptions
    }

    fun getScriptName(): String {
        return options.myScriptName
    }

    fun setScriptName(scriptName: String) {
        options.myScriptName = scriptName
    }

    override fun getState(
        executor: Executor,
        executionEnvironment: ExecutionEnvironment,
    ): RunProfileState {
        return MyCommandLineSate(executionEnvironment, options)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return settings
    }
}

class MyCommandLineSate(
    environment: ExecutionEnvironment,
    private val options: DemoRunConfigurationOptions,
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine: GeneralCommandLine = GeneralCommandLine("cmd.exe", "/c", "echo", options.myScriptName)
        val processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine)
        ProcessTerminatedListener.attach(processHandler)
        return processHandler
    }
}

class DemoSettingsEditor : SettingsEditor<DemoRunConfiguration>() {

    private val myPanel: JPanel
    private val scriptPathField = TextFieldWithBrowseButton()

    init {
        scriptPathField.addBrowseFolderListener(
            "Select Script File",
            null,
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor(),
        )
        myPanel = FormBuilder.createFormBuilder().addLabeledComponent("Script file", scriptPathField).panel
    }

    override fun resetEditorFrom(demoRunConfiguration: DemoRunConfiguration) {
        scriptPathField.text = demoRunConfiguration.getScriptName()
    }

    override fun applyEditorTo(demoRunConfiguration: DemoRunConfiguration) {
        demoRunConfiguration.setScriptName(scriptPathField.text)
    }

    override fun createEditor(): JComponent {
        return myPanel
    }
}
