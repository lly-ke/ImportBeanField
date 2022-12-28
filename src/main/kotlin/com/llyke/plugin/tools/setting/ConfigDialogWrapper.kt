package com.llyke.plugin.tools.setting;

import com.intellij.openapi.ui.DialogWrapper
import com.llyke.plugin.tools.dialog.search.ConfigDialogForm
import javax.swing.Action
import javax.swing.JComponent

/**
 * @author lw
 * @date 2022/12/28 10:07
 */
class ConfigDialogWrapper : DialogWrapper(true) {

    val configDialogForm = ConfigDialogForm()

    init {
        init();
        title = "配置";
    }

    override fun createCenterPanel(): JComponent? {
        return configDialogForm.mainPanel;
    }

    override fun createActions(): Array<Action> {
        val exitAction = DialogWrapperExitAction("取消", CANCEL_EXIT_CODE)
        val okAction = DialogWrapperExitAction("确定", OK_EXIT_CODE).also {
            it.putValue(DEFAULT_ACTION, true)
        }
        return arrayOf(exitAction, okAction)
    }
}
