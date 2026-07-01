package com.guardianova.child.ui.pairing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.guardianova.child.core.network.ApiClient
import com.guardianova.child.core.network.PairDeviceRequest
import com.guardianova.child.core.network.PairingApiService
import com.guardianova.child.core.storage.EncryptedStorage
import com.guardianova.child.ui.MainActivity
import kotlinx.coroutines.launch

class PairingActivity : ComponentActivity() {

    private lateinit var storage: EncryptedStorage
    private lateinit var apiService: PairingApiService

    // طلب صلاحية الكاميرا لمسح QR
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, "صلاحية الكاميرا مطلوبة لمسح QR", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        storage = EncryptedStorage(this)
        apiService = ApiClient.build(this).create(PairingApiService::class.java)

        // إذا كان الجهاز مرتبطاً مسبقاً، انتقل مباشرة للرئيسية
        if (storage.isPairingComplete()) {
            navigateToMain()
            return
        }

        // طلب صلاحية الكاميرا إن لم تكن ممنوحة
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        setContent {
            MaterialTheme {
                PairingScreen(
                    onPairWithCode = { code -> pairWithCode(code) }
                )
            }
        }
    }

    private fun pairWithCode(code: String) {
        val qrPayload = "pairing:$code"
        val deviceModel = Build.MANUFACTURER + " " + Build.MODEL

        lifecycleScope.launch {
            try {
                val response = apiService.pairDevice(
                    PairDeviceRequest(
                        qrPayload = qrPayload,
                        platform = "android",
                        deviceModel = deviceModel,
                        publicKey = "placeholder_key"
                    )
                )

                storage.saveDeviceId(response.id)
                storage.savePairingComplete(true)

                Toast.makeText(
                    this@PairingActivity,
                    "تم ربط الجهاز بنجاح",
                    Toast.LENGTH_SHORT
                ).show()

                navigateToMain()

            } catch (e: Exception) {
                Toast.makeText(
                    this@PairingActivity,
                    "فشل الربط: تأكد من الكود أو الاتصال",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ─── Composable UI ────────────────────────────────────────────────────────────

@Composable
fun PairingScreen(onPairWithCode: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ربط الجهاز",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "أدخل الكود المكوّن من 6 أرقام من تطبيق ولي الأمر",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6) code = it },
            label = { Text("كود الربط") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        FilledTonalButton(
            onClick = {
                if (code.length == 6) {
                    isLoading = true
                    onPairWithCode(code)
                }
            },
            enabled = code.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("ربط الجهاز")
            }
        }
    }
}
