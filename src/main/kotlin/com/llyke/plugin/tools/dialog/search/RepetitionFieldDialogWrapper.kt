package com.llyke.plugin.tools.dialog.search

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import java.awt.BorderLayout
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel


/**
 * @author lw
 * @date 2022/12/10 10:45
 */
class RepetitionFieldDialogWrapper(
    private val psiClassType: PsiClassType,
    private val detectionWriteTargetClass: PsiClass
) : DialogWrapper(true) {
    init {
        init()
        title = ""
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())

        val label =
            JLabel("当前 ${detectionWriteTargetClass.name} 类中已经存在 ${psiClassType.name} 类型的字段, 确认添加吗?")
        dialogPanel.add(label, BorderLayout.CENTER)

        return dialogPanel
    }

    override fun createActions(): Array<Action> {
        val exitAction = DialogWrapperExitAction("取消", CANCEL_EXIT_CODE)
        val okAction = DialogWrapperExitAction("确定", OK_EXIT_CODE).also {
            it.putValue(DEFAULT_ACTION, true)
        }
        return arrayOf(exitAction, okAction)
    }
}