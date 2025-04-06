package com.github.simonhauck.actintellijplugin.runconfiguration

import com.github.simonhauck.actintellijplugin.workflowparser.GitHubActParameters
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

interface GitHubActParametersEditable : GitHubActParameters {
    override var workFlowFile: String
}

class GitHubActRunConfigurationOptions : RunConfigurationOptions(), GitHubActParametersEditable {
    private val _workflowFile: StoredProperty<String?> =
        string("").provideDelegate(this, "workFlowFile")

    override var workFlowFile: String
        get() = _workflowFile.getValue(this) ?: ""
        set(value) = _workflowFile.setValue(this, value)
}
