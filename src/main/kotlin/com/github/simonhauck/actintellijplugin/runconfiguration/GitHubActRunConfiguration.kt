package com.github.simonhauck.actintellijplugin.runconfiguration

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

class GitHubActRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    RunConfigurationBase<GitHubActRunConfigurationOptions>(project, factory, name),
    GitHubActParametersEditable {

    override fun getOptions(): GitHubActRunConfigurationOptions {
        return super.getOptions() as GitHubActRunConfigurationOptions
    }

    override fun getState(
        executor: Executor,
        executionEnvironment: ExecutionEnvironment,
    ): RunProfileState {
        return GitHubActRunCommandLineState(options, executionEnvironment)
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
        return GitHubActRunSettingsEditor(project.basePath)
    }

    override var workFlowFile: String
        get() = options.workFlowFile
        set(value) {
            options.workFlowFile = value
        }
}
