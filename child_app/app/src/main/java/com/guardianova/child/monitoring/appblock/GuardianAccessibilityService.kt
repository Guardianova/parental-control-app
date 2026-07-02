package com.guardianova.child.monitoring.appblock

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class GuardianAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // سيُضاف لاحقاً في المرحلة د (حظر التطبيقات)
    }

    override fun onInterrupt() {
        // مطلوب تنفيذه
    }
}
