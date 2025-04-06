package com.github.simonhauck.actintellijplugin.runconfiguration

import com.intellij.execution.configurations.*
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue

class GitHubActConfigurationType() :
    SimpleConfigurationType(
        id = "com.github.simonhauck.actintellijplugin.runworkflow",
        name = "GitHub Workflow Run",
        description = "Run GitHub workflows locally with 'act' from nektos",
        icon = NotNullLazyValue.createValue { AllIcons.FileTypes.Yaml },
    ) {

    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return GitHubActRunConfiguration(project, this, name = this.name)
    }

    override fun getOptionsClass(): Class<out BaseState> {
        return GitHubActRunConfigurationOptions::class.java
    }
}
