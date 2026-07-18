package com.factlens

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.content.Context
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.factlens.capture.ScreenCaptureManager
import com.factlens.capture.ScreenCaptureService
import com.factlens.history.HistoryDatabase
import com.factlens.model.Source
import com.factlens.overlay.OverlayService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.factlens.ui.screens.*
import com.factlens.ui.theme.FactLensColors
import com.factlens.ui.theme.FactLensTheme
import com.factlens.ui.theme.Spacing

private const val TAG = "FactLens.MainActivity"

class MainActivity : ComponentActivity() {

    lateinit var overlayPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<String>
    lateinit var mediaProjectionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    var hasScreenRecording by mutableStateOf(ScreenCaptureManager.hasProjection())
        private set

    var triggerCaptureRequested by mutableStateOf(false)
    var navigateToScanResult by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        if (intent?.getBooleanExtra("trigger_capture", false) == true) {
            Log.d(TAG, "Trigger capture requested from intent")
            triggerCaptureRequested = true
        }

        if (intent?.getBooleanExtra("open_scan_result", false) == true) {
            navigateToScanResult = true
            Log.d(TAG, "Navigate to scan result requested from intent")
        }

        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }

        notificationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { }

        mediaProjectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                Log.d(TAG, "Screen projection permission GRANTED")
                ScreenCaptureManager.setProjectionResult(result.resultCode, result.data)
                hasScreenRecording = true
                Log.d(TAG, "Starting ScreenCaptureService...")
                val serviceIntent = Intent(this, ScreenCaptureService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            } else {
                Log.w(TAG, "Screen projection permission DENIED")
                Toast.makeText(this, "Screen recording permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Overlay permission already granted, starting OverlayService")
            val intent = Intent(this, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        setContent {
            FactLensTheme {
                MainApp(this)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent?.getBooleanExtra("trigger_capture", false) == true) {
            Log.d(TAG, "onNewIntent: trigger_capture requested")
            triggerCaptureRequested = true
        }
        if (intent?.getBooleanExtra("open_scan_result", false) == true) {
            Log.d(TAG, "onNewIntent: navigate to scan result")
            navigateToScanResult = true
        }
    }
}

data class ScanResultData(
    val verdict: String,
    val confidence: Double,
    val explanation: String,
    val sources: List<com.factlens.model.Source>
)

@Composable
fun MainApp(activity: MainActivity) {
    var currentScreen by remember { mutableStateOf(if (Settings.canDrawOverlays(activity)) "home" else "setup") }
    var hasOverlayPermission by remember {
        mutableStateOf(Settings.canDrawOverlays(activity))
    }
    var isServiceRunning by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<ScanResultData?>(null) }

    LaunchedEffect(activity.triggerCaptureRequested) {
        if (activity.triggerCaptureRequested) {
            activity.triggerCaptureRequested = false
            val manager = activity.getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as? MediaProjectionManager ?: return@LaunchedEffect
            activity.mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
        }
    }

    LaunchedEffect(activity.navigateToScanResult) {
        if (activity.navigateToScanResult) {
            activity.navigateToScanResult = false
            val gson = Gson()
            val type = object : TypeToken<List<Source>>() {}.type
            val intent = activity.intent
            val fromRoom = intent?.hasExtra("sources") != true
            if (fromRoom) {
                val dao = HistoryDatabase.getInstance(activity).historyDao()
                val latest = dao.getLatest()
                if (latest != null) {
                    val sources: List<Source> = gson.fromJson(latest.sourcesJson, type) ?: emptyList()
                    scanResult = ScanResultData(
                        verdict = latest.verdict,
                        confidence = latest.confidence,
                        explanation = latest.explanation,
                        sources = sources
                    )
                    currentScreen = "scan_result"
                }
            } else {
                val sources: List<Source> = gson.fromJson(intent.getStringExtra("sources"), type) ?: emptyList()
                scanResult = ScanResultData(
                    verdict = intent.getStringExtra("verdict") ?: "",
                    confidence = intent.getDoubleExtra("confidence", 0.0),
                    explanation = intent.getStringExtra("explanation") ?: "",
                    sources = sources
                )
                currentScreen = "scan_result"
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "setup" -> {
                SetupScreen(
                    hasOverlayPermission = hasOverlayPermission,
                    hasScreenRecording = activity.hasScreenRecording,
                    isServiceRunning = isServiceRunning,
                    onRequestOverlay = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${activity.packageName}")
                        )
                        activity.overlayPermissionLauncher.launch(intent)
                    },
                    onRequestScreenRecording = {
                        val manager = activity.getSystemService(
                            Context.MEDIA_PROJECTION_SERVICE
                        ) as MediaProjectionManager
                        activity.mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
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
                            onQuickScan = {
                                scanResult = null
                                currentScreen = "scanning"
                            },
                            onScanResult = { currentScreen = "scan_result" },
                            onViewAllHistory = { currentScreen = "history" }
                        )
                    }
                )
            }
            "scanning" -> {
                ScanningScreen(
                    onScanComplete = {
                        scanResult = ScanResultData(
                            verdict = "Supported",
                            confidence = 0.87,
                            explanation = "The claim aligns with multiple credible sources including recent scientific publications and international health organization data.",
                            sources = listOf(
                                com.factlens.model.Source(
                                    "Oxford University Study 2026",
                                    "https://example.com/oxford-study",
                                    "Research findings confirm the validity of this claim."
                                ),
                                com.factlens.model.Source(
                                    "WHO Global Health Report",
                                    "https://example.com/who-report",
                                    "International health organization data corroborates the main assertions."
                                ),
                                com.factlens.model.Source(
                                    "Nature Scientific Review",
                                    "https://example.com/nature-review",
                                    "Comprehensive meta-analysis supports the factual basis."
                                )
                            )
                        )
                        currentScreen = "scan_result"
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
            "saved" -> {
                MainScaffold(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    content = {
                        SavedScreen(
                            onScanResult = { currentScreen = "scan_result" }
                        )
                    }
                )
            }
            "settings" -> {
                MainScaffold(
                    currentScreen = currentScreen,
                    onNavigate = { currentScreen = it },
                    content = {
                        SettingsScreen(
                            hasOverlayPermission = hasOverlayPermission,
                            onRequestOverlay = {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${activity.packageName}")
                                )
                                activity.overlayPermissionLauncher.launch(intent)
                            }
                        )
                    }
                )
            }
            "scan_result" -> {
                scanResult?.let { result ->
                    ScanResultScreen(
                        scanResult = result,
                        onBack = {
                            currentScreen = "home"
                            scanResult = null
                        },
                        onOpenSource = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                activity.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                } ?: run { }
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
                BottomNavItem(
                    icon = Icons.Filled.Home,
                    label = "Home",
                    selected = currentScreen == "home",
                    onClick = { onNavigate("home") }
                )
                BottomNavItem(
                    icon = Icons.Filled.History,
                    label = "History",
                    selected = currentScreen == "history",
                    onClick = { onNavigate("history") }
                )
                BottomNavItem(
                    icon = Icons.Filled.Bookmark,
                    label = "Saved",
                    selected = currentScreen == "saved",
                    onClick = { onNavigate("saved") }
                )
                BottomNavItem(
                    icon = Icons.Filled.Settings,
                    label = "Settings",
                    selected = currentScreen == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
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
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = fg
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
    hasScreenRecording: Boolean,
    isServiceRunning: Boolean,
    onRequestOverlay: () -> Unit,
    onRequestScreenRecording: () -> Unit,
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
                    title = "Screen Recording",
                    desc = "Allow FactLens to capture your screen",
                    done = hasScreenRecording,
                    onAction = onRequestScreenRecording
                )
                Spacer(Modifier.height(Spacing.md))
                SetupStep(
                    number = "3",
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
                if (done) "\u2713" else number,
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