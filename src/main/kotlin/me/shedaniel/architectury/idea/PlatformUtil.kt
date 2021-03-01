package me.shedaniel.architectury.idea

import com.intellij.openapi.module.ModuleUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope

const val EXPECT_PLATFORM = "me.shedaniel.architectury.annotations.ExpectPlatform"
const val OLD_EXPECT_PLATFORM = "me.shedaniel.architectury.ExpectPlatform"
const val EXPECT_PLATFORM_TRANSFORMED = "me.shedaniel.architectury.annotations.ExpectPlatform.Transformed"
val PLATFORMS = setOf("fabric", "forge")

val PsiMethod.isStatic: Boolean
    get() = modifierList.hasModifierProperty(PsiModifier.STATIC)

/**
 * True if this method is a common, untransformed `@ExpectPlatform` method.
 */
val PsiMethod.isCommonExpectPlatform: Boolean
    get() = isStatic
        && (hasAnnotation(EXPECT_PLATFORM) || hasAnnotation(OLD_EXPECT_PLATFORM))
        && !hasAnnotation(EXPECT_PLATFORM_TRANSFORMED)

// TODO: Cache these somehow? Both commonMethods and platformMethods might be really slow and could benefit from caching.

/**
 * The common declarations corresponding to this platform method.
 */
val PsiMethod.commonMethods: List<PsiMethod>
    get() {
        // no common methods for non-statics
        if (!isStatic) return emptyList()

        val clazz = containingClass ?: return emptyList()
        val name = clazz.binaryName ?: return emptyList()
        val pkg = name.substringBeforeLast('.')

        val nameMatches = name.endsWith("Impl") && PLATFORMS.any { pkg.endsWith(".$it") }
        if (!nameMatches) return emptyList()

        val commonPkg = pkg.substringBeforeLast('.')
        val commonClassName = name.substringAfterLast('.').removeSuffix("Impl")
        val baseClass = "$commonPkg.$commonClassName"

        return JavaPsiFacade.getInstance(project).findPackage(commonPkg)
            ?.getClasses(getScopeFor(this))
            ?.asSequence()
            ?.flatMap { it.asSequenceWithInnerClasses() }
            ?.filter { it.binaryName?.replace("$", "") == baseClass }
            ?.mapNotNull {
                it.findMethodBySignature(this, false)
            }
            ?.filter { it.isCommonExpectPlatform }
            ?.toList()
            ?: emptyList()
    }

/**
 * The platform implementations of this common method.
 */
val PsiMethod.platformMethods: List<PsiMethod>
    get() {
        if (!isCommonExpectPlatform) return emptyList()

        val containingClassName = containingClass?.binaryName ?: return emptyList()
        val parts = containingClassName.split('.')
        val head = parts.dropLast(1).joinToString(separator = ".")
        val tail = parts.last().replace("$", "")

        return PLATFORMS.asSequence().flatMap { platform ->
            val implementationClassName = "$head.$platform.${tail}Impl"

            JavaPsiFacade.getInstance(project)
                .findClasses(implementationClassName, getScopeFor(this))
                .asSequence()
                .mapNotNull { clazz ->
                    clazz.findMethodBySignature(this, false)
                }
        }.toList()
    }

/**
 * The binary name of this class in dot-dollar format (eg. `a.b.C$D`)
 */
val PsiClass.binaryName: String?
    get() =
        if (containingClass != null) containingClass!!.binaryName + "$" + name
        else qualifiedName

/**
 * Gets a sequence of this class and all its inner classes, recursed infinitely.
 */
fun PsiClass.asSequenceWithInnerClasses(): Sequence<PsiClass> =
    sequence {
        yield(this@asSequenceWithInnerClasses)
        yieldAll(innerClasses.asSequence().flatMap { it.asSequenceWithInnerClasses() })
    }

/**
 * Gets the searching scope for searching for classes related to the [element].
 * If the element's corresponding module is not null (= an element in this project),
 * uses the project scope. Otherwise uses the all scope.
 */
private fun getScopeFor(element: PsiElement): GlobalSearchScope =
    if (ModuleUtil.findModuleForPsiElement(element) != null) GlobalSearchScope.projectScope(element.project)
    else GlobalSearchScope.allScope(element.project)
