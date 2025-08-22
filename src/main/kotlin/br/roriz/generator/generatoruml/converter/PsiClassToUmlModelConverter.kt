package br.roriz.generator.generatoruml.converter

import br.roriz.generator.generatoruml.extension.populateAssociative
import br.roriz.generator.generatoruml.extension.populateInterfacesClass
import br.roriz.generator.generatoruml.extension.populateSuperClass
import br.roriz.generator.generatoruml.extension.toUmlModel
import br.roriz.generator.generatoruml.model.EdgeKind
import br.roriz.generator.generatoruml.model.UmlEdge
import br.roriz.generator.generatoruml.model.UmlModel
import com.intellij.psi.PsiClass

class PsiClassToUmlModelConverter {

    companion object {

        fun converter(psiClasses: List<PsiClass>): UmlModel {
            val umlModels = psiClasses
                .map { psiClass -> psiClass.toUmlModel() }
                .populateSuperClass()
                .populateInterfacesClass()
                .populateAssociative()

            val edges = mutableListOf<UmlEdge>()

            umlModels.forEach { umlModel ->
                if (umlModel.superClass != null) {
                    edges += UmlEdge(umlModel.name.orEmpty(), umlModel.superClass.name.orEmpty(), EdgeKind.EXTENDS)
                }

                if (umlModel.interfaces.isNotEmpty()) {
                    edges += umlModel.interfaces.map { currentInterface ->
                        UmlEdge(umlModel.name.orEmpty(), currentInterface.name.orEmpty(), EdgeKind.IMPLEMENTS)
                    }
                }

                if (umlModel.fields.any { it.associative != null }) {
                    edges += umlModel.fields.filter { it.associative != null }.map {
                        UmlEdge(it.associative.orEmpty(),umlModel.name.orEmpty() , EdgeKind.ASSOCIATION)
                    }
                }
            }
            return UmlModel(umlModels, edges)
        }
    }
}