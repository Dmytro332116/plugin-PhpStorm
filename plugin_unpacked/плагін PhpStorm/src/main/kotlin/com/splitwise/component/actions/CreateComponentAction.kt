package com.splitwise.component.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.splitwise.component.ui.CreateComponentDialog

class CreateComponentAction : AnAction("Create Component") {
    private val logger = Logger.getInstance(CreateComponentAction::class.java)

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val dialog = CreateComponentDialog(project)
        if (!dialog.showAndGet()) {
            return
        }

        val componentName = dialog.componentName.trim()
        val isBlockComponent = dialog.isBlockComponent
        logger.info("Create Component dialog OK: name='$componentName', isBlock=$isBlockComponent")
    }
}
