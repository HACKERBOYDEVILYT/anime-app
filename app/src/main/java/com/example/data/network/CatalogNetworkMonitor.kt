package com.example.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList

data class CatalogNetworkNotificationState(
    val navigatorOnLine: Boolean = true,
    val isSimulatedOffline: Boolean = false,
    val showOfflineModeIndicator: Boolean = false,
    val lostConnectionDuringFetch: Boolean = false,
    val offlineAlertMessage: String = "Offline Mode Active (navigator.onLine = false): Connection lost while attempting to fetch catalog data. Using cached catalog.",
    val showFetchFailureNotification: Boolean = false,
    val fetchFailureMessage: String? = null,
    val failedSourceEndpoint: String = "Remote Anime Catalog API",
    val isRetrying: Boolean = false,
    val retryAttemptCount: Int = 0,
    val lastCheckedTimestamp: String = "Just now"
)

/**
 * Monitors real-time network connectivity (equivalent to `navigator.onLine` + Android ConnectivityManager)
 * and tracks catalog fetch failures across Jikan, AnimeThemes, AniList, and KuroAPI endpoints.
 *
 * - Alerts the user with an Offline Mode Indicator if the app loses connection while attempting to fetch catalog data.
 * - Automatically surfaces a visual notification component with an error message when a catalog fetch failure
 *   is detected, offering a 'Retry' button to re-attempt the connection.
 */
class CatalogNetworkMonitor(context: Context? = null) {

    private val appContext: Context? = context?.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val connectivityManager: ConnectivityManager? =
        appContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val retryListeners = CopyOnWriteArrayList<suspend () -> Unit>()

    private val _state = MutableStateFlow(
        CatalogNetworkNotificationState(
            navigatorOnLine = queryDeviceOnlineStatus(false),
            showOfflineModeIndicator = !queryDeviceOnlineStatus(false)
        )
    )
    val state: StateFlow<CatalogNetworkNotificationState> = _state.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun formatNow(): String {
        return SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
    }

