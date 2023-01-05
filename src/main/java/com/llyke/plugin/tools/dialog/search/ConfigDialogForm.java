package com.llyke.plugin.tools.dialog.search;

import com.intellij.openapi.diagnostic.Logger;
import com.llyke.plugin.tools.setting.IBFSetting;

import javax.swing.*;

/**
 * @author lw
 * @date 2022/12/27 17:32
 */
public class ConfigDialogForm {

    private static final Logger LOG = Logger.getInstance(ConfigDialogForm.class);

    public JPanel mainPanel;

    public JRadioButton fieldInjectRadioButton;
    public JRadioButton constructionInjectRadioButton;


    public JPanel fieldInjectPanel;
    public JRadioButton autowiredFieldInjectRadioButton;
    public JRadioButton resourceJavaXFieldInjectRadioButton;
    public JRadioButton resourceJakartaFieldInjectRadioButton;
    public JCheckBox insertFieldNameOnCursor;
    public ButtonGroup injectModeGroup;
    public ButtonGroup fieldInjectGroup;

    public ConfigDialogForm() {
        IBFSetting ibfSetting = IBFSetting.getInstance();

        insertFieldNameOnCursor.setSelected(Boolean.TRUE.equals(ibfSetting.getInsertFieldNameOnCursor()));
        Integer injectMode = ibfSetting.getInjectMode();
        if (injectMode != null) {
            switch (injectMode) {
                case 0:
                    fieldInjectRadioButton.setSelected(true);
                    break;
                case 1:
                    constructionInjectRadioButton.setSelected(true);
                    break;
                default:
                    LOG.warn("未知的注入方式");
            }
        }
        Integer fieldInjectMode = ibfSetting.getFieldInjectMode();
        if (fieldInjectMode != null) {
            switch (fieldInjectMode) {
                case 0:
                    autowiredFieldInjectRadioButton.setSelected(true);
                    break;
                case 1:
                    resourceJavaXFieldInjectRadioButton.setSelected(true);
                    break;
                case 2:
                    resourceJakartaFieldInjectRadioButton.setSelected(true);
                    break;
                default:
                    LOG.warn("未知的字段注入方式");
            }
        }

        fieldInjectPanel.setVisible(fieldInjectRadioButton.isSelected());
        fieldInjectRadioButton.addChangeListener(e -> {
            fieldInjectPanel.setVisible(fieldInjectRadioButton.isSelected());
        });
    }
}
