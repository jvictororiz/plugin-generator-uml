package br.roriz.generator.generatoruml.util

import br.roriz.generator.generatoruml.model.Field
import br.roriz.generator.generatoruml.model.Method
import br.roriz.generator.generatoruml.model.TypeOperator
import br.roriz.generator.generatoruml.model.UmlClass
import com.intellij.lang.jvm.JvmField
import com.intellij.lang.jvm.JvmMethod
import com.intellij.lang.jvm.JvmModifier
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.asJava.elements.KtLightElement
import org.jetbrains.kotlin.asJava.elements.KtLightMethod
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNullableType
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeReference
import java.util.*


fun PsiClass.toUmlModel(): List<UmlClass> {
    val excludedMethods = mutableListOf(
        "copy", "component1", "component2", "component3",
        "equals", "hashCode", "toString"
    )
    excludedMethods.add(name.orEmpty())
    val className = name ?: "<anonymous>"
    val fields = this.generateFields()
    val gettersAndSetters = fields.flatMap { field ->
        val propName = field.name
        val capitalized = propName.replaceFirstChar { it.uppercase() }
        listOf("get$capitalized", "set$capitalized")
    }.toSet()
    val methods = if (isEnum) listOf() else methods
        .filter { method -> method.name !in excludedMethods && !method.name.startsWith("component") }
        .filter { method -> method.name !in gettersAndSetters }
        .map { method ->
            Method(
                name = method.name,
                param = method.parameters.map { parameter ->
                    Field(
                        name = parameter.name.orEmpty(),
                        type = parameter.type.toString(),
                        operator = null
                    )
                },
                operator = method.toTypeOperator(),
                dataReturn = (method.returnType?.presentableText + (if (method.isReturnTypeNullable()) "?" else ""))
            )
        }
    var superClass = superClass?.name
    if (superClass.equals("Object", ignoreCase = true)) {
        superClass = null
    }
    val interfaces = interfaces.mapNotNull { it.name }

    val currentClass = UmlClass(
        id = UUID.randomUUID().toString(),
        name = className,
        isInterface = isInterface,
        isSealed = isSealedClass(),
        isEnum = isEnum,
        fields = fields,
        methods = methods,
        superClass = superClass?.let { UmlClass(name = superClass) },
        interfaces = interfaces.map { UmlClass(name = it, isInterface = true) },
    )

    val innerClasses = if(this.innerClasses.isNotEmpty()) {
        this.innerClasses.map {
            UmlClass(
                name = "`$className.${it.name}`",
                fields = it.generateFields(),
                innerFrom = className
            )
        }
    } else {
        emptyList()
    }

    return listOf(
//        *sealedClasses.toTypedArray(),
        *innerClasses.toTypedArray(),
        currentClass
    )
}

private fun PsiClass.generateFields(): List<Field> {
    return fields.filter { it.name != "INSTANCE" }.map {
        Field(
            name = it.name,
            type = it.type.presentableText + if (it.isNullable()) "?" else "",
            operator = it.toTypeOperator(),
        )
    }
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
                            }.replace("?","")
                            itemList.name == nameType
                        }?.name
                    )
                }
            )
        }
    }
}

private fun PsiClass.isSealedClass(): Boolean {
    val ktClass = this.navigationElement as? KtClass
    return ktClass?.isSealed() == true
}

fun JvmMethod.toTypeOperator(): TypeOperator {
    return when {
        this.hasModifier(JvmModifier.PRIVATE) -> TypeOperator.PRIVATE
        this.hasModifier(JvmModifier.PROTECTED) -> TypeOperator.PROTECTED
        this.hasModifier(JvmModifier.PUBLIC) -> TypeOperator.PUBLIC
        else -> TypeOperator.PUBLIC // default
    }
}


fun JvmField.toTypeOperator(): TypeOperator {
    return when {
        this.hasModifier(JvmModifier.PRIVATE) -> TypeOperator.PRIVATE
        this.hasModifier(JvmModifier.PROTECTED) -> TypeOperator.PROTECTED
        this.hasModifier(JvmModifier.PUBLIC) -> TypeOperator.PUBLIC
        else -> TypeOperator.PUBLIC // default
    }
}

fun JvmField.isNullable(): Boolean {
    if (this is PsiField && this !is KtLightElement<*, *>) {
        return true
    }
    val typeElement = when (val origin = (this as? KtLightElement<*, *>)?.kotlinOrigin) {
        is KtProperty -> origin.typeReference?.typeElement
        is KtParameter -> origin.typeReference?.typeElement
        else -> null
    }
    return typeElement is KtNullableType
}

fun PsiMethod.isReturnTypeNullable(): Boolean {
    if (this !is KtLightMethod) return true
    val ktOrigin = this.kotlinOrigin as? KtFunction ?: return false
    val typeRef: KtTypeReference? = ktOrigin.typeReference
    return typeRef?.typeElement is KtNullableType
}