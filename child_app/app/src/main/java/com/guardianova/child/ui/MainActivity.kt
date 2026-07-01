package com.guardianova.child.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.guardianova.child.core.storage.EncryptedStorage
import com.guardianova.child.service.GuardianService
import com.guardianova.child.ui.pairing.PairingActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = EncryptedStorage(this)

        if (!storage.isPairingComplete()) {
            // لم يتم الربط بعد — انتقل لشاشة الربط
            startActivity(Intent(this, PairingActivity::class.java))
            finish()
            return
        }

        // تم الربط — شغّل الخدمة إن لم تكن تعمل
        GuardianService.start(this)

        // شاشة مؤقتة بسيطة — ستُستبدل بشاشة الإعداد الكاملة لاحقاً
        setContent {
            MaterialTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Guardianova — وضع الحماية مفعّل")
                }
            }
        }
    }
}
