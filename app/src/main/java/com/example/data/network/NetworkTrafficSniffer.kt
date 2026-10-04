package com.example.data.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CapturedNetworkPacket(
    val id: String,
    val method: String,
    val url: String,
    val host: String,
    val statusCode: Int,
    val contentType: String,
    val isMediaStream: Boolean,
    val latencyMs: Long,
    val timestamp: String,
    val sourceTag: String = "HTTP Catcher" // "OkHttp Proxy" or "WebView Stream Sniffer"
)

/**
 * Built-in "Stream / HTTP Catcher" style Network Traffic Analyzer & Media Stream Sniffer
 * controllable directly from the Admin Panel.
 */
object NetworkTrafficSniffer {

    private val _isCaptureEnabled = MutableStateFlow(true)
    val isCaptureEnabled: StateFlow<Boolean> = _isCaptureEnabled.asStateFlow()

    private val _mediaOnlyFilter = MutableStateFlow(false)
    val mediaOnlyFilter: StateFlow<Boolean> = _mediaOnlyFilter.asStateFlow()

    private val _capturedPackets = MutableStateFlow<List<CapturedNetworkPacket>>(emptyList())
    val capturedPackets: StateFlow<List<CapturedNetworkPacket>> = _capturedPackets.asStateFlow()

    fun setCaptureEnabled(enabled: Boolean) {
        _isCaptureEnabled.value = enabled
    }

    fun setMediaOnlyFilter(mediaOnly: Boolean) {
        _mediaOnlyFilter.value = mediaOnly
    }

    fun clearCapturedPackets() {
        _capturedPackets.value = emptyList()
    }

    fun isMediaUrlOrMime(url: String, contentType: String): Boolean {
        val u = url.lowercase()
        val c = contentType.lowercase()
        return u.contains(".m3u8") ||
            u.contains(".mp4") ||
            u.contains(".webm") ||
            u.contains(".mkv") ||
            u.contains(".mpd") ||
            u.contains("youtube.com/embed") ||
            u.contains("animethemes.moe") ||
            u.contains("googlevideo.com") ||
            c.contains("video/") ||
            c.contains("mpegurl") ||
            c.contains("dash+xml") ||
            c.contains("octet-stream")
    }

    fun recordPacket(
        method: String,
        url: String,
        statusCode: Int,
        contentType: String,
        latencyMs: Long,
        sourceTag: String = "HTTP Catcher"
    ) {
        if (!_isCaptureEnabled.value) return
        val host = runCatching { URI(url).host ?: "unknown-host" }.getOrDefault("unknown-host")
        val isMedia = isMediaUrlOrMime(url, contentType)
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val packet = CapturedNetworkPacket(
            id = "pkt_${System.currentTimeMillis()}_${(100..999).random()}",
            method = method,
            url = url,
            host = host,
            statusCode = statusCode,
            contentType = contentType.ifBlank { if (isMedia) "video/stream" else "application/octet-stream" },
            isMediaStream = isMedia,
            latencyMs = latencyMs,
            timestamp = timeStr,
            sourceTag = sourceTag
        )

        _capturedPackets.update { current ->
            (listOf(packet) + current).distinctBy { it.url + it.statusCode }.take(80)
        }
    }

    val okHttpSnifferInterceptor = Interceptor { chain ->
        val request = chain.request()
        if (!_isCaptureEnabled.value) {
            return@Interceptor chain.proceed(request)
        }

        val startTime = System.currentTimeMillis()
        val url = request.url.toString()
        val method = request.method

        try {
            val response: Response = chain.proceed(request)
            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            val contentType = response.header("Content-Type") ?: ""
            recordPacket(
                method = method,
                url = url,
                statusCode = response.code,
                contentType = contentType,
                latencyMs = latency,
                sourceTag = "OkHttp Proxy"
            )
            response
        } catch (e: Exception) {
            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            recordPacket(
                method = method,
                url = url,
                statusCode = 0,
                contentType = "Error: ${e.javaClass.simpleName}",
                latencyMs = latency,
                sourceTag = "OkHttp Proxy"
            )
            throw e
        }
    }
}
