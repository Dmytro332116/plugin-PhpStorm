package com.splitwise.component.generator

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

object ComponentGenerator {
    fun generate(project: Project, componentName: String, isBlock: Boolean): Result {
        val basePath = project.basePath ?: return Result(false, "Project base path not found.")
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
            ?: return Result(false, "Project root not found.")

        return try {
            WriteCommandAction.runWriteCommandAction(project) {
                val componentsDir = baseDir.findChild("components")
                    ?: throw GeneratorException("Folder 'components' not found in project root.")

                if (componentsDir.findChild(componentName) != null) {
                    throw GeneratorException("Component '$componentName' already exists.")
                }

                if (LibraryUpdater.libraryExists(baseDir, componentName)) {
                    throw GeneratorException("Library \"$componentName\" already exists in personal.libraries.yml")
                }

                val targetDir = if (isBlock) {
                    componentsDir.findChild("block") ?: componentsDir.createChildDirectory(this, "block")
                } else {
                    componentsDir
                }

                val componentDir = targetDir.createChildDirectory(this, componentName)

                FileCreator.createScss(componentDir, componentName, isBlock)
                FileCreator.createJs(componentDir, componentName)

                LibraryUpdater.appendLibraryEntry(baseDir, componentName, isBlock)
            }
            Result(true, null)
        } catch (e: GeneratorException) {
            Result(false, e.message)
        } catch (e: Exception) {
            Result(false, "Unexpected error: ${e.message}")
        }
    }

    data class Result(val success: Boolean, val message: String?)
}
