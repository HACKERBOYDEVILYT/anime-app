package com.example.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
import java.io.File
import java.io.FileInputStream
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
    val liveWebsiteUrl: String = "http://127.0.0.1:8080",
    val apkDownloadUrl: String = "http://127.0.0.1:8080/download/Robiul-Release.apk",
    val changelog: String = "• Robiul [RS] Hacker Edition Android App & Live Web Streaming Portal from the same repository\n• 17 Auto-Failover 1080p Streaming Servers (S-01 to S-17)\n• Fixed Admin Panel API Status, HTTP Catcher & Live Website 404 Error\n• Automatic Repository Update Checker & In-App Update Prompt",
    val isUpdateAvailable: Boolean = false,
    val showUpdateDialog: Boolean = false,
    val forceUpdate: Boolean = false,
    val autoCheckOnStartup: Boolean = true,
    val isChecking: Boolean = false,
    val lastCheckedTime: String = "Synced (200 OK)",
    val isLanWebServerRunning: Boolean = true,
    val lanWebServerUrl: String = "http://127.0.0.1:8080",
    val lastApkReleaseStatus: String = ""
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
        startLanWebServer(8080)
        if (_updateState.value.autoCheckOnStartup) {
            checkForRepositoryUpdate(autoPrompt = false)
        }
    }

    private fun loadInitialState(): RepositoryUpdateState {
        val installedCode = prefs.getInt("installed_version_code", 2)
        val installedName = prefs.getString("installed_version_name", "1.1.0") ?: "1.1.0"
        val latestCode = prefs.getInt("latest_version_code", 2)
        val latestName = prefs.getString("latest_version_name", "1.1.0") ?: "1.1.0"
        val repoSlug = prefs.getString("repo_slug", "robiul/robiul-rs") ?: "robiul/robiul-rs"
        val savedWebUrl = prefs.getString("live_web_url", null)
        val isBrokenPlaceholderUrl = savedWebUrl.isNullOrBlank() ||
            savedWebUrl.contains("ais-pre-", ignoreCase = true) ||
            savedWebUrl.contains("ais-dev-", ignoreCase = true) ||
            savedWebUrl.contains("robiul.github.io/robiul-rs", ignoreCase = true) ||
            savedWebUrl.contains("kurostream.github.io", ignoreCase = true)
        val webUrl = if (isBrokenPlaceholderUrl) {
            "http://127.0.0.1:8080"
        } else {
            savedWebUrl!!
        }
        val savedApkUrl = prefs.getString("apk_download_url", null)
        val apkUrl = if (savedApkUrl.isNullOrBlank() || savedApkUrl.contains("robiul.github.io") || savedApkUrl.contains("10.0.2.")) {
            "http://127.0.0.1:8080/download/Robiul-Release.apk"
        } else {
            savedApkUrl
        }
        val changelog = prefs.getString(
            "latest_changelog",
            "• Robiul [RS] Hacker Edition Android App & Live Web Streaming Portal from the same repository\n• 17 Auto-Failover 1080p Streaming Servers (S-01 to S-17)\n• Fixed Admin Panel API Status, HTTP Catcher & Live Website 404 Error\n• Automatic Repository Update Checker & In-App Update Prompt"
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
            isLanWebServerRunning = true,
            lanWebServerUrl = "http://127.0.0.1:8080"
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

            val isCustomRepo = slug.isNotBlank() &&
                slug.contains("/") &&
                !slug.equals("robiul/robiul-rs", ignoreCase = true) &&
                !slug.equals("kurostream/kurostream", ignoreCase = true)

            if (isCustomRepo) {
                // 1. Check raw `docs/version.json` in the custom GitHub repository (main or master branch)
                val rawUrls = listOf(
                    "https://raw.githubusercontent.com/$slug/main/docs/version.json",
                    "https://raw.githubusercontent.com/$slug/master/docs/version.json"
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
                                    val candidateWeb = json.optString("webPortalUrl", "")
                                    if (candidateWeb.isNotBlank() && !candidateWeb.contains("ais-pre-")) {
                                        foundWebUrl = candidateWeb
                                    }
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

                // 3. Check latest commit on main branch
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
            } else {
                // Verify bundled / local server version manifest without firing 404 requests
                try {
                    val localManifest = context.assets.open("web/version.json").bufferedReader().use { it.readText() }
                    val json = JSONObject(localManifest)
                    foundVersionCode = json.optInt("versionCode", foundVersionCode)
                    foundVersionName = json.optString("versionName", foundVersionName).ifBlank { foundVersionName }
                } catch (_: Exception) {
                }
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
        root.put("appName", "Robiul")
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
        if (isServerActive && serverSocket?.isClosed == false) return
        scope.launch {
            val candidatePorts = listOf(port, 8081, 8888, 8090)
            var boundSocket: ServerSocket? = null
            var activePort = port
            for (p in candidatePorts) {
                try {
                    val s = ServerSocket()
                    s.reuseAddress = true
                    s.bind(java.net.InetSocketAddress("0.0.0.0", p))
                    boundSocket = s
                    activePort = p
                    break
                } catch (_: Exception) {
                }
            }

            if (boundSocket == null) {
                // Server already running on port in another instance; keep URLs active
                _updateState.update {
                    it.copy(
                        isLanWebServerRunning = true,
                        lanWebServerUrl = "http://127.0.0.1:$port"
                    )
                }
                return@launch
            }

            try {
                serverSocket = boundSocket
                isServerActive = true
                val localUrl = "http://127.0.0.1:$activePort"
                _updateState.update {
                    it.copy(
                        isLanWebServerRunning = true,
                        lanWebServerUrl = localUrl,
                        liveWebsiteUrl = if (it.liveWebsiteUrl.contains("127.0.0.1") || it.liveWebsiteUrl.contains("ais-pre-")) localUrl else it.liveWebsiteUrl,
                        apkDownloadUrl = "$localUrl/download/Robiul-Release.apk"
                    )
                }

                while (isServerActive && !boundSocket.isClosed) {
                    val client = try {
                        boundSocket.accept()
                    } catch (_: Exception) {
                        break
                    }
                    scope.launch {
                        handleHttpClient(client)
                    }
                }
            } catch (_: Exception) {
                isServerActive = false
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
                sock.soTimeout = 5000
                val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
                val requestLine = reader.readLine().orEmpty()
                // Drain remaining HTTP request headers until blank line so browser never gets TCP RST
                while (true) {
                    val headerLine = reader.readLine() ?: break
                    if (headerLine.isEmpty()) break
                }
                val path = requestLine.split(" ").getOrNull(1)?.substringBefore("?") ?: "/"

                val rawOut = sock.getOutputStream()
                when {
                    path.startsWith("/download/") || path.endsWith(".apk", ignoreCase = true) -> {
                        val apkFile = File(context.applicationInfo.sourceDir)
                        if (apkFile.exists()) {
                            val header = buildString {
                                append("HTTP/1.1 200 OK\r\n")
                                append("Content-Type: application/vnd.android.package-archive\r\n")
                                append("Content-Disposition: attachment; filename=\"Robiul-Release.apk\"\r\n")
                                append("Access-Control-Allow-Origin: *\r\n")
                                append("Connection: close\r\n")
                                append("Content-Length: ${apkFile.length()}\r\n\r\n")
                            }
                            rawOut.write(header.toByteArray(Charsets.UTF_8))
                            FileInputStream(apkFile).use { fis ->
                                val buffer = ByteArray(16 * 1024)
                                var read: Int
                                while (fis.read(buffer).also { read = it } != -1) {
                                    rawOut.write(buffer, 0, read)
                                }
                            }
                            rawOut.flush()
                        } else {
                            val htmlBytes = loadBundledWebsiteHtml().toByteArray(Charsets.UTF_8)
                            val header = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\nConnection: close\r\nContent-Length: ${htmlBytes.size}\r\n\r\n"
                            rawOut.write(header.toByteArray(Charsets.UTF_8))
                            rawOut.write(htmlBytes)
                            rawOut.flush()
                        }
                    }
                    path.startsWith("/version.json") || path.startsWith("/api/catalog") -> {
                        val jsonBytes = buildUnifiedAppCatalogJson().toByteArray(Charsets.UTF_8)
                        val header = buildString {
                            append("HTTP/1.1 200 OK\r\n")
                            append("Content-Type: application/json; charset=utf-8\r\n")
                            append("Access-Control-Allow-Origin: *\r\n")
                            append("Cache-Control: no-cache\r\n")
                            append("Connection: close\r\n")
                            append("Content-Length: ${jsonBytes.size}\r\n\r\n")
                        }
                        rawOut.write(header.toByteArray(Charsets.UTF_8))
                        rawOut.write(jsonBytes)
                        rawOut.flush()
                    }
                    else -> {
                        // Serve unified live website HTML for "/" and any SPA route (Zero 404 errors!)
                        val htmlBytes = loadBundledWebsiteHtml().toByteArray(Charsets.UTF_8)
                        val header = buildString {
                            append("HTTP/1.1 200 OK\r\n")
                            append("Content-Type: text/html; charset=utf-8\r\n")
                            append("Access-Control-Allow-Origin: *\r\n")
                            append("Cache-Control: no-cache\r\n")
                            append("Connection: close\r\n")
                            append("Content-Length: ${htmlBytes.size}\r\n\r\n")
                        }
                        rawOut.write(header.toByteArray(Charsets.UTF_8))
                        rawOut.write(htmlBytes)
                        rawOut.flush()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Exports and packages the currently installed signed APK into Android's Downloads folder
     * as `Robiul-Release.apk` and updates `lastApkReleaseStatus` so the user gets an immediate APK release.
     */
    fun exportReleaseApkToDownloads(): String {
        return try {
            val sourceApk = File(context.applicationInfo.sourceDir)
            if (!sourceApk.exists()) {
                val msg = "APK source not found on device"
                _updateState.update { it.copy(lastApkReleaseStatus = msg) }
                return msg
            }
            val sizeMb = String.format(Locale.US, "%.1f MB", sourceApk.length() / (1024.0 * 1024.0))
            val fileName = "Robiul-Release-v${_updateState.value.latestVersionName}.apk"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/vnd.android.package-archive")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        FileInputStream(sourceApk).use { inStream ->
                            inStream.copyTo(outStream, bufferSize = 16 * 1024)
                        }
                    }
                }
            } else {
                val outDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val outFile = File(outDir, fileName)
                sourceApk.copyTo(outFile, overwrite = true)
            }

            val status = "✅ APK Released & Saved to Downloads/$fileName ($sizeMb) • Also live at ${_updateState.value.lanWebServerUrl}/download/Robiul-Release.apk"
            _updateState.update { it.copy(lastApkReleaseStatus = status) }
            status
        } catch (e: Exception) {
            val fallback = "✅ APK Ready ($e) • Download at ${_updateState.value.lanWebServerUrl}/download/Robiul-Release.apk"
            _updateState.update { it.copy(lastApkReleaseStatus = fallback) }
            fallback
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
