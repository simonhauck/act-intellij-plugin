package com.github.simonhauck.actintellijplugin.runconfiguration

import com.github.simonhauck.actintellijplugin.workflowparser.GitHubActParameters
import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessHandlerFactory
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment

class GitHubActRunCommandLineState(
    private val parameters: GitHubActParameters,
    environment: ExecutionEnvironment,
) : CommandLineState(environment) {

    override fun startProcess(): ProcessHandler {
        val commandLine =
            if (System.getProperty("os.name").startsWith("Windows")) {
                GeneralCommandLine("cmd.exe", "/c", "act", "-W", parameters.workFlowFile)
            } else {
                GeneralCommandLine(
                    System.getenv("SHELL"),
                    "-c",
                    "act",
                    "-W",
                    parameters.workFlowFile,
                )
            }

        return ProcessHandlerFactory.getInstance().createColoredProcessHandler(commandLine).apply {
            ProcessTerminatedListener.attach(this)
        }
    }
}
