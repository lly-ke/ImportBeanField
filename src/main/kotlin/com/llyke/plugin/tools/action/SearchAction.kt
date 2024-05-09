package com.llyke.plugin.tools.action

import com.intellij.ide.actions.GotoActionAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.llyke.plugin.tools.action.search.ImportBeanFieldSearchGotoSEContributor


/**
 * @author lw
 * @date 2022/12/1 10:12
 */
class SearchAction : GotoActionAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val tabID = ImportBeanFieldSearchGotoSEContributor::class.java.simpleName
        showInSearchEverywherePopup(tabID, e, false, true)
    }

}
