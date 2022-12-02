package com.llyke.plugin.tools.action.search

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.llyke.plugin.tools.IBFBundle

class ImportBeanFieldModel(project: Project) :
    GotoSymbolModel2(project), DumbAware {

    override fun getItemProvider(context: PsiElement?): ChooseByNameItemProvider {
        return DefaultChooseByNameItemProvider(context)
    }


    override fun acceptItem(item: NavigationItem?): Boolean {
        item ?: return false
        when (item) {
            is PsiField -> {
                return false
            }
        }
        return true
    }

    override fun getPromptText(): String = IBFBundle.getMessage("model.ibf.prompt.text")

//    override fun getNotInMessage(): String = IBFBundle.getMessage("model.ibf.not.in.message")
//
//    override fun getNotFoundMessage(): String = IBFBundle.getMessage("model.ibf.not.found.message")

//    override fun getCheckBoxName(): String? = null
//
//    override fun loadInitialCheckBoxState(): Boolean = false
//
//    override fun saveInitialCheckBoxState(state: Boolean) = Unit
//
//    override fun getSeparators(): Array<String> = emptyArray()
//
//    override fun getFullName(element: Any): String? = getElementName(element)
}