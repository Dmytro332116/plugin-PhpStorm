package com.splitwise.component.generator

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

object LibraryUpdater {
    fun libraryExists(projectRoot: VirtualFile, componentName: String): Boolean {
        val libraryFile = projectRoot.findChild("personal.libraries.yml")
            ?: throw GeneratorException("File 'personal.libraries.yml' not found in project root.")

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

    fun appendLibraryEntry(projectRoot: VirtualFile, componentName: String, isBlock: Boolean) {
        val libraryFile = projectRoot.findChild("personal.libraries.yml")
            ?: throw GeneratorException("File 'personal.libraries.yml' not found in project root.")

        val existing = VfsUtil.loadText(libraryFile)
        val entry = buildEntry(componentName, isBlock)
        val separator = if (existing.endsWith("\n")) "" else "\n"
        val updated = existing + separator + entry + "\n"
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
}
