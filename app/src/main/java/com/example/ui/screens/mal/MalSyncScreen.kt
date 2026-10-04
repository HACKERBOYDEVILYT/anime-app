package com.example.ui.screens.mal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.MalSyncEntity
import com.example.data.repository.MalSyncRepository
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MalSyncScreen(
    malSyncRepository: MalSyncRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configs by malSyncRepository.getAllConfigs().collectAsStateWithLifecycle(emptyList())
    val malConfig = configs.find { it.serviceName == "MAL" }
    val anilistConfig = configs.find { it.serviceName == "ANILIST" }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showConnectDialogFor by remember { mutableStateOf<String?>(null) }
    var inputUsername by remember { mutableStateOf("") }
    var isVerifyingAccount by remember { mutableStateOf(false) }
    var errorBanner by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = CrimsonNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AniList & MAL Sync",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("mal_sync_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark,
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Connect your anime tracking profiles to automatically update watched episodes, import watchlists, and keep your history synchronized across devices.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // MyAnimeList Card
            TrackerCard(
                serviceName = "MyAnimeList (Official Jikan v4 API)",
                serviceKey = "MAL",
                brandColor = Color(0xFF2E51A2),
                config = malConfig,
                onConnectClick = {
                    inputUsername = ""
                    errorBanner = null
                    showConnectDialogFor = "MAL"
                },
                onDisconnectClick = {
                    scope.launch {
                        malSyncRepository.disconnectService("MAL")
                        snackbarHostState.showSnackbar("MyAnimeList disconnected")
                    }
                },
                onAutoSyncToggle = { enabled ->
                    if (malConfig != null) {
                        scope.launch { malSyncRepository.updateAutoSync("MAL", enabled, malConfig) }
                    }
                },
                onSyncNow = {
                    if (malConfig != null) {
                        scope.launch {
                            val res = malSyncRepository.syncNow("MAL", malConfig)
                            res.fold(
                                onSuccess = { updated ->
                                    snackbarHostState.showSnackbar(
                                        "Synced live from MyAnimeList (@${updated.username}): ${updated.totalAnimeTracked} Anime • ${updated.totalEpisodesWatched} Episodes"
                                    )
                                },
                                onFailure = { err ->
                                    snackbarHostState.showSnackbar(err.message ?: "Failed to sync with MyAnimeList API")
                                }
                            )
                        }
                    }
                }
            )

            // AniList Card
            TrackerCard(
                serviceName = "AniList (Official GraphQL API)",
                serviceKey = "ANILIST",
                brandColor = Color(0xFF02A9FF),
                config = anilistConfig,
                onConnectClick = {
                    inputUsername = ""
                    errorBanner = null
                    showConnectDialogFor = "ANILIST"
                },
                onDisconnectClick = {
                    scope.launch {
                        malSyncRepository.disconnectService("ANILIST")
                        snackbarHostState.showSnackbar("AniList disconnected")
                    }
                },
                onAutoSyncToggle = { enabled ->
                    if (anilistConfig != null) {
                        scope.launch { malSyncRepository.updateAutoSync("ANILIST", enabled, anilistConfig) }
                    }
                },
                onSyncNow = {
                    if (anilistConfig != null) {
                        scope.launch {
                            val res = malSyncRepository.syncNow("ANILIST", anilistConfig)
                            res.fold(
                                onSuccess = { updated ->
                                    snackbarHostState.showSnackbar(
                                        "Synced live from AniList (@${updated.username}): ${updated.totalAnimeTracked} Anime • ${updated.totalEpisodesWatched} Episodes"
                                    )
                                },
                                onFailure = { err ->
                                    snackbarHostState.showSnackbar(err.message ?: "Failed to sync with AniList GraphQL API")
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    if (showConnectDialogFor != null) {
        val service = showConnectDialogFor!!
        val displayName = if (service == "MAL") "MyAnimeList" else "AniList"

        AlertDialog(
            onDismissRequest = { if (!isVerifyingAccount) showConnectDialogFor = null },
            title = { Text("Connect Real $displayName Account", color = TextPrimary) },
            text = {
                Column {
                    Text(
                        "Enter your real $displayName username. Your live anime count, episodes watched, and mean score will be verified directly from the $displayName API (no fake demo stats).",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = inputUsername,
                        onValueChange = {
                            inputUsername = it
                            errorBanner = null
                        },
                        label = { Text("$displayName Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonNeon,
                            unfocusedBorderColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (errorBanner != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorBanner!!,
                            color = CrimsonNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputUsername.isNotBlank() && !isVerifyingAccount) {
                            isVerifyingAccount = true
                            errorBanner = null
                            scope.launch {
                                val res = malSyncRepository.connectService(service, inputUsername.trim())
                                isVerifyingAccount = false
                                res.fold(
                                    onSuccess = { entity ->
                                        showConnectDialogFor = null
                                        snackbarHostState.showSnackbar(
                                            "Verified @$displayName (${entity.username}): ${entity.totalAnimeTracked} Anime, ${entity.totalEpisodesWatched} Episodes"
                                        )
                                    },
                                    onFailure = { err ->
                                        errorBanner = err.message ?: "Account not found on $displayName."
                                    }
                                )
                            }
                        }
                    },
                    enabled = !isVerifyingAccount && inputUsername.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                ) {
                    Text(if (isVerifyingAccount) "Verifying Live API..." else "Verify & Connect", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isVerifyingAccount) showConnectDialogFor = null }
                ) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun TrackerCard(
    serviceName: String,
    serviceKey: String,
    brandColor: Color,
    config: MalSyncEntity?,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onSyncNow: () -> Unit
) {
    val isConnected = config != null && config.isConnected

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isConnected) brandColor.copy(alpha = 0.5f) else CardBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(brandColor.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = serviceName.take(2).uppercase(),
                            color = brandColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = serviceName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isConnected) "Connected as @${config?.username}" else "Not connected",
                            color = if (isConnected) Color(0xFF4CAF50) else TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                if (isConnected) {
                    IconButton(onClick = onDisconnectClick) {
                        Icon(
                            imageVector = Icons.Default.LinkOff,
                            contentDescription = "Disconnect",
                            tint = TextMuted
                        )
                    }
                }
            }

            if (isConnected) {
                Spacer(modifier = Modifier.height(14.dp))

                // Stats row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${config?.totalAnimeTracked ?: 0}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Anime", color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${config?.totalEpisodesWatched ?: 0}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Episodes", color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "${config?.meanScore ?: 0.0f}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Mean Score", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Auto-sync toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Auto-Sync Progress",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Update episode count upon completing an episode",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = config?.autoSyncEnabled ?: true,
                        onCheckedChange = onAutoSyncToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CrimsonNeon,
                            checkedTrackColor = CrimsonNeon.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSyncNow,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceVariantDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = CrimsonNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sync Now with $serviceName", color = TextPrimary, fontSize = 13.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConnectClick,
                    colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connect $serviceName Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
