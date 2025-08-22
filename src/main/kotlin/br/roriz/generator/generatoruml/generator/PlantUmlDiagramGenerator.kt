package br.roriz.generator.generatoruml.generator

import br.roriz.generator.generatoruml.model.EdgeKind
import br.roriz.generator.generatoruml.model.UmlModel

class PlantUmlDiagramGenerator : DiagramGenerator() {
    override fun getExtension() = ".plantuml"

    override fun setup(model: UmlModel) {
        stringBuilder.appendLine("@startuml")

        // --- Classes ---
        model.nodes.forEach { node ->
            if (node.isInterface) {
                stringBuilder.appendLine("interface ${node.name} {")
            } else {
                stringBuilder.appendLine("class ${node.name} {")
            }

            // Campos
            node.fields.forEach { f ->
                stringBuilder.appendLine("  - $f")
            }
            // Métodos
            node.methods.forEach { m ->
                stringBuilder.appendLine("  + $m")
            }

            stringBuilder.appendLine("}")
        }

        // --- Relações ---
        model.edges.forEach { e ->
            val arrow = when (e.kind) {
                EdgeKind.EXTENDS -> "--|>"
                EdgeKind.IMPLEMENTS -> "..|>"
                EdgeKind.ASSOCIATION -> "-->"
            }
            stringBuilder.appendLine("${e.from} $arrow ${e.to}")
        }

        stringBuilder.appendLine("@enduml")
    }
}