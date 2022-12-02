package com.llyke.plugin.tools.action.search

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project

class ImportBeanFieldContributor : ChooseByNameContributor {


    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
        return Array(10) { it.toString() }
    }

    override fun getItemsByName(
        name: String?,
        pattern: String?,
        project: Project?,
        includeNonProjectItems: Boolean
    ): Array<NavigationItem> {
        TODO("Not yet implemented")
    }
}
