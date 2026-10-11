package com.example.ui.screens.web

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.model.Anime
import com.example.data.repository.AppUpdateRepository
import com.example.security.AdminSecurityManager
import com.example.ui.components.SecretAdminDialog
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.StarAmber
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

// Tailwind CSS Palette Constants
private val TailwindSlate950 = Color(0xFF020617)
private val TailwindSlate900 = Color(0xFF0F172A)
private val TailwindSlate800 = Color(0xFF1E293B)
private val TailwindSlate400 = Color(0xFF94A3B8)
private val TailwindSky400 = Color(0xFF38BDF8)
private val TailwindIndigo500 = Color(0xFF6366F1)
private val TailwindEmerald400 = Color(0xFF34D399)

private data class RandomHeroStreamItem(
    val animeId: String,
    val title: String,
    val streamUrl: String,
    val badge: String,
    val bannerUrl: String,
    val synopsis: String
)

/**
 * Tailwind-Styled ROBIUL Live Website Portal.
 * - Navigating to Admin Panel or Video Player always opens a dedicated separate full page
 *   (`onOpenAdminPanel()` -> `Screen.Admin.route`, `onWatchEpisode()` -> `Screen.Player.route`)
 *   instead of opening a modal/popup overlay on top of the previous page.
 * - Clean Tailwind CSS typography & spacing with no crowded/squished header text.
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
    val isAdminAuthenticated by AdminSecurityManager.isAdminAuthenticated.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var useRawHtmlWebView by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var secretHeaderTapCount by remember { mutableIntStateOf(0) }
    var lastHeaderTapTime by remember { mutableStateOf(0L) }
    var showStealthAdminDialog by remember { mutableStateOf(false) }
    var showQuickLinkStrip by remember { mutableStateOf(false) }
    var catalogRefreshTick by remember { mutableIntStateOf(0) }

    val fullCatalog = remember(catalogRefreshTick, updateState.lastCheckedTime) {
        appUpdateRepository.getCatalogSnapshot()
    }
    val paidServers = remember(catalogRefreshTick, updateState.lastCheckedTime) {
        appUpdateRepository.getApiConfigsSnapshot()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("All") }

    val randomHeroPool = remember {
        listOf(
            RandomHeroStreamItem(
                animeId = "anime_1",
                title = "Frieren: Beyond Journey's End",
                streamUrl = "https://v.animethemes.moe/SousouNoFrieren-OP1.webm",
                badge = "LIVE • Frieren 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                synopsis = "After a ten-year quest to defeat the Demon King, elven mage Frieren embarks on a new journey to understand humanity."
            ),
            RandomHeroStreamItem(
                animeId = "anime_3",
                title = "Solo Leveling",
                streamUrl = "https://v.animethemes.moe/SoloLeveling-OP1.webm",
                badge = "LIVE • Solo Leveling 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1171/142313l.jpg",
                synopsis = "In a world of awakened hunters and dungeons, E-rank hunter Sung Jinwoo receives the mysterious System to level up infinitely."
            ),
            RandomHeroStreamItem(
                animeId = "anime_2",
                title = "Jujutsu Kaisen Season 2",
                streamUrl = "https://v.animethemes.moe/JujutsuKaisenS2-OP1.webm",
                badge = "LIVE • Jujutsu Kaisen S2 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
                synopsis = "Satoru Gojo and Suguru Geto are assigned a critical mission in the Hidden Inventory arc before the Shibuya Incident erupts."
            ),
            RandomHeroStreamItem(
                animeId = "anime_4",
                title = "Demon Slayer: Kimetsu no Yaiba",
                streamUrl = "https://v.animethemes.moe/KimetsuNoYaiba-OP1.webm",
                badge = "LIVE • Demon Slayer 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg",
                synopsis = "Tanjiro Kamado joins the Demon Slayer Corps to turn his sister Nezuko back into a human and defeat Muzan Kibutsuji."
            ),
            RandomHeroStreamItem(
                animeId = "anime_5",
                title = "Chainsaw Man",
                streamUrl = "https://v.animethemes.moe/ChainsawMan-OP1.webm",
                badge = "LIVE • Chainsaw Man 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1806/126216l.jpg",
                synopsis = "Denji merges with his chainsaw devil pet Pochita and joins Public Safety Devil Hunters."
            ),
            RandomHeroStreamItem(
                animeId = "anime_6",
                title = "Attack on Titan: Final Season",
                streamUrl = "https://v.animethemes.moe/ShingekiNoKyojin-OP1.webm",
                badge = "LIVE • Attack on Titan 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1000/110531l.jpg",
                synopsis = "Eren Yeager and the Survey Corps confront the truth beyond the walls in an epic battle for freedom."
            ),
            RandomHeroStreamItem(
                animeId = "anime_7",
                title = "Cyberpunk: Edgerunners",
                streamUrl = "https://v.animethemes.moe/CyberpunkEdgerunners-OP1.webm",
                badge = "LIVE • Edgerunners 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1818/126435l.jpg",
                synopsis = "A street kid trying to survive in a technology and body modification-obsessed Night City becomes an edgerunner."
            ),
            RandomHeroStreamItem(
                animeId = "anime_8",
                title = "SPY x FAMILY",
                streamUrl = "https://v.animethemes.moe/SpyXFamily-OP1.webm",
                badge = "LIVE • SPY x FAMILY 1080p",
                bannerUrl = "https://cdn.myanimelist.net/images/anime/1441/122795l.jpg",
                synopsis = "Master spy Twilight forms a disguised family with an assassin and a telepathic child to preserve world peace."
            )
        )
    }

    var currentHeroIdx by remember { mutableIntStateOf(0) }
    val activeHero = randomHeroPool[currentHeroIdx % randomHeroPool.size]

    val filteredCatalog = remember(fullCatalog, searchQuery, selectedGenre) {
        fullCatalog.filter { anime ->
            val matchesQuery = searchQuery.isBlank() ||
                anime.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                anime.titleRomaji.contains(searchQuery, ignoreCase = true) ||
                anime.studio.contains(searchQuery, ignoreCase = true) ||
                anime.genres.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesGenre = selectedGenre == "All" ||
                anime.genres.any { it.equals(selectedGenre, ignoreCase = true) }
            matchesQuery && matchesGenre
        }
    }

    // Password Dialog -> On success, navigates directly to the SEPARATE full-screen Admin Page!
    if (showStealthAdminDialog) {
        SecretAdminDialog(
            onDismiss = { showStealthAdminDialog = false },
            onSuccess = {
                showStealthAdminDialog = false
                onOpenAdminPanel()
            }
        )
    }

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
            .background(TailwindSlate950)
            .statusBarsPadding()
    ) {
        // ====================================================================
        // TAILWIND CSS WEBSITE NAVBAR (Spacious, Single-Line, Zero Crowding)
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TailwindSlate900)
                .border(1.dp, TailwindSlate800)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Brand Logo (5-tap stealth trigger to open Separate Admin Page)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val now = System.currentTimeMillis()
                    if (now - lastHeaderTapTime > 2500L) {
                        secretHeaderTapCount = 1
                    } else {
                        secretHeaderTapCount++
                    }
                    lastHeaderTapTime = now
                    if (secretHeaderTapCount >= 5) {
                        secretHeaderTapCount = 0
                        if (isAdminAuthenticated) {
                            onOpenAdminPanel()
                        } else {
                            showStealthAdminDialog = true
                        }
                    }
                }
            ) {
                Surface(
                    color = TailwindSlate950,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.5.dp, TailwindSky400, RoundedCornerShape(10.dp))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "RS",
                            color = TailwindSky400,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ROBIUL",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = TailwindEmerald400.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier.border(1.dp, TailwindEmerald400.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                        ) {
                            Text(
                                text = "LIVE",
                                color = TailwindEmerald400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "${fullCatalog.size} Anime • 1080p CDN",
                        color = TailwindSlate400,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // Right: Clean Tailwind Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opens Separate Full-Screen Admin Page (Screen.Admin.route)
                Button(
                    onClick = {
                        if (isAdminAuthenticated) {
                            onOpenAdminPanel()
                        } else {
                            showStealthAdminDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdminAuthenticated) TailwindIndigo500 else TailwindSlate800
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .border(1.dp, TailwindSky400.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .testTag("web_admin_modal_btn")
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = TailwindSky400, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Admin",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = { showQuickLinkStrip = !showQuickLinkStrip },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (showQuickLinkStrip) "Hide" else "Links",
                        color = TailwindSky400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Button(
                    onClick = {
                        appUpdateRepository.checkForRepositoryUpdate(autoPrompt = true)
                        catalogRefreshTick++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TailwindSky400),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("check_repo_update_btn")
                ) {
                    if (updateState.isChecking) {
                        CircularProgressIndicator(color = TailwindSlate950, strokeWidth = 2.dp, modifier = Modifier.size(13.dp))
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = TailwindSlate950, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync", color = TailwindSlate950, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                }
            }
        }

        if (showQuickLinkStrip) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TailwindSlate950,
                contentColor = TailwindSky400,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TailwindSky400
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("🌐 Website", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("🚀 Repo & :8080 Server", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (selectedTab == 0) {
            if (showQuickLinkStrip) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TailwindSlate900)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = "Live: ${updateState.liveWebsiteUrl} • LAN: ${updateState.lanWebServerUrl}",
                            color = TailwindSky400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
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
                            colors = ButtonDefaults.buttonColors(containerColor = TailwindSky400),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("copy_website_link_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = TailwindSlate950, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Link", color = TailwindSlate950, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        Button(
                            onClick = {
                                val resultMsg = appUpdateRepository.exportReleaseApkToDownloads()
                                Toast.makeText(context, resultMsg, Toast.LENGTH_LONG).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("download_release_apk_btn")
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = "Release APK", tint = Color.White, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Release APK", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }

                        OutlinedButton(
                            onClick = { useRawHtmlWebView = !useRawHtmlWebView },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (useRawHtmlWebView) "⚡ Fast View" else "🌐 HTML View",
                                color = TailwindSky400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onBack,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("web_portal_back_btn")
                        ) {
                            Text("📱 App Home", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (useRawHtmlWebView) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.allowFileAccess = true
                                settings.allowContentAccess = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.cacheMode = WebSettings.LOAD_NO_CACHE
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onRenderProcessGone(
                                        view: WebView?,
                                        detail: RenderProcessGoneDetail?
                                    ): Boolean {
                                        useRawHtmlWebView = false
                                        return true
                                    }
                                }
                                addJavascriptInterface(
                                    object {
                                        @JavascriptInterface
                                        fun getAppCatalogJson(): String = appUpdateRepository.buildUnifiedAppCatalogJson()

                                        @JavascriptInterface
                                        fun openNativePlayer(animeId: String, episodeNum: Int) {
                                            post { onWatchEpisode(animeId.ifBlank { "anime_1" }, episodeNum.coerceAtLeast(1)) }
                                        }

                                        @JavascriptInterface
                                        fun openNativeAdminPanel() {
                                            post { onOpenAdminPanel() }
                                        }

                                        @JavascriptInterface
                                        fun inspectWebsiteFromWeb(targetUrl: String): String =
                                            appUpdateRepository.inspectWebsiteFromWeb(targetUrl)

                                        @JavascriptInterface
                                        fun authenticateAdminFromWeb(password: String): Boolean =
                                            AdminSecurityManager.authenticate(password)
                                    },
                                    "KuroStreamBridge"
                                )
                                webViewRef = this
                                loadUrl("file:///android_asset/web/index.html?tw=1")
                            }
                        },
                        update = { view -> webViewRef = view },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                // ================================================================
                // TAILWIND CSS WEBSITE LAYOUT (Slate-950 / Slate-900 / Sky-400)
                // Clicking any video or Admin navigates to a SEPARATE full page!
                // ================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(TailwindSlate950),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Tailwind Utility Action Strip
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = {
                                    currentHeroIdx = (currentHeroIdx + 1) % randomHeroPool.size
                                },
                                color = TailwindSlate900,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.border(1.dp, TailwindSky400.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                            ) {
                                Text(
                                    text = "🎲 Random Hero Video",
                                    color = TailwindSky400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = {
                                    selectedGenre = "All"
                                    searchQuery = ""
                                },
                                color = TailwindSlate900,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.border(1.dp, TailwindSlate800, RoundedCornerShape(999.dp))
                            ) {
                                Text(
                                    text = "📚 All ${fullCatalog.size} Anime",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = {
                                    if (isAdminAuthenticated) {
                                        onOpenAdminPanel()
                                    } else {
                                        showStealthAdminDialog = true
                                    }
                                },
                                color = TailwindSlate900,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.border(1.dp, TailwindIndigo500.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                            ) {
                                Text(
                                    text = "🕵️‍♂️ URL Inspector (Admin Page)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                onClick = {
                                    if (isAdminAuthenticated) {
                                        onOpenAdminPanel()
                                    } else {
                                        showStealthAdminDialog = true
                                    }
                                },
                                color = TailwindSlate900,
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier
                                    .border(1.dp, TailwindEmerald400.copy(alpha = 0.5f), RoundedCornerShape(999.dp))
                                    .testTag("open_native_admin_from_web_btn")
                            ) {
                                Text(
                                    text = "🛠️ Open Admin Page (${paidServers.size} CDNs)",
                                    color = TailwindEmerald400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // 2. Tailwind Hero Spotlight Card (Clicking Watch opens Separate Video Player Page)
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = TailwindSlate900),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp)
                                .border(1.dp, TailwindSlate800, RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(240.dp)
                            ) {
                                AsyncImage(
                                    model = activeHero.bannerUrl,
                                    contentDescription = activeHero.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0x99020617),
                                                    Color(0x55020617),
                                                    Color(0xF2020617)
                                                )
                                            )
                                        )
                                )

                                // Top Row: Live Badge & Randomize Button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .align(Alignment.TopStart),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = TailwindSlate950.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(999.dp),
                                        modifier = Modifier.border(1.dp, TailwindEmerald400.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                                    ) {
                                        Text(
                                            text = "🟢 ${activeHero.badge}",
                                            color = TailwindEmerald400,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        onClick = {
                                            currentHeroIdx = (currentHeroIdx + 1) % randomHeroPool.size
                                        },
                                        color = TailwindIndigo500,
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Text(
                                            text = "🎲 Next Random Video",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }

                                // Bottom Row: Title, Synopsis, and Separate Watch Page Button
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = activeHero.title,
                                        color = Color.White,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = activeHero.synopsis,
                                        color = TailwindSlate400,
                                        fontSize = 11.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Opens Separate Full-Screen Video Player Page
                                        Button(
                                            onClick = { onWatchEpisode(activeHero.animeId, 1) },
                                            colors = ButtonDefaults.buttonColors(containerColor = TailwindSky400),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TailwindSlate950, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Watch Video Page", color = TailwindSlate950, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                currentHeroIdx = (currentHeroIdx + 1) % randomHeroPool.size
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("🎲 Shuffle Video", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 3. Tailwind Search Input & Genre Pills
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null, tint = TailwindSky400)
                                },
                                placeholder = {
                                    Text("Search ${fullCatalog.size} anime, genres, studios...", fontSize = 12.sp, color = TailwindSlate400)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TailwindSky400,
                                    unfocusedBorderColor = TailwindSlate800,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = TailwindSlate900,
                                    unfocusedContainerColor = TailwindSlate900
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            val genres = listOf("All", "Action", "Fantasy", "Adventure", "Sci-Fi", "Supernatural", "Comedy", "Drama", "Romance")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genres.forEach { genre ->
                                    val selected = selectedGenre == genre
                                    Surface(
                                        onClick = { selectedGenre = genre },
                                        color = if (selected) TailwindSky400 else TailwindSlate900,
                                        shape = RoundedCornerShape(999.dp),
                                        modifier = Modifier.border(
                                            1.dp,
                                            if (selected) TailwindSky400 else TailwindSlate800,
                                            RoundedCornerShape(999.dp)
                                        )
                                    ) {
                                        Text(
                                            text = if (genre == "All") "All Anime (${fullCatalog.size})" else genre,
                                            color = if (selected) TailwindSlate950 else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Complete Anime Catalog (${filteredCatalog.size})",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "1080p • Bangla & Eng Sub",
                                    color = TailwindSky400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 4. Tailwind 2-Column Anime Grid (Clicking any card opens Separate Video Player Page)
                    items(filteredCatalog.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { anime ->
                                TailwindWebsiteAnimeCard(
                                    anime = anime,
                                    onWatchClick = { onWatchEpisode(anime.id, 1) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // Tab 1: Repository Auto-Update & Unified Website Hosting Control
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TailwindSlate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TailwindSlate800, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SystemUpdateAlt, contentDescription = null, tint = TailwindSky400, modifier = Modifier.size(22.dp))
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
                                    color = TailwindEmerald400.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = if (updateState.isUpdateAvailable) "UPDATE READY (v${updateState.latestVersionName})" else "UP TO DATE",
                                        color = TailwindEmerald400,
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
                                    colors = ButtonDefaults.buttonColors(containerColor = TailwindSky400),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.CloudDownload, contentDescription = null, tint = TailwindSlate950, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Show Auto-Update Prompt", color = TailwindSlate950, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { appUpdateRepository.markUpdateInstalled() },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Mark v${updateState.latestVersionName} Installed", color = TailwindSky400, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TailwindSlate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TailwindSlate800, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Icon(Icons.Default.Dns, contentDescription = null, tint = TailwindSky400, modifier = Modifier.size(22.dp))
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
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TailwindEmerald400)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Local Server URL: ${updateState.lanWebServerUrl}  |  GitHub Pages: ${updateState.liveWebsiteUrl}",
                                color = TailwindSky400,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = TailwindSlate900),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, TailwindSlate800, RoundedCornerShape(14.dp))
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

                            OutlinedTextField(
                                value = repoSlugInput,
                                onValueChange = { repoSlugInput = it },
                                label = { Text("GitHub Repository (owner/repo)") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TailwindSky400,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = webUrlInput,
                                onValueChange = { webUrlInput = it },
                                label = { Text("Live Website URL") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TailwindSky400,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = apkUrlInput,
                                onValueChange = { apkUrlInput = it },
                                label = { Text("Direct APK Download URL") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TailwindSky400,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = versionNameInput,
                                onValueChange = { versionNameInput = it },
                                label = { Text("Target Repository Version Name") },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = TailwindSky400,
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
                                    focusedBorderColor = TailwindSky400,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

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
                                colors = ButtonDefaults.buttonColors(containerColor = TailwindIndigo500),
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

@Composable
private fun TailwindWebsiteAnimeCard(
    anime: Anime,
    onWatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TailwindSlate900),
        modifier = modifier
            .border(1.dp, TailwindSlate800, RoundedCornerShape(14.dp))
            .clickable { onWatchClick() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
            ) {
                AsyncImage(
                    model = anime.posterUrl,
                    contentDescription = anime.titleEnglish,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    color = TailwindSlate950.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .border(1.dp, TailwindSlate800, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "★ ${String.format(Locale.US, "%.1f", (anime.rating * 2f).coerceIn(7.8f, 9.9f))}",
                        color = StarAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    color = TailwindIndigo500,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${anime.episodesCount} EPS",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = anime.titleEnglish,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${anime.studio} • ${anime.genres.take(2).joinToString(", ")}",
                    color = TailwindSlate400,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = onWatchClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TailwindSlate800),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .border(1.dp, TailwindSky400.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = TailwindSky400, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Watch Video Page", color = TailwindSky400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
