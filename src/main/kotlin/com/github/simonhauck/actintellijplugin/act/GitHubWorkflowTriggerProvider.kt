package com.github.simonhauck.actintellijplugin.act

class GitHubWorkflowTriggerProvider {

    fun getSupportedTriggers(): List<String> {
        return listOf("push", "pull_request")
    }
}