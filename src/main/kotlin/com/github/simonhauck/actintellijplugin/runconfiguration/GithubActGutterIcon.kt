package com.github.simonhauck.actintellijplugin.runconfiguration

import com.github.simonhauck.actintellijplugin.workflowparser.GitHubWorkflowTriggerProvider
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue

class GithubActGutterIcon : RunLineMarkerContributor() {

    private val gitHubWorkflowTriggerProvider = GitHubWorkflowTriggerProvider()

    override fun getInfo(element: PsiElement): Info? {
        println("Get info called")
        val file: PsiFile = element.containingFile
        val project: Project = file.project

        if (element !is LeafPsiElement) return null
        if (!element.elementType.debugName.contains("scalar key")) return null
        if (!file.isWorkflowFile()) return null

        if (!element.isWorkflowTrigger() || !element.isSupportedTrigger()) return null

        val action = { _: PsiElement ->
            val runManager = RunManager.getInstance(project)
            val configuration = getOrCreateRunConfiguration(runManager)

        }
        return Info(AllIcons.Actions.Execute, null)
    }

    private fun getOrCreateRunConfiguration(
        runManager: RunManager
    ): RunnerAndConfigurationSettings {
        val existingRunConfiguration =
            runManager.findConfigurationByTypeAndName(
                GitHubActConfigurationType().id,
                "Run GitHub Workflow",
            )

        if (existingRunConfiguration != null) {
            println("Existing run configuration found: ${existingRunConfiguration.name}")
            return existingRunConfiguration
        }

        return runManager.createConfiguration(
            "Run GitHub Workflow",
            GitHubActConfigurationType::class.java,
        )
    }

    private fun PsiFile.isWorkflowFile() =
        parent?.virtualFile?.path?.endsWith(".github/workflows") ?: false

    private fun PsiElement.isWorkflowTrigger(): Boolean {
        return isBelowOn(this)
    }

    private fun isBelowOn(startElement: PsiElement): Boolean {
        var currentSearch = startElement
        repeat(3) {
            val parent = currentSearch.parent
            if (parent is YAMLKeyValue && parent.key?.text == "on") return true

            currentSearch = parent
        }
        return false
    }

    private fun PsiElement.isSupportedTrigger(): Boolean {
        return gitHubWorkflowTriggerProvider.getSupportedTriggers().contains(text)
    }
}
