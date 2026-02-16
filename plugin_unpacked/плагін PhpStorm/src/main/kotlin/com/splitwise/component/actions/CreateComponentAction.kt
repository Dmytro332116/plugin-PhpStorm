package com.splitwise.component.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import com.splitwise.component.generator.ComponentGenerator
import com.splitwise.component.ui.CreateComponentDialog

class CreateComponentAction : AnAction("Component Generator") {
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

        val componentName = dialog.componentName
        val isBlockComponent = dialog.isBlockComponent
        val themeName = dialog.themeName
        logger.info(
            "Component Generator dialog OK: name='$componentName', isBlock=$isBlockComponent, theme='$themeName'"
        )

        val result = ComponentGenerator.generate(project, componentName, isBlockComponent, themeName)
        if (result.success) {
            Messages.showInfoMessage(project, "Component '$componentName' created.", "Create Component")
        } else {
            Messages.showErrorDialog(
                project,
                result.message ?: "Failed to create component.",
                "Create Component"
            )
        }
    }
}
