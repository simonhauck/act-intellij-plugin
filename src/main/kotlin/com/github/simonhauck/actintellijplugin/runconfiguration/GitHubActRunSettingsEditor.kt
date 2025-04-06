package com.github.simonhauck.actintellijplugin.runconfiguration

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.FormBuilder
import java.io.File
import javax.swing.JComponent
import javax.swing.JPanel

class GitHubActRunSettingsEditor(basePath: String?) : SettingsEditor<GitHubActRunConfiguration>() {
    private var myPanel: JPanel
    private var workflowFile: TextFieldWithBrowseButton = TextFieldWithBrowseButton()

    init {
        val workflowDir =
            if (basePath != null) {
                val projectBaseDir: VirtualFile? = VfsUtil.findFileByIoFile(File(basePath), true)
                projectBaseDir?.findFileByRelativePath(".github/workflows")
            } else null

        workflowFile.addBrowseFolderListener(
            "GitHub Workflow",
            "Select the GitHub workflow file",
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withRoots(workflowDir)
                .withShowHiddenFiles(true)
                .withFileFilter { it.extension == "yml" || it.extension == "yaml" }
                .apply { isForcedToUseIdeaFileChooser = true },
        )
        myPanel =
            FormBuilder.createFormBuilder()
                .addLabeledComponent("GitHub workflow", workflowFile)
                .panel
    }

    override fun resetEditorFrom(configuration: GitHubActRunConfiguration) {
        workflowFile.text = configuration.workFlowFile
    }

    override fun applyEditorTo(configuration: GitHubActRunConfiguration) {
        configuration.workFlowFile = workflowFile.text
    }

    override fun createEditor(): JComponent = myPanel
}
