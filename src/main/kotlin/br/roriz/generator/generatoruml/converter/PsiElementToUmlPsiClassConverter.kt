package br.roriz.generator.generatoruml.converter

import com.intellij.psi.*
import org.jetbrains.kotlin.asJava.toLightClass
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

class PsiElementToUmlPsiClassConverter {

    companion object {

        fun converter(psiElement: PsiElement?): List<PsiClass> {
            return when (psiElement) {
                is PsiClass -> converter(psiElement)
                is PsiDirectory -> converter(psiElement)
                is KtClass -> converter(psiElement)
                is KtFile -> converter(psiElement)
                else -> emptyList()
            }
        }

       private fun converter(element: PsiClass): List<PsiClass> {
            return listOf(element)
        }

        private  fun converter(element: PsiDirectory): List<PsiClass> {
            return collectJavaClassesFromDirectory(element)
        }

        private  fun converter(element: KtClass): List<PsiClass> {
            return element.toLightClass()?.let { listOf(it) } ?: emptyList()
        }

        private fun converter(element: KtFile): List<PsiClass> {
            return collectKtClassesFromFile(element)
        }

        private fun collectJavaClassesFromDirectory(dir: PsiDirectory): List<PsiClass> {
            val result = mutableListOf<PsiClass>()
            dir.accept(object : PsiRecursiveElementVisitor() {
                override fun visitFile(file: PsiFile) {
                    if (file is KtFile) result += file.classes
                }
            })
            return result
        }

        private fun collectKtClassesFromFile(file: KtFile): List<PsiClass> {
            val ktClasses = file.declarations.filterIsInstance<KtClass>().mapNotNull { it.toLightClass() }
            val psiClasses = file.declarations.filterIsInstance<PsiClass>()
            return mutableListOf<PsiClass>().apply {
                addAll(ktClasses)
                addAll(psiClasses)
            }
        }

    }
}