package br.roriz.generator.generatoruml.model

data class UmlEdge(
    val from: String,
    val to: String,
    val kind: EdgeKind
)
