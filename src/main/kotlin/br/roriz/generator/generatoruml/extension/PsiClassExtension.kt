package br.roriz.generator.generatoruml.extension

import br.roriz.generator.generatoruml.model.Field
import br.roriz.generator.generatoruml.model.Method
import br.roriz.generator.generatoruml.model.UmlClass
import com.intellij.psi.PsiClass
import java.util.*


fun PsiClass.toUmlModel(): UmlClass {
    val excludedMethods = mutableListOf(
        "copy", "component1", "component2", "component3",
        "equals", "hashCode", "toString"
    )
    excludedMethods.add(name.orEmpty())
    val className = name ?: "<anonymous>"
    val fields = fields.map {
        Field(name = it.name, type = it.type.presentableText)
    }
    val gettersAndSetters = fields.flatMap { field ->
        val propName = field.name
        val capitalized = propName.replaceFirstChar { it.uppercase() }
        listOf("get$capitalized", "set$capitalized")
    }.toSet()
    val methods = if (isEnum) listOf() else methods
        .filter { method -> method.name !in excludedMethods && !method.name.startsWith("component") }
        .filter { method ->
            method.name !in gettersAndSetters
        }
        .map { method ->
            Method(
                name = method.name,
                param = method.parameters.map { parameter ->
                    Field(name = parameter.name.orEmpty(), type = parameter.type.toString())
                },
                dataReturn = method.returnType?.presentableText ?: "void"
            )
        }
    var superClass = superClass?.name
    if (superClass.equals("Object", ignoreCase = true)) {
        superClass = null
    }
    val interfaces = interfaces.mapNotNull { it.name }
    return UmlClass(
        id = UUID.randomUUID().toString(),
        name = className,
        isInterface = isInterface,
        isEnum = isEnum,
        fields = fields,
        methods = methods,
        superClass = superClass?.let { UmlClass(name = superClass) },
        interfaces = interfaces.map { UmlClass(name = it, isInterface = true) }
    )
}

fun List<UmlClass>.populateSuperClass(): List<UmlClass> {
    return map { currentClass ->
        if (currentClass.superClass == null) {
            currentClass
        } else {
            currentClass.copy(
                superClass = find { it.name == currentClass.superClass.name }
            )
        }
    }
}

fun List<UmlClass>.populateInterfacesClass(): List<UmlClass> {
    return map { currentClass ->
        currentClass.copy(
            interfaces = currentClass.interfaces.mapNotNull { currentInterface ->
                val classMatch = this.find { it.name == currentInterface.name }
                classMatch
            }
        )
    }
}

fun List<UmlClass>.populateAssociative(): List<UmlClass> {
    return map { currentClass ->
        if (currentClass.isEnum || currentClass.isInterface) {
            currentClass
        } else {
            currentClass.copy(
                fields = currentClass.fields.map { currentField ->
                    currentField.copy(
                        associative = find { itemList ->
                            val nameType = if ("<" in currentField.type && ">" in currentField.type) {
                                currentField.type.substringAfter("<").substringBeforeLast(">")
                            } else {
                                currentField.type
                            }
                            itemList.name == nameType
                        }?.name
                    )
                }
            )
        }
    }
}