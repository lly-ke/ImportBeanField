package com.llyke.plugin.tools.action.search

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.llyke.plugin.tools.IBFBundle
import com.llyke.plugin.tools.setting.IBFSetting

/**
 * @author lw
 * @date 2022/12/1 10:30
 */
class ImportBeanFieldSearchGotoSEContributor(private val e: AnActionEvent) : AbstractGotoSEContributor(e) {

    class InClassSearchAction : DumbAwareToggleAction(IBFBundle.getMessage("model.ibf.checkbox.name")), DumbAware {
        override fun isSelected(e: AnActionEvent): Boolean {
            return IBFSetting.getInstance().includeClassBySearch == true
        }

        override fun setSelected(e: AnActionEvent, state: Boolean) {
            val instance = IBFSetting.getInstance()
            instance.includeClassBySearch = state
        }

    }

    override fun getActions(onChanged: Runnable): MutableList<AnAction> {
        val list = mutableListOf<AnAction>()
        list.add(InClassSearchAction())
        return list
    }


    override fun getGroupName(): String {
        return IBFBundle.getMessage("name")
    }

    override fun getSortWeight(): Int {
        return 1
    }

    override fun getAdvertisement(): String {
        return IBFBundle.getMessage("search.ibf.filter.classes.description")
    }

    override fun createModel(project: Project): FilteringGotoByModel<*> {
//        val model = GotoSymbolModel2(project)
//        if (myFilter != null) {
//            model.setFilterItems(myFilter.getSelectedElements())
//        }
//        return model
        return ImportBeanFieldModel(
            project, this
//            emptyArray<ChooseByNameContributor>().asList()
//            ExtensionPointName.create<ChooseByNameContributor>("com.llyke.plugin.tools.requestMappingContributor").extensionList
        )
    }

    override fun isEmptyPatternSupported(): Boolean {
        return true
    }

    override fun processSelectedItem(selected: Any, modifiers: Int, searchText: String): Boolean {
        (selected as? PsiElement)?.let {
            IBFGotoActionCallBackWriter(e).elementChosen(it)
        }
        return true
    }

    internal class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(e: AnActionEvent): SearchEverywhereContributor<Any> {
            return ImportBeanFieldSearchGotoSEContributor(e)
        }
    }


}
