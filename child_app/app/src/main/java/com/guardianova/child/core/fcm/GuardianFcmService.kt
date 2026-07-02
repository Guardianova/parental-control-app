package com.guardianova.child.core.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class GuardianFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // سيُضاف لاحقاً: إرسال FCM token للـ Backend
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // سيُضاف لاحقاً: معالجة أوامر ولي الأمر
    }
}
