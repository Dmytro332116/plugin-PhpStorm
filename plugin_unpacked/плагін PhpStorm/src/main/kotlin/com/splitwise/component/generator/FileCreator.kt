package com.splitwise.component.generator

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

object FileCreator {
    fun createScss(componentDir: VirtualFile, componentName: String, isBlock: Boolean) {
        val templatePath = if (isBlock) "templates/scss-block.tpl" else "templates/scss.tpl"
        val content = loadTemplate(templatePath).replace("{component}", componentName)
        val file = componentDir.createChildData(this, "$componentName.scss")
        VfsUtil.saveText(file, content)
    }

    fun createJs(componentDir: VirtualFile, componentName: String) {
        val content = loadTemplate("templates/js.tpl").replace("{component}", componentName)
        val file = componentDir.createChildData(this, "$componentName.js")
        VfsUtil.saveText(file, content)
    }

    private fun loadTemplate(path: String): String {
        val stream = FileCreator::class.java.classLoader.getResourceAsStream(path)
            ?: throw GeneratorException("Template not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }
}
