package com.splitwise.component.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import com.splitwise.component.generator.ComponentGenerator
import com.splitwise.component.settings.ComponentGeneratorSettings
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class ComponentToolWindowPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val settings = ComponentGeneratorSettings.getInstance(project)
    private val componentNameField = JBTextField()
    private val themeNameField = JBTextField()
    private val blockCheckBox = JBCheckBox("Block component (components/blocks)")
    private var updatingThemeField = false

    init {
        componentNameField.emptyText.text = "Component name"
        themeNameField.emptyText.text = "Theme name (e.g. personal)"
        themeNameField.text = settings.themeName

        themeNameField.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent) = persistThemeIfValid()
            override fun removeUpdate(e: DocumentEvent) = persistThemeIfValid()
            override fun changedUpdate(e: DocumentEvent) = persistThemeIfValid()
        })

        add(buildContent(), BorderLayout.CENTER)
    }

    private fun buildContent(): JComponent {
        val root = JPanel()
        root.layout = BoxLayout(root, BoxLayout.Y_AXIS)
        root.border = JBUI.Borders.empty(8)

        val createPanel = buildCreatePanel()
        val settingsPanel = buildSettingsPanel()

        root.add(CollapsibleSection("Create Component", createPanel, true))
        root.add(Box.createVerticalStrut(8))
        root.add(CollapsibleSection("Settings", settingsPanel, true))

        return root
    }

    private fun buildCreatePanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(4, 8)

        val nameRow = JPanel(BorderLayout(8, 0))
        nameRow.add(JBLabel("Component name:"), BorderLayout.WEST)
        nameRow.add(componentNameField, BorderLayout.CENTER)

        val buttonRow = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))
        val createButton = JButton("Create")
        createButton.addActionListener { onCreate() }
        buttonRow.add(createButton)

        panel.add(nameRow)
        panel.add(Box.createVerticalStrut(6))
        panel.add(blockCheckBox)
        panel.add(Box.createVerticalStrut(8))
        panel.add(buttonRow)

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

    private fun onCreate() {
        val componentName = ComponentGeneratorSettings.normalizeComponentName(componentNameField.text)
        if (componentName.isEmpty()) {
            Messages.showErrorDialog(project, "Component name is required.", "Create Component")
            return
        }

        val themeName = ComponentGeneratorSettings.normalizeThemeName(themeNameField.text)
        if (themeName.isEmpty()) {
            Messages.showErrorDialog(project, "Theme name is required.", "Create Component")
            return
        }

        if (componentNameField.text.trim() != componentName) {
            componentNameField.text = componentName
        }
        if (themeNameField.text.trim() != themeName) {
            themeNameField.text = themeName
        }
        settings.themeName = themeName

        val result = ComponentGenerator.generate(project, componentName, blockCheckBox.isSelected, themeName)
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
