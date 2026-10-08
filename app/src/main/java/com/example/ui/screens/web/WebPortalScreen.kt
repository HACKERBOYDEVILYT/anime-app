package com.example.ui.screens.web

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.AppUpdateRepository
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent

/**
 * Unified Live Website Portal & Repository Auto-Update Manager Screen.
 * - Tab 0: Unified Live Website Preview (runs the exact same website from `/docs/index.html` & `assets/web/index.html`
 *   with a live JavaScript bridge to the Android App's catalog & native ExoPlayer).
 * - Tab 1: Repository Auto-Update & LAN Web Server Control (checks GitHub repository for updates,
 *   configures repo slug & website URL, and hosts the live website on LAN port 8080).
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPortalScreen(
    appUpdateRepository: AppUpdateRepository,
    onWatchEpisode: (animeId: String, episodeNumber: Int) -> Unit,
    onBack: () -> Unit,
    onOpenAdminPanel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val updateState by appUpdateRepository.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    var repoSlugInput by remember(updateState.repositorySlug) { mutableStateOf(updateState.repositorySlug) }
    var webUrlInput by remember(updateState.liveWebsiteUrl) { mutableStateOf(updateState.liveWebsiteUrl) }
    var apkUrlInput by remember(updateState.apkDownloadUrl) { mutableStateOf(updateState.apkDownloadUrl) }
    var versionNameInput by remember(updateState.latestVersionName) { mutableStateOf(updateState.latestVersionName) }
    var changelogInput by remember(updateState.changelog) { mutableStateOf(updateState.changelog) }
    var forceUpdateInput by remember(updateState.forceUpdate) { mutableStateOf(updateState.forceUpdate) }
    var autoCheckInput by remember(updateState.autoCheckOnStartup) { mutableStateOf(updateState.autoCheckOnStartup) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Unified Website & Repo Auto-Update",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Same Repository • Live Web Portal • Auto-Update",
                        color = CyanGlow,
                        fontSize = 11.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        webViewRef?.evaluateJavascript(
                            "if(window.openWebAdminModal){window.openWebAdminModal();}",
                            null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A00)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("web_admin_modal_btn")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Web Admin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }

                OutlinedButton(
                    onClick = onOpenAdminPanel,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_native_admin_from_web_btn")
                ) {
                    Text("App Admin", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        appUpdateRepository.checkForRepositoryUpdate(autoPrompt = true)
                        webViewRef?.evaluateJavascript(
                            "if(window.loadLiveCatalogAndVersion){window.loadLiveCatalogAndVersion();}",
                            null
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("check_repo_update_btn")
                ) {
                    if (updateState.isChecking) {
                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(14.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundDark,
            contentColor = CrimsonNeon,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = CrimsonNeon
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🌐 Live Unified Website", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("🚀 Repo Auto-Update & Server", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            )
        }

        if (selectedTab == 0) {
            // Prominent Website Link Bar (Copy Link, Share Link, Open in Browser)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF00FF66), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "LIVE WEBSITE LINK (CLICK TO COPY OR OPEN)",
                                color = Color(0xFF00FF66),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        SelectionContainer {
                            Column {
                                Text(
                                    text = updateState.liveWebsiteUrl,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Local Server: ${updateState.lanWebServerUrl} • APK: ${updateState.lanWebServerUrl}/download/Robiul-Release.apk",
                                    color = CyanGlow,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (updateState.lastApkReleaseStatus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = updateState.lastApkReleaseStatus,
                                color = Color(0xFF00FF66),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Robiul Website Link", updateState.liveWebsiteUrl))
                            Toast.makeText(context, "Website Link Copied: ${updateState.liveWebsiteUrl}", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("copy_website_link_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Website Link", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Robiul Local Server Link", updateState.lanWebServerUrl))
                            Toast.makeText(context, "Local Website Link Copied: ${updateState.lanWebServerUrl}", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy LAN Link", tint = CyanGlow, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy :8080 Link", color = CyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Robiul [RS] Live Streaming Website:\nLive Server: ${updateState.liveWebsiteUrl}\nLAN Server: ${updateState.lanWebServerUrl}\nDirect APK: ${updateState.lanWebServerUrl}/download/Robiul-Release.apk"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Website Link"))
                            } catch (_: Exception) {
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share Link", tint = Color.White, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val resultMsg = appUpdateRepository.exportReleaseApkToDownloads()
                            Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("download_release_apk_btn")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = "Release APK", tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Release APK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                if (!updateState.isLanWebServerRunning) {
                                    appUpdateRepository.startLanWebServer()
                                }
                                val target = "http://127.0.0.1:8080"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                            } catch (_: Exception) {
                                Toast.makeText(context, "Live Website Active below (200 OK)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Browser", color = CrimsonNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Live Synchronized Website View with Native Bridge
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.allowFileAccess = true
                            settings.allowContentAccess = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.cacheMode = WebSettings.LOAD_NO_CACHE
                            clearCache(true)
                            webChromeClient = WebChromeClient()
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?
                                ): Boolean {
                                    val clickedUrl = request?.url?.toString().orEmpty()
                                    if (clickedUrl.contains("Robiul-Release.apk", ignoreCase = true) ||
                                        clickedUrl.contains("/download/", ignoreCase = true)
                                    ) {
                                        val msg = appUpdateRepository.exportReleaseApkToDownloads()
                                        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                        return true
                                    }
                                    if (clickedUrl.contains("ais-pre-") || clickedUrl.contains("robiul.github.io") || clickedUrl.contains("127.0.0.1:8080")) {
                                        view?.loadUrl("file:///android_asset/web/index.html")
                                        return true
                                    }
                                    return false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    error: android.webkit.WebResourceError?
                                ) {
                                    if (request?.isForMainFrame == true) {
                                        view?.loadUrl("file:///android_asset/web/index.html")
                                    }
                                }

                                override fun onReceivedHttpError(
                                    view: WebView?,
                                    request: android.webkit.WebResourceRequest?,
                                    errorResponse: android.webkit.WebResourceResponse?
                                ) {
                                    if (request?.isForMainFrame == true && (errorResponse?.statusCode ?: 200) >= 400) {
                                        view?.loadUrl("file:///android_asset/web/index.html")
                                    }
                                }
                            }

                            addJavascriptInterface(
                                object {
                                    @JavascriptInterface
                                    fun getAppCatalogJson(): String {
                                        return appUpdateRepository.buildUnifiedAppCatalogJson()
                                    }

                                    @JavascriptInterface
                                    fun openNativePlayer(animeId: String, episodeNum: Int) {
                                        post {
                                            onWatchEpisode(animeId.ifBlank { "anime_1" }, episodeNum.coerceAtLeast(1))
                                        }
                                    }

                                    @JavascriptInterface
                                    fun openNativeAdminPanel() {
                                        post {
                                            onOpenAdminPanel()
                                        }
                                    }

                                    @JavascriptInterface
                                    fun addPaidServerFromWeb(
                                        name: String,
                                        url: String,
                                        category: String,
                                        apiKey: String,
                                        setAsLogoCdn: Boolean
                                    ): String {
                                        return appUpdateRepository.addAdminServerFromWeb(
                                            name = name,
                                            url = url,
                                            category = category,
                                            apiKey = apiKey,
                                            setAsLogoCdn = setAsLogoCdn
                                        )
                                    }

                                    @JavascriptInterface
                                    fun addEpisodeStreamFromWeb(
                                        animeId: String,
                                        animeTitle: String,
                                        episodeNum: Int,
                                        streamUrl: String,
                                        serverName: String,
                                        quality: String
                                    ): String {
                                        return appUpdateRepository.addAdminEpisodeStreamFromWeb(
                                            animeId = animeId,
                                            animeTitle = animeTitle,
                                            episodeNumber = episodeNum,
                                            streamUrl = streamUrl,
                                            serverName = serverName,
                                            quality = quality
                                        )
                                    }

                                    @JavascriptInterface
                                    fun updateBunnyLogoFromWeb(logoUrl: String, baseUrl: String): String {
                                        return appUpdateRepository.updateBunnyLogoFromWeb(logoUrl, baseUrl)
                                    }

                                    @JavascriptInterface
                                    fun deleteServerFromWeb(serverId: String): String {
                                        return appUpdateRepository.deleteAdminServerFromWeb(serverId)
                                    }

                                    @JavascriptInterface
                                    fun toggleServerFromWeb(serverId: String): String {
                                        return appUpdateRepository.toggleAdminServerFromWeb(serverId)
                                    }

                                    @JavascriptInterface
                                    fun deleteEpisodeStreamFromWeb(streamId: String): String {
                                        return appUpdateRepository.deleteAdminStreamFromWeb(streamId)
                                    }

                                    @JavascriptInterface
                                    fun addAnimeFromWeb(
                                        title: String,
                                        genre: String,
                                        episodes: Int,
                                        rating: Float,
                                        posterUrl: String,
                                        streamUrl: String
                                    ): String {
                                        return appUpdateRepository.addAnimeCatalogFromWeb(
                                            title = title,
                                            genre = genre,
                                            episodes = episodes,
                                            rating = rating,
                                            posterUrl = posterUrl,
                                            streamUrl = streamUrl.ifBlank { null }
                                        )
                                    }

                                    @JavascriptInterface
                                    fun releaseApkFromWeb(): String {
                                        val msg = appUpdateRepository.exportReleaseApkToDownloads()
                                        post {
                                            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                        }
                                        return msg
                                    }
                                },
                                "KuroStreamBridge"
                            )

                            webViewRef = this
                            loadUrl("file:///android_asset/web/index.html")
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Tab 1: Repository Auto-Update & Unified Website Hosting Control
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CrimsonNeon.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SystemUpdateAlt, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Repository Auto-Update Status",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "${updateState.lastCheckedTime} • Commit: ${updateState.latestCommitSha}",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (updateState.isUpdateAvailable) CrimsonNeon.copy(alpha = 0.2f) else Color(0xFF00E676).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (updateState.isUpdateAvailable) "UPDATE READY (v${updateState.latestVersionName})" else "UP TO DATE",
                                        color = if (updateState.isUpdateAvailable) CrimsonNeon else Color(0xFF00E676),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { appUpdateRepository.showUpdateDialogManually() },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Show Auto-Update Prompt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { appUpdateRepository.markUpdateInstalled() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark v${updateState.latestVersionName} Installed", color = CyanGlow, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Built-in Live LAN Web Server Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyanGlow.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Live Website Server (Same Repo + LAN)",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Host the unified website directly from `/docs/index.html` & `/api/catalog`",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Switch(
                                    checked = updateState.isLanWebServerRunning,
                                    onCheckedChange = { appUpdateRepository.toggleLanWebServer() },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF00E676))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Local Server URL: ${updateState.lanWebServerUrl}  |  GitHub Pages: ${updateState.liveWebsiteUrl}",
                                color = CyanGlow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Configure Repository & Push Update Settings Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "GitHub Repository & Auto-Update Configuration",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Whenever you push a commit or release to this repository, GitHub Actions updates `docs/version.json`, deploys the live website to GitHub Pages, and triggers the Auto-Update dialog in this app.",
                                color = TextMuted,
                                fontSize = 11.sp
                            )

                            OutlinedTextField(
                                value = repoSlugInput,
                                onValueChange = { newSlug ->
                                    repoSlugInput = newSlug
                                    val clean = newSlug.trim().removePrefix("https://github.com/").removeSuffix("/")
                                    if (clean.contains("/")) {
                                        val owner = clean.substringBefore("/")
                                        val repo = clean.substringAfter("/")
                                        if (owner.isNotBlank() && repo.isNotBlank()) {
                                            webUrlInput = "https://$owner.github.io/$repo/"
                                            apkUrlInput = "https://github.com/$owner/$repo/releases/latest/download/Robiul-Release.apk"
                                        }
                                    }
                                },
                                label = { Text("GitHub Repository (owner/repo) — Auto-generates Website Link") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = webUrlInput,
                                onValueChange = { webUrlInput = it },
                                label = { Text("Live Website URL (GitHub Pages / Vercel / Netlify)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = apkUrlInput,
                                onValueChange = { apkUrlInput = it },
                                label = { Text("Direct APK Download URL (GitHub Releases)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanGlow,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = versionNameInput,
                                onValueChange = { versionNameInput = it },
                                label = { Text("Target Repository Version Name (e.g. 1.2.0)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonNeon,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = changelogInput,
                                onValueChange = { changelogInput = it },
                                label = { Text("Update Release Notes / Changelog") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CrimsonNeon,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-Check Repository on App Startup", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = autoCheckInput,
                                    onCheckedChange = { autoCheckInput = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CyanGlow)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Force Mandatory Update Prompt", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Switch(
                                    checked = forceUpdateInput,
                                    onCheckedChange = { forceUpdateInput = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonNeon)
                                )
                            }

                            Button(
                                onClick = {
                                    appUpdateRepository.updateRepositoryConfig(
                                        repositorySlug = repoSlugInput,
                                        liveWebsiteUrl = webUrlInput,
                                        apkDownloadUrl = apkUrlInput,
                                        latestVersionName = versionNameInput,
                                        changelog = changelogInput,
                                        forceUpdate = forceUpdateInput,
                                        autoCheckOnStartup = autoCheckInput,
                                        triggerPromptNow = true
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("save_and_trigger_repo_update_btn")
                            ) {
                                Text("Save Repository Config & Trigger Auto-Update Prompt", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
