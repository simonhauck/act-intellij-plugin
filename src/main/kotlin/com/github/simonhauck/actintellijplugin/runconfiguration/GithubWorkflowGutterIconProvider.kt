package com.github.simonhauck.actintellijplugin.runconfiguration

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.LeafPsiElement

class GithubWorkflowGutterIconProvider : LineMarkerProvider {

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        val file: PsiFile = element.containingFile

        val isWorkflowFile = file.parent?.virtualFile?.path?.endsWith(".github/workflows") ?: false

        if (!isWorkflowFile) return null

        if (element !is LeafPsiElement) return null

        if (element.text.contains("push")) {
            println("Push element detected")
        }

        if (file.name.endsWith(".yml") || file.name.endsWith(".yaml")) {
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
        return null
    }
}
