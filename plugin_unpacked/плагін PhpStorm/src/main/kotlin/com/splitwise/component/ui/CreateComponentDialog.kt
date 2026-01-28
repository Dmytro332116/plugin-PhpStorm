package com.splitwise.component.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CreateComponentDialog(project: Project) : DialogWrapper(project) {
    private val nameField = JBTextField()
    private val blockCheckBox = JBCheckBox("This component is a block")

    val componentName: String
        get() = nameField.text

    val isBlockComponent: Boolean
        get() = blockCheckBox.isSelected

    init {
        title = "Create Component"
        init()
        nameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateOkAction()
            override fun removeUpdate(e: DocumentEvent) = updateOkAction()
            override fun changedUpdate(e: DocumentEvent) = updateOkAction()
        })
        updateOkAction()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)

        val namePanel = JPanel(BorderLayout(8, 8))
        namePanel.add(JBLabel("Component name:"), BorderLayout.WEST)
        namePanel.add(nameField, BorderLayout.CENTER)

        panel.add(namePanel)
        panel.add(Box.createVerticalStrut(8))
        panel.add(blockCheckBox)

        return panel
    }

    private fun updateOkAction() {
        isOKActionEnabled = nameField.text.trim().isNotEmpty()
    }
}
