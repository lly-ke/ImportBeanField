package com.llyke.plugin.tools.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

/**
 * @author lw
 * @date 2022/12/5 17:11
 */
class IBFNotifier {

    companion object {


        fun notifyError(
            project: Project?,
            content: String
        ) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(IBFConstant.NOTIFICATION_GROUP_ID)
                .createNotification(content, NotificationType.ERROR)
                .notify(project)
        }

        fun notifyWarning(
            project: Project?,
            content: String
        ) {
            NotificationGroupManager.getInstance()
                .getNotificationGroup(IBFConstant.NOTIFICATION_GROUP_ID)
                .createNotification(content, NotificationType.WARNING)
                .notify(project)
        }
    }

}