package com.github.simonhauck.actintellijplugin.ui.guttericon

import com.github.simonhauck.actintellijplugin.act.GitHubWorkflowTriggerProvider
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.yaml.psi.YAMLKeyValue

//class TestRun : RunLineMarkerContributor {
//
//    override fun getInfo(p0: PsiElement): Info? {
//        TODO("Not yet implemented")
//    }
//
//}

class GithubWorkflowGutterIconProvider : LineMarkerProvider {

    private val gitHubWorkflowTriggerProvider = GitHubWorkflowTriggerProvider()

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val file: PsiFile = element.containingFile

        if (element !is LeafPsiElement) return null
        if (!element.elementType.debugName.contains("scalar key")) return null
        if (!file.isWorkflowFile()) return null

        if (!element.isWorkflowTrigger() || !element.isSupportedTrigger()) return null

        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Execute,
            { "Run GitHub Workflow" },
            GutterIconNavigationHandler { _, _ ->
                // Define the action to be performed when the icon is clicked
                println("Action")
            },
            GutterIconRenderer.Alignment.CENTER,
            { "Accessible name provider" },
        )
    }

    private fun PsiFile.isWorkflowFile() = parent?.virtualFile?.path?.endsWith(".github/workflows") ?: false

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
