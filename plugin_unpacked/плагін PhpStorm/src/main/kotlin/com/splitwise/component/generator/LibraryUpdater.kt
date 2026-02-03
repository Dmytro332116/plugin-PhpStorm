package com.splitwise.component.generator

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

object LibraryUpdater {
    fun libraryExists(
        project: Project,
        baseDir: VirtualFile,
        componentsDir: VirtualFile?,
        componentName: String
    ): Boolean {
        val libraryFile = findLibraryFile(project, baseDir, componentsDir)
            ?: throw GeneratorException("File 'personal.libraries.yml' not found in project root or content roots.")

        val existing = VfsUtil.loadText(libraryFile)
        return existing.split('\n').any { line ->
            val cleaned = line.trimEnd('\r').removePrefix("\uFEFF")
            if (cleaned.isEmpty()) {
                return@any false
            }
            if (cleaned[0].isWhitespace()) {
                return@any false
            }
            if (!cleaned.startsWith(componentName)) {
                return@any false
            }
            if (cleaned.length <= componentName.length || cleaned[componentName.length] != ':') {
                return@any false
            }
            cleaned.substring(componentName.length + 1).all { it == ' ' || it == '\t' }
        }
    }

    fun appendLibraryEntry(
        project: Project,
        baseDir: VirtualFile,
        componentsDir: VirtualFile?,
        componentName: String,
        isBlock: Boolean
    ) {
        val libraryFile = findLibraryFile(project, baseDir, componentsDir)
            ?: throw GeneratorException("File 'personal.libraries.yml' not found in project root or content roots.")

        val existing = VfsUtil.loadText(libraryFile)
        val entry = buildEntry(componentName, isBlock)
        val trimmed = existing.trimEnd('\n', '\r')
        val separator = if (trimmed.isEmpty()) "" else "\n\n"
        val updated = trimmed + separator + entry + "\n"
        VfsUtil.saveText(libraryFile, updated)
    }

    private fun buildEntry(componentName: String, isBlock: Boolean): String {
        val pathPrefix = if (isBlock) "components/block" else "components"
        return "$componentName:\n" +
            "  css:\n" +
            "    theme:\n" +
            "      $pathPrefix/$componentName/$componentName.scss: {}\n" +
            "  js:\n" +
            "    $pathPrefix/$componentName/$componentName.js: {}"
    }

    private fun findLibraryFile(
        project: Project,
        baseDir: VirtualFile,
        componentsDir: VirtualFile?
    ): VirtualFile? {
        baseDir.findChild("personal.libraries.yml")?.let { return it }

        var cursor: VirtualFile? = componentsDir
        while (cursor != null) {
            cursor.findChild("personal.libraries.yml")?.let { return it }
            cursor = cursor.parent
        }

        val roots = ProjectRootManager.getInstance(project).contentRoots
        roots.mapNotNull { it.findChild("personal.libraries.yml") }.firstOrNull()?.let { return it }

        val indexed = FilenameIndex.getVirtualFilesByName(
            project,
            "personal.libraries.yml",
            GlobalSearchScope.projectScope(project)
        )
        return indexed.firstOrNull()
    }
}
