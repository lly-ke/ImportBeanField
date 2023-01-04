package com.llyke.plugin.tools.action.search;

import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.ide.util.gotoByName.ChooseByNamePopupComponent
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.llyke.plugin.tools.setting.ConfigDialogWrapper
import com.llyke.plugin.tools.setting.IBFSetting
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent

/**
 * @author lw
 * @date 2022/12/28 11:03
 */
class SearchPopup(
    project: Project?,
    private val model: ImportBeanFieldModel,
    provider: ChooseByNameItemProvider,
    oldPopup: ChooseByNamePopup?,
    predefinedText: String?,
    mayRequestOpenInCurrentWindow: Boolean,
    initialIndex: Int
) : ChooseByNamePopup(
    project,
    model,
    provider,
    oldPopup,
    predefinedText,
    mayRequestOpenInCurrentWindow,
    initialIndex
) {
    override fun initUI(
        callback: ChooseByNamePopupComponent.Callback?,
        modalityState: ModalityState?,
        allowMultipleSelection: Boolean
    ) {
        super.initUI(callback, modalityState, allowMultipleSelection)

        myCheckBox.addChangeListener {
            model.includeClassBySearch = myCheckBox.isSelected
        }
        textField.addKeyListener(object : KeyAdapter() {
            override fun keyReleased(e: KeyEvent) {
                // alt + enter
                // todo 快捷键可以配置
                if (e.keyCode == KeyEvent.VK_ENTER && e.isAltDown) {
                    val elements = chosenElements
                    if (elements.isNotEmpty()) {
                        val configDialogWrapper = ConfigDialogWrapper()
                        if (configDialogWrapper.showAndGet()) {
                            IBFSetting.getInstance().parseFormData(configDialogWrapper.configDialogForm)
                            for (element in elements) {
                                myActionListener.elementChosen(element);
                            }
                        }
                    }
                }
            }
        })
    }

    companion object {
        fun createPopup(
            project: Project?,
            model: ImportBeanFieldModel,
            provider: ChooseByNameItemProvider,
            predefinedText: String?,
            mayRequestOpenInCurrentWindow: Boolean,
            initialIndex: Int
        ): ChooseByNamePopup {
            val oldPopup = project?.getUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY)
            oldPopup?.close(false)
            val newPopup = SearchPopup(
                project,
                model,
                provider,
                oldPopup,
                predefinedText,
                mayRequestOpenInCurrentWindow,
                initialIndex
            )
            project?.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, newPopup)
            return newPopup
        }
    }
}
