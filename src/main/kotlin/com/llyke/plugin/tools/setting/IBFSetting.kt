package com.llyke.plugin.tools.setting

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.llyke.plugin.tools.dialog.search.ConfigDialogForm
import java.io.Serializable

/**
 * @author lw
 * @date 2022/12/28 14:19
 */
@State(
    name = "ImportBeanFieldSetting",
    storages = [Storage("\$APP_CONFIG$/ImportBeanField.xml")]
)
class IBFSetting : PersistentStateComponent<IBFSetting>, Serializable {

    var injectMode: Int? = null
    var fieldInjectMode: Int? = null

    companion object {
        @JvmStatic
        fun getInstance(): IBFSetting {
            val ibfSetting = ApplicationManager.getApplication().getService(IBFSetting::class.java)
            ibfSetting.injectMode = ibfSetting.injectMode ?: 0
            ibfSetting.fieldInjectMode = ibfSetting.fieldInjectMode ?: 0
            return ibfSetting
        }
    }

    override fun getState(): IBFSetting {
        return this
    }

    override fun loadState(state: IBFSetting) {
        XmlSerializerUtil.copyBean(state, this);
    }

    /**
     * 解析ConfigDialogForm的选中值到this中
     */
    fun parseFormData(configDialogForm: ConfigDialogForm) {
        injectMode =
            if (configDialogForm.injectModeGroup.isSelected(configDialogForm.fieldInjectRadioButton.model)) 0 else
                if (configDialogForm.injectModeGroup.isSelected(configDialogForm.constructionInjectRadioButton.model)) 1 else 0
        fieldInjectMode =
            if (configDialogForm.fieldInjectGroup.isSelected(configDialogForm.autowiredFieldInjectRadioButton.model)) 0 else
                if (configDialogForm.fieldInjectGroup.isSelected(configDialogForm.resourceJavaXFieldInjectRadioButton.model)) 1 else
                    if (configDialogForm.fieldInjectGroup.isSelected(configDialogForm.resourceJakartaFieldInjectRadioButton.model)) 2 else 0
    }

}