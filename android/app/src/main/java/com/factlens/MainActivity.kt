package com.factlens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.factlens.capture.ScreenCaptureManager
import com.factlens.overlay.OverlayService
import com.factlens.ui.screens.HistoryScreen
import com.factlens.ui.screens.HomeScreen
import com.factlens.ui.screens.ScanResultScreen
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.FactLensTheme
import com.factlens.ui.theme.Spacing

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { /* check manually */ }

    private val notificationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { /* granted or not */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            FactLensTheme {
                MainApp(this)
            }
        }
    }
}

@Composable
fun MainApp(activity: MainActivity) {
    var currentScreen by remember { mutableStateOf("home") }
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(activity))
    }
    var isServiceRunning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "setup" -> {
                SetupScreen(
                    hasOverlayPermission = hasOverlayPermission,
                    isServiceRunning = isServiceRunning,
                    onRequestOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")
                        )
                        activity.overlayPermissionLauncher.launch(intent)
                    },
                    onStartService = {
                        if (Settings.canDrawOverlays(activity)) {
                            val intent = Intent(activity, OverlayService::class.java)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                activity.startForegroundService(intent)
                            } else {
                                activity.startService(intent)
                            }
                            isServiceRunning = true
                            currentScreen = "home"
                        } else {
                            Toast.makeText(activity, "Grant overlay permission first", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            "home" -> {
                MainScaffold(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    content = {
                        HomeScreen(
                            onQuickScan = { currentScreen = "scan_result" },
                            onScanResult = { currentScreen = "scan_result" },
                            onViewAllHistory = { currentScreen = "history" }
                        )
                    }
                )
            }
            "history" -> {
                MainScaffold(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    content = {
                        HistoryScreen(
                            onScanResult = { currentScreen = "scan_result" }
                        )
                    }
                )
            }
            "scan_result" -> {
                ScanResultScreen(
                    onBack = { currentScreen = "home" },
                    onOpenSource = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            activity.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                )
            }
        }
    }
}

@Composable
fun MainScaffold(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        // Bottom Nav Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FactLensColors.surfaceContainerLowest,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavItem(
                    icon = "home",
                    label = "Home",
                    selected = currentScreen == "home",
                    onClick = { onNavigate("home") }
                )
                NavItem(
                    icon = "history",
                    label = "History",
                    selected = currentScreen == "history",
                    onClick = { onNavigate("history") }
                )
                NavItem(
                    icon = "bookmark",
                    label = "Saved",
                    selected = false,
                    onClick = {}
                )
                NavItem(
                    icon = "settings",
                    label = "Settings",
                    selected = false,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun NavItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) FactLensColors.secondaryContainer else Color.Transparent
    val fg = if (selected) FactLensColors.onSecondaryContainer else FactLensColors.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            icon,
            fontSize = 22.sp,
            color = fg
        )
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg
        )
    }
}

@Composable
fun SetupScreen(
    hasOverlayPermission: Boolean,
    isServiceRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onStartService: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FactLensColors.background)
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(80.dp))

        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(FactLensColors.primary),
            contentAlignment = Alignment.Center
        ) {
            Text("F", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(Spacing.lg))

        Text("FactLens", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
        Spacer(Modifier.height(Spacing.xs))
        Text(
            "Verify information directly from your screen",
            fontSize = 14.sp,
            color = FactLensColors.neutralGray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(48.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = FactLensColors.surfacePureWhite)
        ) {
            Column(modifier = Modifier.padding(Spacing.xl)) {
                Text("Setup", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = FactLensColors.primary)
                Spacer(Modifier.height(Spacing.lg))

                SetupStep(
                    number = "1",
                    title = "Overlay Permission",
                    desc = "Allow FactLens to show the floating button",
                    done = hasOverlayPermission,
                    onAction = onRequestOverlay
                )
                Spacer(Modifier.height(Spacing.md))
                SetupStep(
                    number = "2",
                    title = "Start Overlay",
                    desc = "Launch the floating verification button",
                    done = isServiceRunning,
                    onAction = onStartService
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            "Tap the floating button on any screen\nto verify information instantly",
            fontSize = 13.sp,
            color = FactLensColors.neutralGray,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.xl))
    }
}

@Composable
fun SetupStep(number: String, title: String, desc: String, done: Boolean, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (done) FactLensColors.successEmerald else FactLensColors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (done) "✓" else number,
                color = if (done) Color.White else FactLensColors.neutralGray,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(Spacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = FactLensColors.onSurface)
            Text(desc, fontSize = 12.sp, color = FactLensColors.neutralGray)
        }
        if (!done) {
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FactLensColors.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text("Allow", fontSize = 12.sp)
            }
        }
    }
}
