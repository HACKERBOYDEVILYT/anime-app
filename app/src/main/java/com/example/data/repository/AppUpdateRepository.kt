package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.Anime
import com.example.data.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RepositoryUpdateState(
    val installedVersionCode: Int = 2,
    val installedVersionName: String = "1.1.0",
    val latestVersionCode: Int = 2,
    val latestVersionName: String = "1.1.0",
    val latestCommitSha: String = "main-latest",
    val repositorySlug: String = "robiul/robiul-rs",
    val liveWebsiteUrl: String = "https://robiul.github.io/robiul-rs/",
    val apkDownloadUrl: String = "https://github.com/robiul/robiul-rs/releases/latest/download/Robiul-Release.apk",
    val changelog: String = "• Robiul [RS] Hacker Edition Android App & Live Web Streaming Portal from the same repository\n• Custom Media3 ExoPlayer Progress Bar with live buffer, OP/ED chapters & time telemetry\n• Real-time Crunchyroll Simulcast (1080p HLS), HiAnime HD-1/HD-2 & AnimeThemes servers\n• Automatic Repository Update Checker & In-App Update Prompt",
    val isUpdateAvailable: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val forceUpdate: Boolean = false,
    val autoCheckOnStartup: Boolean = true,
    val isChecking: Boolean = false,
    val lastCheckedTime: String = "Synced",
    val isLanWebServerRunning: Boolean = false,
    val lanWebServerUrl: String = "http://127.0.0.1:8080"
)

/**
 * Manages:
 * 1. Automatic Repository Update Checking (GitHub `docs/version.json`, GitHub Releases API, and Commits API).
 * 2. Automatic In-App Update Prompt when a new version or commit is pushed to the repository.
 * 3. Unified App <-> Website synchronization and built-in LAN HTTP Website Server (`port 8080`)
 *    so the exact same website and catalog run both locally and on GitHub Pages / Vercel / Netlify.
 */
