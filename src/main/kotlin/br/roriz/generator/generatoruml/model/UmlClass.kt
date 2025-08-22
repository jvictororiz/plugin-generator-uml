package br.roriz.generator.generatoruml.model

import com.jetbrains.rd.generator.nova.PredefinedType

data class UmlModel(val nodes: List<UmlClass>, val edges: List<UmlEdge>)

data class UmlClass(
    val id: String = "",
    val name: String?,
    val isInterface: Boolean = false,
    val isEnum: Boolean = false,
    val fields: List<Field> = emptyList(),
    val methods: List<Method> = emptyList(),
    val superClass: UmlClass? = null,
    val interfaces: List<UmlClass> = emptyList()
)

data class Field(
    val name: String,
    val type: String,
    val associative: String? = null
)

data class Method(
    val name: String,
    val param: List<Field>,
    val dataReturn: String
)