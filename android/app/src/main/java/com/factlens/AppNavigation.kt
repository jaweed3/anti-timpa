package com.factlens

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.factlens.capture.ScreenCaptureManager
import com.factlens.network.VerdictEngine
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import com.factlens.history.HistoryDatabase
import com.factlens.model.ScanHistory
import com.factlens.model.ScanResultData
import com.factlens.model.Source
import com.factlens.overlay.OverlayService
import com.factlens.overlay.OverlayService.Companion.toggleOverlayCallback
import com.factlens.ui.components.MainScaffold
import com.factlens.ui.screens.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun AppNavigation(activity: MainActivity) {
    var currentScreen by remember { mutableStateOf(if (Settings.canDrawOverlays(activity)) "home" else "setup") }
    var hasOverlayPermission by remember { mutableStateOf(Settings.canDrawOverlays(activity)) }
    var isServiceRunning by remember { mutableStateOf(false) }
    var overlayVisible by remember {
        mutableStateOf(
            activity.getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
                .getBoolean("overlay_visible", true)
        )
    }
    var scanResult by remember { mutableStateOf<ScanResultData?>(null) }
    var selectedHistoryItem by remember { mutableStateOf<ScanHistory?>(null) }
    var scanResultHistoryId by remember { mutableStateOf<Long?>(null) }
    var previousScreen by remember { mutableStateOf("home") }
    var backendUrl by remember {
        mutableStateOf(
            activity.getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
                .getString("backend_url", VerdictEngine.backendUrl) ?: VerdictEngine.backendUrl
        )
    }

    LaunchedEffect(Unit) {
        val savedUrl = activity.getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
            .getString("backend_url", null)
        if (savedUrl != null) {
            VerdictEngine.backendUrl = savedUrl
        }
    }

    LaunchedEffect(activity.navigateToHistoryDetail) {
        if (activity.navigateToHistoryDetail) {
            currentScreen = "history"
            activity.navigateToHistoryDetail = false
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
                    scanResultHistoryId = latest.id
                    scanResult = ScanResultData(latest.claim, latest.verdict, latest.confidence, latest.explanation, sources)
                    currentScreen = "scan_result"
                }
            } else {
                val sources: List<Source> = gson.fromJson(intent.getStringExtra("sources"), type) ?: emptyList()
                scanResultHistoryId = null
                scanResult = ScanResultData(
                    intent.getStringExtra("claim") ?: "",
                    intent.getStringExtra("verdict") ?: "",
                    intent.getDoubleExtra("confidence", 0.0),
                    intent.getStringExtra("explanation") ?: "",
                    sources
                )
                currentScreen = "scan_result"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            "setup" -> SetupRoute(activity, hasOverlayPermission, activity.hasScreenRecording, isServiceRunning,
                onOverlayPermChanged = { hasOverlayPermission = it },
                onServiceStarted = { isServiceRunning = true; currentScreen = "home" },
                onPermissionError = { Toast.makeText(activity, "Grant overlay permission first", Toast.LENGTH_SHORT).show() }
            )
            "home" -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) {
                HomeScreen(
                    onQuickScan = { scanResult = null; currentScreen = "scanning" },
                    onScanResult = { currentScreen = "scan_result" },
                    onViewAllHistory = { currentScreen = "history" },
                    onViewAllSaved = { currentScreen = "saved" },
                    onHistoryItemClick = { item -> selectedHistoryItem = item; previousScreen = "home"; currentScreen = "history_detail" }
                )
            }
            "scanning" -> ScanningScreen(
                onScanComplete = {
                    scanResultHistoryId = null
                    scanResult = ScanResultData(
                        claim = "\"Studies show that drinking 5 liters of water a day reverses aging by 20 years within a week.\"",
                        verdict = "Supported", confidence = 0.87,
                        explanation = "The claim aligns with multiple credible sources including recent scientific publications and international health organization data.",
                        sources = listOf(
                            Source("Oxford University Study 2026", "https://example.com/oxford-study", "Research findings confirm the validity of this claim."),
                            Source("WHO Global Health Report", "https://example.com/who-report", "International health organization data corroborates the main assertions."),
                            Source("Nature Scientific Review", "https://example.com/nature-review", "Comprehensive meta-analysis supports the factual basis.")
                        )
                    )
                    currentScreen = "scan_result"
                }
            )
            "history" -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) {
                HistoryScreen(
                    onScanResult = { selectedHistoryItem = null; currentScreen = "scan_result" },
                    onHistoryItemClick = { item -> selectedHistoryItem = item; previousScreen = "history"; currentScreen = "history_detail" }
                )
            }
            "saved" -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) {
                SavedScreen(
                    onScanResult = { currentScreen = "scan_result" },
                    onHistoryItemClick = { item -> selectedHistoryItem = item; previousScreen = "saved"; currentScreen = "history_detail" }
                )
            }
            "settings" -> MainScaffold(currentScreen, onNavigate = { currentScreen = it }) {
                SettingsScreen(
                    hasOverlayPermission = hasOverlayPermission,
                    onRequestOverlay = {
                        activity.overlayPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${activity.packageName}")))
                    },
                    overlayVisible = overlayVisible,
                    onToggleOverlay = { visible ->
                        overlayVisible = visible
                        activity.getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
                            .edit().putBoolean("overlay_visible", visible).apply()
                        toggleOverlayCallback?.invoke(visible)
                    },
                    backendUrl = backendUrl,
                    onSaveBackendUrl = { newUrl ->
                        backendUrl = newUrl
                        VerdictEngine.backendUrl = newUrl
                        activity.getSharedPreferences("factlens_prefs", Context.MODE_PRIVATE)
                            .edit().putString("backend_url", newUrl).apply()
                    }
                )
            }
            "scan_result" -> scanResult?.let { result ->
                ScanResultScreen(result,
                    onBack = { currentScreen = "home"; scanResult = null; scanResultHistoryId = null },
                    onOpenSource = { url -> try { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {} },
                    onSave = {
                        val id = scanResultHistoryId
                        if (id != null) {
                            kotlinx.coroutines.MainScope().launch {
                                val dao = HistoryDatabase.getInstance(activity).historyDao()
                                val item = dao.getById(id)
                                if (item != null) dao.toggleFavorite(id, !item.isFavorite)
                            }
                        }
                    },
                    onShare = {
                        val text = "AntiTimpa Verification:\n\"${result.claim}\"\nVerdict: ${result.verdict}\nConfidence: ${(result.confidence * 100).toInt()}%\n${result.explanation}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            this.type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        activity.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                )
            } ?: run {}
            "history_detail" -> selectedHistoryItem?.let { item ->
                val gson = Gson()
                val typeToken = object : TypeToken<List<Source>>() {}.type
                val sources: List<Source> = gson.fromJson(item.sourcesJson, typeToken) ?: emptyList()
                ScanResultScreen(ScanResultData(item.claim, item.verdict, item.confidence, item.explanation, sources),
                    onBack = { currentScreen = previousScreen; selectedHistoryItem = null },
                    onOpenSource = { url -> try { activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {} },
                    onSave = {
                        kotlinx.coroutines.MainScope().launch {
                            HistoryDatabase.getInstance(activity).historyDao().toggleFavorite(item.id, !item.isFavorite)
                            selectedHistoryItem = item.copy(isFavorite = !item.isFavorite)
                        }
                    },
                    onShare = {
                        val text = "AntiTimpa Verification:\n\"${item.claim}\"\nVerdict: ${item.verdict}\nConfidence: ${(item.confidence * 100).toInt()}%\n${item.explanation}"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            this.type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        activity.startActivity(Intent.createChooser(shareIntent, "Share via"))
                    }
                )
            } ?: run { currentScreen = "history" }
        }
    }
}

@Composable
private fun SetupRoute(
    activity: MainActivity,
    hasOverlayPermission: Boolean,
    hasScreenRecording: Boolean,
    isServiceRunning: Boolean,
    onOverlayPermChanged: (Boolean) -> Unit,
    onServiceStarted: () -> Unit,
    onPermissionError: () -> Unit
) {
    SetupScreen(
        hasOverlayPermission = hasOverlayPermission,
        hasScreenRecording = hasScreenRecording,
        isServiceRunning = isServiceRunning,
        onRequestOverlay = {
            activity.overlayPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${activity.packageName}")))
        },
        onRequestScreenRecording = {
            val manager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            activity.mediaProjectionLauncher.launch(manager.createScreenCaptureIntent())
        },
        onStartService = {
            if (Settings.canDrawOverlays(activity)) {
                val intent = Intent(activity, OverlayService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent)
                } else { activity.startService(intent) }
                onServiceStarted()
            } else { onPermissionError() }
        }
    )
}
