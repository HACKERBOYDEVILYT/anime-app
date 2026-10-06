package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.network.CatalogNetworkNotificationState
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Visual Notification & Offline Mode Indicator Component:
 * 1. Offline Mode Indicator: Checks `navigator.onLine` (and Android ConnectivityManager) and alerts the user
 *    if the app has lost connection while attempting to fetch catalog data.
 * 2. Fetch Failure Visual Notification: Automatically appears with an error message when the application
 *    detects a catalog fetch failure, offering a prominent 'Retry' button to re-attempt the connection.
 */
@Composable
fun CatalogNetworkNotificationBanner(
    networkState: CatalogNetworkNotificationState,
    externalErrorMessage: String? = null,
    onRetryClick: () -> Unit,
    onDismissError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val showOfflineBanner = !networkState.navigatorOnLine || networkState.showOfflineModeIndicator
    val resolvedErrorMessage = networkState.fetchFailureMessage ?: externalErrorMessage
    val showErrorNotification = networkState.showFetchFailureNotification || !resolvedErrorMessage.isNullOrBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Offline Mode Indicator (navigator.onLine == false)
        AnimatedVisibility(
            visible = showOfflineBanner,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF23180B)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, StarAmber.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .testTag("offline_mode_indicator")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(StarAmber.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = "Offline Mode Indicator",
                                tint = StarAmber,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "OFFLINE MODE",
                                    color = StarAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Surface(
                                    color = StarAmber.copy(alpha = 0.18f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "navigator.onLine: false",
                                        color = StarAmber,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = networkState.offlineAlertMessage,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StarAmber,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 64.dp, minHeight = 48.dp)
                            .testTag("offline_reconnect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reconnect",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Retry",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // 2. Visual Fetch Failure Notification Component with 'Retry' Button
        AnimatedVisibility(
            visible = showErrorNotification,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF261019)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, CrimsonNeon.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                    .testTag("fetch_failure_notification")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CrimsonNeon.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (!networkState.navigatorOnLine) Icons.Default.CloudOff else Icons.Default.ErrorOutline,
                                    contentDescription = "Catalog Fetch Failure",
                                    tint = CrimsonNeon,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "CATALOG FETCH FAILURE",
                                        color = CrimsonNeon,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "• ${networkState.lastCheckedTimestamp}",
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = resolvedErrorMessage
                                        ?: "Failed to fetch anime catalog data. Check your network connection and tap Retry.",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.testTag("fetch_failure_error_message")
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismissError,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dismiss_fetch_error_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss notification",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Endpoint: ${networkState.failedSourceEndpoint}",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Button(
                            onClick = onRetryClick,
                            enabled = !networkState.isRetrying,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonNeon,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier
                                .defaultMinSize(minWidth = 96.dp, minHeight = 48.dp)
                                .testTag("catalog_fetch_retry_button")
                        ) {
                            if (networkState.isRetrying) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retrying...",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry catalog connection",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Retry",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Compact header pill showing real-time `navigator.onLine` status and allowing one-tap
 * offline simulation / connection verification.
 */
@Composable
fun NavigatorOnlineStatusPill(
    networkState: CatalogNetworkNotificationState,
    onToggleOfflineTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = networkState.navigatorOnLine && !networkState.showOfflineModeIndicator
    val pillColor = if (isOnline) EmeraldSuccess else StarAmber

    Surface(
        color = pillColor.copy(alpha = 0.14f),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clickable { onToggleOfflineTest() }
            .testTag("navigator_online_status_pill")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = if (isOnline) "Online" else "Offline",
                tint = pillColor,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (isOnline) "ONLINE" else "OFFLINE",
                color = pillColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
