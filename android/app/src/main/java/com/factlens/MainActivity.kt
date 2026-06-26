package com.factlens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.factlens.capture.ScreenCaptureManager
import com.factlens.overlay.OverlayService

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* result ignored, we check permission manually */ }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not */ }

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            ScreenCaptureManager.setProjectionResult(result.resultCode, result.data)
            ScreenCaptureManager(this).startCaptureService(this)
        } else {
            Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notifications permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FactLensTheme()
        }
    }

    @Composable
    fun FactLensTheme() {
        val bgColor = Color(0xFF0D0D1A)
        val surfaceColor = Color(0xFF1A1A2E)
        val primaryColor = Color(0xFF6C63FF)
        val textColor = Color.White
        val secondaryText = Color(0xFFB0B0B0)

        var hasOverlayPermission by remember {
            mutableStateOf(Settings.canDrawOverlays(this@MainActivity))
        }

        var hasCapturePermission by remember {
            mutableStateOf(false)
        }

        var isServiceRunning by remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // Logo area
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(primaryColor, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("F", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("FactLens", color = primaryColor, fontSize = 32.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Verify information directly from your screen",
                color = secondaryText,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Permission steps
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Setup",
                        color = primaryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PermissionStep(
                        number = 1,
                        title = "Overlay Permission",
                        description = "Allow FactLens to show the floating button",
                        isGranted = hasOverlayPermission,
                        onRequest = {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:$packageName")
                            )
                            overlayPermissionLauncher.launch(intent)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    PermissionStep(
                        number = 2,
                        title = "Start Overlay",
                        description = "Launch the floating verification button",
                        isGranted = isServiceRunning,
                        onRequest = {
                            if (Settings.canDrawOverlays(this@MainActivity)) {
                                val intent = Intent(this@MainActivity, OverlayService::class.java)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(intent)
                                } else {
                                    startService(intent)
                                }
                                isServiceRunning = true
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Please grant overlay permission first",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Instructions
            Text(
                "Tap the floating button \"F\" on any screen\nto verify information instantly",
                color = secondaryText,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    @Composable
    fun PermissionStep(
        number: Int,
        title: String,
        description: String,
        isGranted: Boolean,
        onRequest: () -> Unit
    ) {
        val primaryColor = Color(0xFF6C63FF)
        val textColor = Color.White
        val secondaryText = Color(0xFFB0B0B0)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (isGranted) Color(0xFF4CAF50) else Color(0xFF2A2A3E),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (isGranted) "✓" else "$number",
                    color = if (isGranted) Color.White else secondaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Text(description, color = secondaryText, fontSize = 12.sp)
            }

            if (!isGranted) {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Allow", fontSize = 12.sp)
                }
            }
        }
    }
}
