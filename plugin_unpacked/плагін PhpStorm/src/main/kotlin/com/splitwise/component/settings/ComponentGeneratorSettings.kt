package com.splitwise.component.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "ComponentGeneratorSettings",
    storages = [Storage("component-generator.xml")]
)
class ComponentGeneratorSettings : PersistentStateComponent<ComponentGeneratorSettings.State> {
    data class State(var themeName: String = "personal")

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state = state
    }

    var themeName: String
        get() = state.themeName
        set(value) {
            state.themeName = value
        }

    companion object {
        fun getInstance(project: Project): ComponentGeneratorSettings {
            return project.getService(ComponentGeneratorSettings::class.java)
        }

        fun normalizeThemeName(input: String): String {
            val trimmed = input.trim()
            val withoutSuffix = if (trimmed.endsWith(".libraries.yml")) {
                trimmed.removeSuffix(".libraries.yml").trimEnd()
            } else {
                trimmed
            }
            return normalizeIdentifier(withoutSuffix)
        }

        fun normalizeComponentName(input: String): String {
            return normalizeIdentifier(input)
        }

        private fun normalizeIdentifier(input: String): String {
            val lowered = input.trim().lowercase()
            if (lowered.isEmpty()) {
                return ""
            }

            val builder = StringBuilder()
            var lastUnderscore = false
            for (ch in lowered) {
                val isValid = (ch in 'a'..'z') || (ch in '0'..'9') || ch == '_'
                if (isValid) {
                    builder.append(ch)
                    lastUnderscore = false
                } else if (!lastUnderscore) {
                    builder.append('_')
                    lastUnderscore = true
                }
            }

            var normalized = builder.toString().trim('_')
            if (normalized.isEmpty()) {
                return ""
            }
            if (normalized[0].isDigit()) {
                normalized = "_$normalized"
            }
            return normalized
        }
    }
}
