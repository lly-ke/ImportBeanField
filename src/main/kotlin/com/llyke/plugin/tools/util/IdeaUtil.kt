package com.llyke.plugin.tools.util

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SyntheticElement
import com.intellij.psi.util.PsiTreeUtil

/**
 * @author lw
 * @date 2022/12/1 00:10
 */
class IdeaUtil {

    companion object {

        fun toCamelCase(name: String): String {
            val chars = name.toCharArray()
            chars[0] = chars[0].lowercaseChar()
            return String(chars)
        }


        fun getTargetClassByCursor(editor: Editor, file: PsiFile): PsiClass? {
            val element: PsiElement = file.findElementAt(editor.caretModel.offset) ?: return null
            val target: PsiClass? = PsiTreeUtil.getParentOfType(element, PsiClass::class.java)
            return if (target is SyntheticElement) null else target
        }
    }

}