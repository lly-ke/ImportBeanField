package com.llyke.plugin.tools.setting

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.util.ClearableLazyValue
import com.llyke.plugin.tools.dialog.search.ConfigDialogForm
import javax.swing.JComponent

/**
 * @author lw
 * @date 2023/1/5 14:46
 */
class IBFConfigurable : SearchableConfigurable {

    private val ui: ClearableLazyValue<ConfigDialogForm> = ClearableLazyValue.create {
        ConfigDialogForm()
    }

    override fun createComponent(): JComponent? {
        return ui.value.mainPanel
    }

    override fun isModified(): Boolean {
        // 懒得判断了, 直接返回true
        return true
    }

    override fun apply() {
        IBFSetting.getInstance().parseFormData(ui.value)
    }

    override fun getDisplayName(): String {
        return id
    }

    override fun getId(): String {
        return "ImportBeanField"
    }

    override fun getHelpTopic(): String? {
        return id
    }


}