    private fun queryDeviceOnlineStatus(simulatedOffline: Boolean): Boolean {
        if (simulatedOffline) return false
        val cm = connectivityManager ?: return true
        return try {
            val activeNetwork = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Checks `navigator.onLine` (real Android network state + simulated offline override).
     */
    fun checkNavigatorOnLine(): Boolean {
        val online = queryDeviceOnlineStatus(_state.value.isSimulatedOffline)
        _state.update { current ->
            current.copy(
                navigatorOnLine = online,
                showOfflineModeIndicator = !online || current.lostConnectionDuringFetch
            )
        }
        return online
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(
                request,
                object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val online = queryDeviceOnlineStatus(_state.value.isSimulatedOffline)
                        if (online) {
                            _state.update { current ->
                                current.copy(
                                    navigatorOnLine = true,
                                    showOfflineModeIndicator = false,
                                    lostConnectionDuringFetch = false,
                                    lastCheckedTimestamp = formatNow()
                                )
                            }
                        }
                    }

                    override fun onLost(network: Network) {
                        val online = queryDeviceOnlineStatus(_state.value.isSimulatedOffline)
                        if (!online) {
                            _state.update { current ->
                                current.copy(
                                    navigatorOnLine = false,
                                    showOfflineModeIndicator = true,
                                    offlineAlertMessage = "Offline Mode (navigator.onLine = false): Network connection lost. Catalog fetches paused.",
                                    lastCheckedTimestamp = formatNow()
                                )
                            }
                        }
                    }

                    override fun onCapabilitiesChanged(
                        network: Network,
                        networkCapabilities: NetworkCapabilities
                    ) {
                        val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            !_state.value.isSimulatedOffline
                        _state.update { current ->
                            current.copy(
                                navigatorOnLine = hasInternet,
                                showOfflineModeIndicator = !hasInternet || current.lostConnectionDuringFetch,
                                lastCheckedTimestamp = formatNow()
                            )
                        }
                    }
                }
            )
        } catch (_: Exception) {
            // Ignore security or registration exceptions on restricted environments
        }
    }

    /**
     * Called right before attempting to fetch catalog data.
     * Checks `navigator.onLine` and alerts the user if the app has lost connection while attempting to fetch catalog data.
     */
    fun verifyConnectionBeforeCatalogFetch(sourceLabel: String = "Anime Catalog API"): Boolean {
        val isOnline = checkNavigatorOnLine()
        if (!isOnline) {
            _state.update { current ->
                current.copy(
                    navigatorOnLine = false,
                    showOfflineModeIndicator = true,
                    lostConnectionDuringFetch = true,
                    offlineAlertMessage = "Offline Mode Alert (navigator.onLine = false): Lost connection while attempting to fetch catalog data ($sourceLabel).",
                    showFetchFailureNotification = true,
                    failedSourceEndpoint = sourceLabel,
                    fetchFailureMessage = "Connection Lost: Unable to fetch catalog data from $sourceLabel because device is offline (navigator.onLine = false). Tap Retry to reconnect.",
                    isRetrying = false,
                    lastCheckedTimestamp = formatNow()
                )
            }
        }
        return isOnline
    }

    /**
     * Called when the application detects a fetch failure while retrieving catalog data.
     * Automatically displays the visual error notification component with a 'Retry' button.
     */
    fun reportCatalogFetchFailure(
        sourceLabel: String,
        errorDetail: String? = null
    ) {
        val isOnline = checkNavigatorOnLine()
        val cleanReason = errorDetail?.takeIf { it.isNotBlank() }
            ?: if (!isOnline) "Device is offline (navigator.onLine = false)" else "Upstream catalog server unreachable or timed out"

        _state.update { current ->
            current.copy(
                navigatorOnLine = isOnline,
                showOfflineModeIndicator = !isOnline || current.lostConnectionDuringFetch,
                lostConnectionDuringFetch = !isOnline,
                offlineAlertMessage = if (!isOnline) {
                    "Offline Mode Alert (navigator.onLine = false): Lost connection while attempting to fetch catalog data ($sourceLabel)."
                } else {
                    current.offlineAlertMessage
                },
                showFetchFailureNotification = true,
                failedSourceEndpoint = sourceLabel,
                fetchFailureMessage = "Catalog Fetch Failed ($sourceLabel): $cleanReason. Tap 'Retry' to re-attempt the connection.",
                isRetrying = false,
                lastCheckedTimestamp = formatNow()
            )
        }
    }

    /**
     * Called when catalog data is successfully fetched from remote servers.
     */
    fun reportCatalogFetchSuccess() {
        val isOnline = checkNavigatorOnLine()
        if (isOnline) {
            _state.update { current ->
                current.copy(
                    navigatorOnLine = true,
                    showOfflineModeIndicator = false,
                    lostConnectionDuringFetch = false,
                    showFetchFailureNotification = false,
                    fetchFailureMessage = null,
                    isRetrying = false,
                    lastCheckedTimestamp = formatNow()
                )
            }
        }
    }

    /**
     * Registers a callback invoked when the user clicks the 'Retry' button on the notification component.
     */
    fun registerRetryListener(listener: suspend () -> Unit) {
        retryListeners.add(listener)
    }

    /**
     * Re-attempts the catalog connection when the user clicks the 'Retry' button.
     */
    fun retryCatalogConnection(customRetryBlock: (suspend () -> Unit)? = null) {
        scope.launch {
            _state.update { current ->
                current.copy(
                    isRetrying = true,
                    retryAttemptCount = current.retryAttemptCount + 1,
                    lastCheckedTimestamp = formatNow()
                )
            }
            delay(350)

            // If user was in simulated offline mode, restore online connection on explicit Retry or re-verify
            if (_state.value.isSimulatedOffline) {
                _state.update { it.copy(isSimulatedOffline = false) }
            }

            val onlineNow = checkNavigatorOnLine()
            if (!onlineNow) {
                _state.update { current ->
                    current.copy(
                        navigatorOnLine = false,
                        showOfflineModeIndicator = true,
                        lostConnectionDuringFetch = true,
                        showFetchFailureNotification = true,
                        fetchFailureMessage = "Retry #${current.retryAttemptCount} Failed: Still offline (navigator.onLine = false). Check your Wi-Fi/Mobile Data and tap Retry.",
                        isRetrying = false,
                        lastCheckedTimestamp = formatNow()
                    )
                }
                return@launch
            }

            try {
                if (customRetryBlock != null) {
                    customRetryBlock.invoke()
                }
                for (listener in retryListeners) {
                    listener.invoke()
                }
                reportCatalogFetchSuccess()
            } catch (e: Exception) {
                reportCatalogFetchFailure(_state.value.failedSourceEndpoint, e.localizedMessage)
            }
        }
    }

    /**
     * Allows simulating an offline drop (`navigator.onLine = false`) during a catalog fetch
     * so users can verify the Offline Mode Indicator and Fetch Failure Retry Notification.
     */
    fun toggleSimulatedOfflineCatalogFailure() {
        val nextOffline = !_state.value.isSimulatedOffline
        if (nextOffline) {
            _state.update { it.copy(isSimulatedOffline = true) }
            verifyConnectionBeforeCatalogFetch("Jikan v4 & AnimeThemes Catalog API")
        } else {
            _state.update { it.copy(isSimulatedOffline = false) }
            retryCatalogConnection()
        }
    }

    fun dismissFetchNotification() {
        _state.update {
            it.copy(
                showFetchFailureNotification = false,
                fetchFailureMessage = null
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CatalogNetworkMonitor? = null

        fun getInstance(context: Context? = null): CatalogNetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                val instance = CatalogNetworkMonitor(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
