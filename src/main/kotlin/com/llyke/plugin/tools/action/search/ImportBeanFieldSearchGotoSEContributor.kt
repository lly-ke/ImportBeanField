package com.llyke.plugin.tools.action.search

import com.intellij.ide.actions.searcheverywhere.AbstractGotoSEContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory
import com.intellij.ide.util.gotoByName.FilteringGotoByModel
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.llyke.plugin.tools.IBFBundle

/**
 * @author lw
 * @date 2022/12/1 10:30
 */
class ImportBeanFieldSearchGotoSEContributor(e: AnActionEvent) : AbstractGotoSEContributor(e) {


    override fun getGroupName(): String {
        return IBFBundle.getMessage("name")
    }

    override fun getSortWeight(): Int {
        return 100
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
            project,
//            emptyArray<ChooseByNameContributor>().asList()
//            ExtensionPointName.create<ChooseByNameContributor>("com.llyke.plugin.tools.requestMappingContributor").extensionList
        )
    }

    override fun showInFindResults(): Boolean {
        return false
    }

    internal class Factory : SearchEverywhereContributorFactory<Any> {
        override fun createContributor(e: AnActionEvent): SearchEverywhereContributor<Any> {
            return ImportBeanFieldSearchGotoSEContributor(e)
        }
    }


}