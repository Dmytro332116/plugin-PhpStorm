package com.splitwise.component.generator

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object ComponentGenerator {
    fun generate(project: Project, componentName: String, isBlock: Boolean): Result {
        val basePath = project.basePath ?: return Result(false, "Project base path not found.")
        val baseDir = LocalFileSystem.getInstance().findFileByPath(basePath)
            ?: return Result(false, "Project root not found.")

        return try {
            WriteCommandAction.runWriteCommandAction(project) {
                baseDir.refresh(false, true)
                val componentsDir = findComponentsDir(project, baseDir)
                    ?: throw GeneratorException(
                        "Folder 'components' not found in project root or content roots."
                    )

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

    private fun findComponentsDir(project: Project, baseDir: VirtualFile): VirtualFile? {
        if (baseDir.name == "components") {
            return baseDir
        }

        baseDir.findChild("components")?.takeIf { it.isDirectory }?.let { return it }

        val roots = ProjectRootManager.getInstance(project).contentRoots
        val rootCandidates = roots.mapNotNull { root ->
            if (root.name == "components" && root.isDirectory) {
                root
            } else {
                root.findChild("components")?.takeIf { it.isDirectory }
            }
        }
        chooseBestCandidate(rootCandidates, baseDir)?.let { return it }

        val indexed = FilenameIndex.getVirtualFilesByName(
            project,
            "components",
            GlobalSearchScope.projectScope(project)
        ).filter { it.isDirectory }

        return chooseBestCandidate(indexed, baseDir)
    }

    private fun chooseBestCandidate(candidates: Collection<VirtualFile>, baseDir: VirtualFile): VirtualFile? {
        if (candidates.isEmpty()) return null
        val basePath = baseDir.path
        return candidates.minWith(
            compareBy<VirtualFile> { candidate ->
                if (candidate.path.startsWith(basePath)) 0 else 1
            }.thenBy { candidate ->
                candidate.path.count { it == '/' }
            }
        )
    }
}
