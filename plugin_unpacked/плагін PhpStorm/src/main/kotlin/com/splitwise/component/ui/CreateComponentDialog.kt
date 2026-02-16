package com.splitwise.component.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.splitwise.component.settings.ComponentGeneratorSettings
import java.awt.BorderLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CreateComponentDialog(project: Project) : DialogWrapper(project) {
    private val settings = ComponentGeneratorSettings.getInstance(project)
    private val componentNameField = JBTextField()
    private val themeNameField = JBTextField()
    private val blockCheckBox = JBCheckBox("Block component (components/blocks)")
    private var updatingThemeField = false
    private var normalizedComponent = ""
    private var normalizedTheme = settings.themeName

    val componentName: String
        get() = normalizedComponent

    val isBlockComponent: Boolean
        get() = blockCheckBox.isSelected

    val themeName: String
        get() = normalizedTheme

    init {
        title = "Component Generator"
        setOKButtonText("Create")
        init()
        componentNameField.emptyText.text = "Component name"
        themeNameField.emptyText.text = "Theme name (e.g. personal)"
        themeNameField.text = settings.themeName

        componentNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = updateOkAction()
            override fun removeUpdate(e: DocumentEvent) = updateOkAction()
            override fun changedUpdate(e: DocumentEvent) = updateOkAction()
        })

        themeNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = persistThemeIfValid()
            override fun removeUpdate(e: DocumentEvent) = persistThemeIfValid()
            override fun changedUpdate(e: DocumentEvent) = persistThemeIfValid()
        })
        updateOkAction()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel()
        root.layout = BoxLayout(root, BoxLayout.Y_AXIS)
        root.border = JBUI.Borders.empty(8)

        root.add(CollapsibleSection("Create Component", buildCreatePanel(), true))
        root.add(Box.createVerticalStrut(8))
        root.add(CollapsibleSection("Settings", buildSettingsPanel(), true))

        return root
    }

    override fun doOKAction() {
        val componentName = ComponentGeneratorSettings.normalizeComponentName(componentNameField.text)
        if (componentName.isEmpty()) {
            Messages.showErrorDialog(componentNameField, "Component name is required.", "Create Component")
            return
        }

        val themeName = ComponentGeneratorSettings.normalizeThemeName(themeNameField.text)
        if (themeName.isEmpty()) {
            Messages.showErrorDialog(themeNameField, "Theme name is required.", "Create Component")
            return
        }

        if (componentNameField.text.trim() != componentName) {
            componentNameField.text = componentName
        }
        if (themeNameField.text.trim() != themeName) {
            themeNameField.text = themeName
        }

        settings.themeName = themeName
        normalizedComponent = componentName
        normalizedTheme = themeName
        super.doOKAction()
    }

    private fun buildCreatePanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(4, 8)

        val nameRow = JPanel(BorderLayout(8, 0))
        nameRow.add(JBLabel("Component name:"), BorderLayout.WEST)
        nameRow.add(componentNameField, BorderLayout.CENTER)

        panel.add(nameRow)
        panel.add(Box.createVerticalStrut(6))
        panel.add(blockCheckBox)

        return panel
    }

    private fun buildSettingsPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(4, 8)

        val themeRow = JPanel(BorderLayout(8, 0))
        themeRow.add(JBLabel("Theme name:"), BorderLayout.WEST)
        themeRow.add(themeNameField, BorderLayout.CENTER)

        val hint = JBLabel("Used for {theme}.libraries.yml and Drupal.behaviors.{theme}_*")
        hint.font = JBUI.Fonts.smallFont()

        panel.add(themeRow)
        panel.add(Box.createVerticalStrut(4))
        panel.add(hint)

        return panel
    }

    private fun updateOkAction() {
        isOKActionEnabled = componentNameField.text.trim().isNotEmpty()
    }

    private fun persistThemeIfValid() {
        if (updatingThemeField) {
            return
        }
        val normalized = ComponentGeneratorSettings.normalizeThemeName(themeNameField.text)
        if (normalized.isEmpty()) {
            return
        }
        if (themeNameField.text.trim() != normalized) {
            updatingThemeField = true
            themeNameField.text = normalized
            updatingThemeField = false
        }
        settings.themeName = normalized
    }

    private class CollapsibleSection(
        title: String,
        private val content: JComponent,
        expandedByDefault: Boolean
    ) : JPanel(BorderLayout()) {
        private val sectionTitle = title
        private var expanded = expandedByDefault
        private val toggleButton = JButton()

        init {
            border = JBUI.Borders.empty(2, 0)
            toggleButton.horizontalAlignment = SwingConstants.LEFT
            toggleButton.isContentAreaFilled = false
            toggleButton.isBorderPainted = false
            toggleButton.margin = JBUI.emptyInsets()
            toggleButton.addActionListener { toggle() }
            updateHeader()

            add(toggleButton, BorderLayout.NORTH)
            add(content, BorderLayout.CENTER)
            content.isVisible = expanded
        }

        private fun toggle() {
            expanded = !expanded
            content.isVisible = expanded
            updateHeader()
            revalidate()
            repaint()
        }

        private fun updateHeader() {
            toggleButton.text = if (expanded) {
                "[-] $sectionTitle"
            } else {
                "[+] $sectionTitle"
            }
        }
    }
}