class AppUpdateRepository(
    private val context: Context,
    private val mediaProvider: LocalLicensedMediaProvider
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("kuro_repo_update_prefs", Context.MODE_PRIVATE)

    private val _updateState = MutableStateFlow(loadInitialState())
    val updateState: StateFlow<RepositoryUpdateState> = _updateState.asStateFlow()

    private var serverSocket: ServerSocket? = null
    @Volatile
    private var isServerActive = false

    init {
        if (_updateState.value.autoCheckOnStartup) {
            checkForRepositoryUpdate(autoPrompt = true)
        }
    }

    private fun loadInitialState(): RepositoryUpdateState {
        val installedCode = prefs.getInt("installed_version_code", 2)
        val installedName = prefs.getString("installed_version_name", "1.1.0") ?: "1.1.0"
        val latestCode = prefs.getInt("latest_version_code", 2)
        val latestName = prefs.getString("latest_version_name", "1.1.0") ?: "1.1.0"
        val repoSlug = prefs.getString("repo_slug", "robiul/robiul-rs") ?: "robiul/robiul-rs"
        val webUrl = prefs.getString("live_web_url", "https://${repoSlug.substringBefore("/")}.github.io/${repoSlug.substringAfter("/")}/")
            ?: "https://robiul.github.io/robiul-rs/"
        val apkUrl = prefs.getString(
            "apk_download_url",
            "https://github.com/$repoSlug/releases/latest/download/Robiul-Release.apk"
        ) ?: "https://github.com/$repoSlug/releases/latest/download/Robiul-Release.apk"
        val changelog = prefs.getString(
            "latest_changelog",
            "• Robiul [RS] Hacker Edition Android App & Live Web Streaming Portal from the same repository\n• Custom Media3 ExoPlayer Progress Bar with live buffer, OP/ED chapters & time telemetry\n• Real-time Crunchyroll Simulcast (1080p HLS), HiAnime HD-1/HD-2 & AnimeThemes servers\n• Automatic Repository Update Checker & In-App Update Prompt"
        ) ?: ""
        val forceUpdate = prefs.getBoolean("force_update", false)
        val autoCheck = prefs.getBoolean("auto_check_startup", true)
        val dismissedVersion = prefs.getInt("dismissed_version_code", 0)
        val hasUpdate = latestCode > installedCode

        return RepositoryUpdateState(
            installedVersionCode = installedCode,
            installedVersionName = installedName,
            latestVersionCode = latestCode,
            latestVersionName = latestName,
            repositorySlug = repoSlug,
            liveWebsiteUrl = webUrl,
            apkDownloadUrl = apkUrl,
            changelog = changelog,
            isUpdateAvailable = hasUpdate,
            showUpdateDialog = hasUpdate && (forceUpdate || dismissedVersion < latestCode),
            forceUpdate = forceUpdate,
            autoCheckOnStartup = autoCheck,
            lanWebServerUrl = "http://${getDeviceIpAddress()}:8080"
        )
    }

    /**
     * Queries the configured GitHub repository (`docs/version.json`, `/releases/latest`, and `/commits/main`)
     * to detect if any new update was pushed to the repository.
     */
    fun checkForRepositoryUpdate(autoPrompt: Boolean = true) {
        val current = _updateState.value
        val slug = current.repositorySlug.trim().removePrefix("https://github.com/").removeSuffix("/")
        _updateState.update { it.copy(isChecking = true) }

        scope.launch {
            var foundVersionCode = current.latestVersionCode
            var foundVersionName = current.latestVersionName
            var foundCommitSha = current.latestCommitSha
            var foundApkUrl = current.apkDownloadUrl
            var foundWebUrl = current.liveWebsiteUrl
            var foundChangelog = current.changelog
            var foundForce = current.forceUpdate

            // 1. Check raw `docs/version.json` in the GitHub repository (main or master branch)
            val rawUrls = listOf(
                "https://raw.githubusercontent.com/$slug/main/docs/version.json",
                "https://raw.githubusercontent.com/$slug/master/docs/version.json",
                "${current.liveWebsiteUrl.trimEnd('/')}/version.json"
            )

            for (url in rawUrls) {
                try {
                    val req = Request.Builder().url(url).get().build()
                    RetrofitClient.okHttpClient.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            val body = resp.body?.string().orEmpty()
                            if (body.trim().startsWith("{")) {
                                val json = JSONObject(body)
                                foundVersionCode = json.optInt("versionCode", foundVersionCode)
                                foundVersionName = json.optString("versionName", foundVersionName).ifBlank { foundVersionName }
                                foundCommitSha = json.optString("commitSha", foundCommitSha).ifBlank { foundCommitSha }
                                foundApkUrl = json.optString("apkDownloadUrl", foundApkUrl).ifBlank { foundApkUrl }
                                foundWebUrl = json.optString("webPortalUrl", foundWebUrl).ifBlank { foundWebUrl }
                                foundChangelog = json.optString("changelog", foundChangelog).ifBlank { foundChangelog }
                                foundForce = json.optBoolean("forceUpdate", foundForce)
                                break
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }

            // 2. Also check GitHub Releases API (/releases/latest)
            try {
                val releaseReq = Request.Builder()
                    .url("https://api.github.com/repos/$slug/releases/latest")
                    .header("Accept", "application/vnd.github+json")
                    .get()
                    .build()
                RetrofitClient.okHttpClient.newCall(releaseReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string().orEmpty())
                        val tagName = json.optString("tag_name", "").removePrefix("v")
                        val bodyNotes = json.optString("body", "")
                        if (tagName.isNotBlank() && tagName != current.installedVersionName) {
                            foundVersionName = tagName
                            foundVersionCode = foundVersionCode.coerceAtLeast(current.installedVersionCode + 1)
                        }
                        if (bodyNotes.isNotBlank()) {
                            foundChangelog = bodyNotes
                        }
                        val assets = json.optJSONArray("assets")
                        if (assets != null && assets.length() > 0) {
                            for (i in 0 until assets.length()) {
                                val asset = assets.optJSONObject(i) ?: continue
                                val dl = asset.optString("browser_download_url", "")
                                if (dl.endsWith(".apk", ignoreCase = true)) {
                                    foundApkUrl = dl
                                    break
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            // 3. Check latest commit on main branch so even a direct commit push triggers an update notification
            try {
                val commitReq = Request.Builder()
                    .url("https://api.github.com/repos/$slug/commits/main")
                    .header("Accept", "application/vnd.github+json")
                    .get()
                    .build()
                RetrofitClient.okHttpClient.newCall(commitReq).execute().use { resp ->
                    if (resp.isSuccessful) {
                        val json = JSONObject(resp.body?.string().orEmpty())
                        val sha = json.optString("sha", "").take(7)
                        val commitObj = json.optJSONObject("commit")
                        val msg = commitObj?.optString("message", "").orEmpty().lineSequence().firstOrNull().orEmpty()
                        if (sha.isNotBlank() && sha != current.latestCommitSha) {
                            foundCommitSha = sha
                            foundVersionCode = foundVersionCode.coerceAtLeast(current.installedVersionCode + 1)
                            if (msg.isNotBlank()) {
                                foundChangelog = "Latest Repository Commit ($sha): $msg\n\n$foundChangelog"
                            }
                        }
                    }
                }
            } catch (_: Exception) {
            }

            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val installedCode = _updateState.value.installedVersionCode
            val hasUpdate = foundVersionCode > installedCode || foundVersionName != _updateState.value.installedVersionName

            prefs.edit()
                .putInt("latest_version_code", foundVersionCode)
                .putString("latest_version_name", foundVersionName)
                .putString("apk_download_url", foundApkUrl)
                .putString("live_web_url", foundWebUrl)
                .putString("latest_changelog", foundChangelog)
                .putBoolean("force_update", foundForce)
                .apply()

            _updateState.update {
                it.copy(
                    latestVersionCode = foundVersionCode,
                    latestVersionName = foundVersionName,
                    latestCommitSha = foundCommitSha,
                    apkDownloadUrl = foundApkUrl,
                    liveWebsiteUrl = foundWebUrl,
                    changelog = foundChangelog,
                    forceUpdate = foundForce,
                    isUpdateAvailable = hasUpdate,
                    showUpdateDialog = if (autoPrompt && hasUpdate) true else it.showUpdateDialog,
                    isChecking = false,
                    lastCheckedTime = "Checked $timeStr"
                )
            }
        }
    }

    /**
     * Saves repository & website configuration and optionally triggers an immediate update prompt across the app.
     */
    fun updateRepositoryConfig(
        repositorySlug: String,
        liveWebsiteUrl: String,
        apkDownloadUrl: String,
        latestVersionName: String,
        changelog: String,
        forceUpdate: Boolean,
        autoCheckOnStartup: Boolean,
        triggerPromptNow: Boolean = true
    ) {
        val cleanSlug = repositorySlug.trim().removePrefix("https://github.com/").removeSuffix("/").ifBlank { "kurostream/kurostream" }
        val cleanWeb = liveWebsiteUrl.trim().ifBlank { "https://${cleanSlug.substringBefore("/")}.github.io/${cleanSlug.substringAfter("/")}/" }
        val cleanApk = apkDownloadUrl.trim().ifBlank { "https://github.com/$cleanSlug/releases/latest/download/KuroStream-Release.apk" }
        val nextCode = (_updateState.value.latestVersionCode + 1).coerceAtLeast(_updateState.value.installedVersionCode + 1)

        prefs.edit()
            .putString("repo_slug", cleanSlug)
            .putString("live_web_url", cleanWeb)
            .putString("apk_download_url", cleanApk)
            .putString("latest_version_name", latestVersionName.ifBlank { "1.1.0" })
            .putInt("latest_version_code", nextCode)
            .putString("latest_changelog", changelog)
            .putBoolean("force_update", forceUpdate)
            .putBoolean("auto_check_startup", autoCheckOnStartup)
            .remove("dismissed_version_code")
            .apply()

        _updateState.update {
            it.copy(
                repositorySlug = cleanSlug,
                liveWebsiteUrl = cleanWeb,
                apkDownloadUrl = cleanApk,
                latestVersionName = latestVersionName.ifBlank { "1.1.0" },
                latestVersionCode = nextCode,
                changelog = changelog,
                forceUpdate = forceUpdate,
                autoCheckOnStartup = autoCheckOnStartup,
                isUpdateAvailable = true,
                showUpdateDialog = triggerPromptNow
            )
        }
    }

    fun dismissUpdateDialog() {
        val current = _updateState.value
        if (!current.forceUpdate) {
            prefs.edit().putInt("dismissed_version_code", current.latestVersionCode).apply()
            _updateState.update { it.copy(showUpdateDialog = false) }
        }
    }

    fun showUpdateDialogManually() {
        _updateState.update { it.copy(showUpdateDialog = true, isUpdateAvailable = true) }
    }

    fun markUpdateInstalled() {
        val current = _updateState.value
        prefs.edit()
            .putInt("installed_version_code", current.latestVersionCode)
            .putString("installed_version_name", current.latestVersionName)
            .putInt("dismissed_version_code", current.latestVersionCode)
            .apply()

        _updateState.update {
            it.copy(
                installedVersionCode = current.latestVersionCode,
                installedVersionName = current.latestVersionName,
                isUpdateAvailable = false,
                showUpdateDialog = false
            )
        }
    }

    /**
     * Generates the unified JSON payload shared between the Android App and the Live Website.
     */
    fun buildUnifiedAppCatalogJson(): String {
        val state = _updateState.value
        val catalog: List<Anime> = mediaProvider.getAllCatalogSnapshot()
        val root = JSONObject()
        root.put("appName", "KuroStream")
        root.put("versionName", state.latestVersionName)
        root.put("versionCode", state.latestVersionCode)
        root.put("repositorySlug", state.repositorySlug)
        root.put("apkDownloadUrl", state.apkDownloadUrl)
        root.put("webPortalUrl", state.liveWebsiteUrl)

        val animeArray = JSONArray()
        catalog.forEach { anime ->
            val obj = JSONObject()
            obj.put("id", anime.id)
            obj.put("title", anime.titleEnglish)
            obj.put("studio", anime.studio)
            obj.put("score", String.format(Locale.US, "%.1f", anime.rating * 2f))
            obj.put("episodes", anime.episodesCount)
            obj.put("poster", anime.posterUrl)
            obj.put("banner", anime.bannerUrl)
            animeArray.put(obj)
        }
        root.put("anime", animeArray)
        return root.toString()
    }

    // ========================================================================
    // BUILT-IN LIVE LAN HTTP SERVER (SERVES UNIFIED WEBSITE & API FROM APP)
    // ========================================================================

    fun toggleLanWebServer() {
        if (isServerActive) {
            stopLanWebServer()
        } else {
            startLanWebServer()
        }
    }

    fun startLanWebServer(port: Int = 8080) {
        if (isServerActive) return
        scope.launch {
            try {
                val socket = ServerSocket(port)
                serverSocket = socket
                isServerActive = true
                val ip = getDeviceIpAddress()
                _updateState.update {
                    it.copy(
                        isLanWebServerRunning = true,
                        lanWebServerUrl = "http://$ip:$port"
                    )
                }

                while (isServerActive && !socket.isClosed) {
                    val client = try {
                        socket.accept()
                    } catch (_: Exception) {
                        break
                    }
                    scope.launch {
                        handleHttpClient(client)
                    }
                }
            } catch (_: Exception) {
                isServerActive = false
                _updateState.update { it.copy(isLanWebServerRunning = false) }
            }
        }
    }

    fun stopLanWebServer() {
        isServerActive = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
        serverSocket = null
        _updateState.update { it.copy(isLanWebServerRunning = false) }
    }

    private suspend fun handleHttpClient(client: java.net.Socket) = withContext(Dispatchers.IO) {
        try {
            client.use { sock ->
                val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
                val requestLine = reader.readLine().orEmpty()
                val path = requestLine.split(" ").getOrNull(1) ?: "/"

                val output = PrintWriter(sock.getOutputStream(), true)
                when {
                    path.startsWith("/version.json") || path.startsWith("/api/catalog") -> {
                        val json = buildUnifiedAppCatalogJson()
                        output.print("HTTP/1.1 200 OK\r\n")
                        output.print("Content-Type: application/json; charset=utf-8\r\n")
                        output.print("Access-Control-Allow-Origin: *\r\n")
                        output.print("Content-Length: ${json.toByteArray(Charsets.UTF_8).size}\r\n\r\n")
                        output.print(json)
                        output.flush()
                    }
                    else -> {
                        val html = loadBundledWebsiteHtml()
                        output.print("HTTP/1.1 200 OK\r\n")
                        output.print("Content-Type: text/html; charset=utf-8\r\n")
                        output.print("Access-Control-Allow-Origin: *\r\n")
                        output.print("Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n\r\n")
                        output.print(html)
                        output.flush()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    fun loadBundledWebsiteHtml(): String {
        return try {
            context.assets.open("web/index.html").bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "<html><body style='background:#0B0C10;color:#fff;'><h1>KuroStream Unified Web Portal</h1></body></html>"
        }
    }

    private fun getDeviceIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "127.0.0.1"
            for (intf in interfaces) {
                val addrs = intf.inetAddresses
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {
        }
        return "127.0.0.1"
    }
}
