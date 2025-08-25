package br.roriz.generator.generatoruml.model

data class UmlModel(val nodes: List<UmlClass>, val edges: List<UmlEdge>)

data class UmlClass(
    val id: String = "",
    val name: String?,
    val isInterface: Boolean = false,
    val isEnum: Boolean = false,
    val isSealed: Boolean = false,
    val fields: List<Field> = emptyList(),
    val methods: List<Method> = emptyList(),
    val superClass: UmlClass? = null,
    val interfaces: List<UmlClass> = emptyList(),
    val innerFrom: String = ""
)

data class Field(
    val name: String,
    val type: String,
    val associative: String? = null,
    val operator: TypeOperator?
)

data class Method(
    val name: String,
    val param: List<Field>,
    val dataReturn: String,
    val operator: TypeOperator
)

enum class  TypeOperator(
    val operatorName: String
) {
    PUBLIC("+"), PRIVATE("-"), PROTECTED("#")
}