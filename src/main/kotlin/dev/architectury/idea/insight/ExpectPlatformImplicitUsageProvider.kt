package dev.architectury.idea.insight

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import dev.architectury.idea.util.commonMethods
import dev.architectury.idea.util.isCommonExpectPlatform

class ExpectPlatformImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement): Boolean {
        // if the element is a class, mark it as used if it has any ExpectPlatform methods
        if (element is PsiClass) {
            return element.methods.any { it.isCommonExpectPlatform || it.commonMethods.isNotEmpty() }
        }

        // if the method is implementing a common ExpectPlatform method mark it as used
        if (element is PsiMethod) {
            return element.commonMethods.isNotEmpty()
        }

        // if the method is annotated with ExpectPlatform or implements an ExpectPlatform method,
        // mark all of its parameters as used.
        if (element is PsiParameter) {
            // the method is the parent's parent
            val parent = element.parent.parent

            return parent is PsiMethod && (parent.isCommonExpectPlatform || parent.commonMethods.isNotEmpty())
        }

        return false
    }

    override fun isImplicitRead(element: PsiElement): Boolean = false

    override fun isImplicitWrite(element: PsiElement): Boolean = false
}
