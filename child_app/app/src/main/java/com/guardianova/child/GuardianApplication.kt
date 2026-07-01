package com.guardianova.child

import android.app.Application

class GuardianApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // سيُضاف منطق التهيئة لاحقاً (FCM، WorkManager، إلخ)
    }
}
