package com.llyke.plugin.tools.action

import com.intellij.ide.actions.GotoActionBase
import com.intellij.ide.util.gotoByName.ChooseByNameModelEx
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.llyke.plugin.tools.action.search.IBFGotoActionCallBackWriter
import com.llyke.plugin.tools.action.search.ImportBeanFieldModel
import com.llyke.plugin.tools.action.search.SearchPopup


/**
 * @author lw
 * @date 2022/12/1 10:12
 */
class SearchAction : GotoActionBase() {

    companion object {
        val LOG = logger<SearchAction>()
    }

    override fun update(event: AnActionEvent) {
        super.update(event)
    }

    override fun gotoActionPerformed(e: AnActionEvent) {
        val writer = IBFGotoActionCallBackWriter(e)
        if (writer.detectionWriteTargetClass() == null) {
            // myInAction = null必须要调用, 负责会导致插件不可用
            myInAction = null
            return
        }
        val model = ImportBeanFieldModel(e.project ?: return)

        val project = e.getData(CommonDataKeys.PROJECT)
        val mayRequestOpenInCurrentWindow =
            model.willOpenEditor() && FileEditorManagerEx.getInstanceEx(project!!).hasSplitOrUndockedWindows()
        val start = getInitialText(true, e)
        val popup: ChooseByNamePopup = SearchPopup.createPopup(
            project, model, ChooseByNameModelEx.getItemProvider(model, getPsiContext(e)), start.first,
            mayRequestOpenInCurrentWindow,
            start.second
        )
        showNavigationPopup(object : GotoActionCallback<Any>() {
            override fun elementChosen(popup: ChooseByNamePopup?, element: Any?) {
                writer.elementChosen(popup, element)
            }
        }, null, popup, true)
    }

}