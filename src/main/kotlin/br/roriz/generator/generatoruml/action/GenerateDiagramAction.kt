package br.roriz.generator.generatoruml.action

import br.roriz.generator.generatoruml.converter.PsiClassToUmlModelConverter
import br.roriz.generator.generatoruml.converter.PsiElementToUmlPsiClassConverter
import br.roriz.generator.generatoruml.generator.MermaidDiagramGenerator
import br.roriz.generator.generatoruml.generator.PlantUmlDiagramGenerator
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement

class GenerateDiagramAction : AnAction() {

    private val diagramGenerators = listOf(MermaidDiagramGenerator()/*, PlantUmlDiagramGenerator()*/)

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT


    override fun update(e: AnActionEvent) {
        val element = e.getData(CommonDataKeys.PSI_ELEMENT)
        e.presentation.isEnabledAndVisible = element != null
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val psiElement: PsiElement? = e.getData(CommonDataKeys.PSI_ELEMENT)

        val classes: List<PsiClass> = runReadAction {
            PsiElementToUmlPsiClassConverter.converter(psiElement)
        }

        if (classes.isEmpty()) {
            Messages.showWarningDialog(project, "Não foram encontradas classes na seleção.", "Generate Diagram")
            return
        }

        val model = runReadAction { PsiClassToUmlModelConverter.converter(classes) }

        diagramGenerators.forEach { generator ->
            generator.setup(model)
            generator.export(project)
        }
    }
}



