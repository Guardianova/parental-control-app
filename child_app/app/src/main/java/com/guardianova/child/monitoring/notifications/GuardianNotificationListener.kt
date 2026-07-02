package com.guardianova.child.monitoring.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class GuardianNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        // سيُضاف لاحقاً في المرحلة د
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        // سيُضاف لاحقاً في المرحلة د
    }
}
