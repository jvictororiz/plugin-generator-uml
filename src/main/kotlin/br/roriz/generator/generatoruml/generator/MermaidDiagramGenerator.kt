package br.roriz.generator.generatoruml.generator

import br.roriz.generator.generatoruml.model.EdgeKind
import br.roriz.generator.generatoruml.model.UmlModel

class MermaidDiagramGenerator : DiagramGenerator() {
    override fun getExtension() = ".mermaid"

    override fun setup(model: UmlModel) {
        stringBuilder.appendLine("---")
        stringBuilder.appendLine("title: Diagrama de classes")
        stringBuilder.appendLine("---")
        stringBuilder.appendLine("")
        stringBuilder.appendLine("classDiagram")

        model.nodes.forEach { node ->
            val nodeName = node.name

            stringBuilder.appendLine("class $nodeName {")
            if (node.isInterface) {
                stringBuilder.appendLine("  <<interface>>")
            } else if (node.isEnum) {
                stringBuilder.appendLine("  <<enum>>")
            }

            // Campos
            node.fields.forEach { field ->
                if (node.isEnum) {
                    stringBuilder.appendLine("  - ${field.name}")
                } else {
                    stringBuilder.appendLine("  - ${field.name} : ${field.type}")
                }
            }
            // Métodos
            node.methods.forEach { method ->
                stringBuilder.appendLine("  + ${method.name}() : ${method.dataReturn}")
            }

            stringBuilder.appendLine("}")
        }

        model.edges.forEach { edge ->
            val arrow = when (edge.kind) {
                EdgeKind.EXTENDS -> "<|--"
                EdgeKind.IMPLEMENTS -> "<|.."
                EdgeKind.ASSOCIATION -> "-->"
            }
            stringBuilder.appendLine("${edge.to} $arrow ${edge.from}")
        }
    }
}