package br.roriz.generator.generatoruml.generator

import br.roriz.generator.generatoruml.model.UmlModel
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

abstract class DiagramGenerator {
    val stringBuilder = StringBuilder()

    abstract fun getExtension(): String

    abstract fun setup(model: UmlModel)

    protected fun clear() {
        stringBuilder.clear()
    }

    fun export(project: Project) {
        WriteCommandAction.runWriteCommandAction(project) {
            try {
                val dateGenerate = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(Date())
                val baseDir = project.basePath?.let { File(it) } ?: File(System.getProperty("java.io.tmpdir"))
                val outFile = File(baseDir, "diagrama ${dateGenerate}${getExtension()}")
                outFile.writeText(stringBuilder.toString(), Charsets.UTF_8)

                val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(outFile)
                vFile?.let {
                    VfsUtil.markDirtyAndRefresh(false, false, false, it)
                    FileEditorManager.getInstance(project).openFile(it, true, true)
                }

                Messages.showInfoMessage(project, "Diagrama gerado: ${outFile.absolutePath}", "Gerador de diagrama")
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Erro ao gerar arquivo: ${ex.message}", "Gerador de diagrama")
            }
            clear()
        }
    }

}