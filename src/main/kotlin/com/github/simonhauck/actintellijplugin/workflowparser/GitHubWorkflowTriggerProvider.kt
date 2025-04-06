package com.github.simonhauck.actintellijplugin.workflowparser

class GitHubWorkflowTriggerProvider {

    fun getSupportedTriggers(): List<String> {
        return listOf("push", "pull_request")
    }
}
