package com.llyke.plugin.tools.action.search

import com.intellij.ide.util.gotoByName.*
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.llyke.plugin.tools.IBFBundle
import javax.swing.JCheckBox

class ImportBeanFieldModel(project: Project) :
    GotoSymbolModel2(project), DumbAware {

    override fun getItemProvider(context: PsiElement?): ChooseByNameItemProvider {
        return DefaultChooseByNameItemProvider(context)
    }

    override fun getContributorList(): MutableList<ChooseByNameContributor> {
        // todo 暂时找不到对应的接口, 后期换成接口调用
        val contributorList = project.getUserData(ChooseByNamePopup.CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY)?.let {
            val field =
                Class.forName("com.intellij.ide.util.gotoByName.ChooseByNameBase").getDeclaredField("myCheckBox")
            field.isAccessible = true
            val myCheckBox = field.get(it) as JCheckBox
            if (myCheckBox.isSelected) {
                mutableListOf<ChooseByNameContributor>(DefaultClassNavigationContributor())
            } else {
                mutableListOf<ChooseByNameContributor>()
            }
        } ?: mutableListOf<ChooseByNameContributor>()

        try {
            val clazz = Class.forName("com.intellij.spring.navigation.GotoSpringBeanProvider")
            val declaredConstructor = clazz.getDeclaredConstructor()
            declaredConstructor.isAccessible = true
            contributorList.add(declaredConstructor.newInstance() as ChooseByNameContributor)
        } catch (e: ClassNotFoundException) {
            LOG.error("GotoSpringBeanProvider not found")
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