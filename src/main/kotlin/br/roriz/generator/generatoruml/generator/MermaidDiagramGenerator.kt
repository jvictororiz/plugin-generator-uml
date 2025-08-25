package br.roriz.generator.generatoruml.generator

import br.roriz.generator.generatoruml.model.EdgeKind
import br.roriz.generator.generatoruml.model.UmlModel

class MermaidDiagramGenerator : DiagramGenerator() {
    override fun getExtension() = ".mermaid"

    override fun setup(model: UmlModel) {
        stringBuilder.appendLine("classDiagram")

        model.nodes.forEach { node ->
            val nodeName = node.name

            val canOpenKey = node.isInterface || node.isEnum || node.fields.isNotEmpty() || node.isSealed
            val openKey = if(canOpenKey) "{" else ""
            val closeKey = if(canOpenKey) "}" else ""

            stringBuilder.appendLine("class $nodeName $openKey")
            if (node.isInterface) {
                stringBuilder.appendLine("  <<interface>>")
            } else if (node.isEnum) {
                stringBuilder.appendLine("  <<enum>>")
            } else if(node.isSealed) {
                stringBuilder.appendLine("  <<sealed>>")
            }

            // Campos
            node.fields.forEach { field ->
                if (node.isEnum) {
                    stringBuilder.appendLine("  ${field.operator?.operatorName?:"+"} ${field.name}")
                } else {
                    stringBuilder.appendLine(" ${field.operator?.operatorName?:"+"} ${field.name} : ${field.type}")
                }
            }
            // Métodos
            node.methods.forEach { method ->
                stringBuilder.appendLine("  ${method.operator.operatorName} ${method.name}() ${method.dataReturn}")
            }

            stringBuilder.appendLine(closeKey)
        }

        model.edges.forEach { edge ->
            val arrow = when (edge.kind) {
                EdgeKind.EXTENDS -> "<|--"
                EdgeKind.IMPLEMENTS -> "<|.."
                EdgeKind.ASSOCIATION -> "-->"
                EdgeKind.INNER -> ".."
            }
            stringBuilder.appendLine("${edge.to} $arrow ${edge.from}")
        }
    }
}