package com.llyke.plugin.tools.action.search

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.DefaultChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.DefaultClassNavigationContributor
import com.intellij.ide.util.gotoByName.GotoSymbolModel2
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.ChooseByNameRegistry
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.llyke.plugin.tools.IBFBundle

class ImportBeanFieldModel(project: Project) :
    GotoSymbolModel2(project), DumbAware {

    var includeClassBySearch: Boolean = false

    override fun getItemProvider(context: PsiElement?): ChooseByNameItemProvider {
        return DefaultChooseByNameItemProvider(context)
    }

    override fun getContributorList(): MutableList<ChooseByNameContributor> {
        val contributorList = mutableListOf<ChooseByNameContributor>()
        if (includeClassBySearch) {
            contributorList.add(DefaultClassNavigationContributor())
        }

        // todo 暂时找不到对应的接口, 后期换成接口调用
        ChooseByNameRegistry.getInstance().symbolModelContributors.filter { it.javaClass.simpleName == "GotoSpringBeanProvider" }
            .forEach {
                contributorList.add(it)
            }

        return contributorList
//        return ExtensionPointName.create<ChooseByNameContributor>("com.llyke.plugin.tools.IBFContributor").extensionList
    }

    override fun getPromptText(): String = IBFBundle.getMessage("model.ibf.prompt.text")

    //    override fun getNotInMessage(): String = IBFBundle.getMessage("model.ibf.not.in.message")
    override fun getCheckBoxName(): String = IBFBundle.getMessage("model.ibf.checkbox.name")
    override fun getNotFoundMessage(): String {
        return IBFBundle.getMessage("model.ibf.not.found.message")
    }

    //    override fun loadInitialCheckBoxState(): Boolean = false
//
//    override fun saveInitialCheckBoxState(state: Boolean) = Unit
//
//    override fun getSeparators(): Array<String> = emptyArray()
//
//    override fun getFullName(element: Any): String? = getElementName(element)
}