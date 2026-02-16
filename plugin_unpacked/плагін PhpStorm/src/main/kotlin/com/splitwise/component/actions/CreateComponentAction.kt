package com.splitwise.component.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindowManager

class CreateComponentAction : AnAction("Component Generator") {
    private val logger = Logger.getInstance(CreateComponentAction::class.java)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Component Generator")
        if (toolWindow == null) {
            Messages.showErrorDialog(
                project,
                "Component Generator tool window is not available.",
                "Create Component"
            )
            return
        }

        logger.info("Opening Component Generator tool window")
        toolWindow.activate(null)
    }
}
