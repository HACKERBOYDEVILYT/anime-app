package com.example.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.sync.CloudSyncStatus
import com.example.ui.components.RobiulBrandHeader
import com.example.ui.components.RsHackerEmblem
import com.example.ui.components.SecretAdminDialog
import com.example.ui.components.SecureAuthDialog
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceVariantDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VioletAccent
import com.example.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onAdminClick: () -> Unit,
    onScheduleClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onQuizClick: () -> Unit = {},
    onMalSyncClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val syncState by viewModel.cloudSyncManager.syncState.collectAsStateWithLifecycle()
    val activeSessions by viewModel.userRepository.activeSessions.collectAsStateWithLifecycle()
    val loginHistory by viewModel.userRepository.loginHistory.collectAsStateWithLifecycle()
    val characters by viewModel.gamificationRepository.characters.collectAsStateWithLifecycle()
    val favoriteCharacters = remember(characters) { characters.filter { it.isFavorite } }
    val prefs = user.preferences

    var showQualityMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubMenu by remember { mutableStateOf(false) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showVerifyEmailDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    var editUsername by remember(user.username) { mutableStateOf(user.username) }
    var editBio by remember(user.bio) { mutableStateOf(user.bio) }
    var editAvatarUrl by remember(user.avatarUrl) { mutableStateOf(user.avatarUrl) }

    var resetEmail by remember(user.email) { mutableStateOf(user.email) }
    var resetCode by remember { mutableStateOf("") }
    var newResetPassword by remember { mutableStateOf("") }
    var verificationCodeInput by remember { mutableStateOf("") }

    if (showAuthDialog) {
        SecureAuthDialog(
            onDismiss = { showAuthDialog = false },
            onLogin = { email, pass -> viewModel.login(email, pass) },
            onRegister = { uname, email, pass -> viewModel.register(uname, email, pass) }
        )
    }

    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Edit Profile, Username & Avatar", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("Username") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text("Bio") }
                    )
                    OutlinedTextField(
                        value = editAvatarUrl,
                        onValueChange = { editAvatarUrl = it },
                        label = { Text("Avatar Image URL") },
                        singleLine = true
                    )
                    Text("Quick Avatar Presets:", color = TextSecondary, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                            "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                            "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300"
                        ).forEachIndexed { idx, preset ->
                            FilterChip(
                                selected = editAvatarUrl == preset,
                                onClick = { editAvatarUrl = preset },
                                label = { Text("Avatar #${idx + 1}", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editUsername, editBio, editAvatarUrl)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showVerifyEmailDialog) {
        AlertDialog(
            onDismissRequest = { showVerifyEmailDialog = false },
            containerColor = SurfaceDark,
            title = { Text("📩 Verify Your Email Address", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Click 'Send Code' to generate a 6-digit verification code for ${user.email}.", color = TextSecondary, fontSize = 12.sp)
                    OutlinedButton(onClick = { viewModel.sendVerificationCode(user.email) }) {
                        Text("Send 6-Digit Verification Code", color = CyanAccent)
                    }
                    OutlinedTextField(
                        value = verificationCodeInput,
                        onValueChange = { verificationCodeInput = it },
                        label = { Text("6-Digit Code") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyEmailCode(user.email, verificationCodeInput)
                        showVerifyEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Verify Email")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVerifyEmailDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            }
        )
    }

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            containerColor = SurfaceDark,
            title = { Text("🔑 Forgot Password & Reset", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Account Email") },
                        singleLine = true
                    )
                    OutlinedButton(onClick = { viewModel.requestPasswordReset(resetEmail) }) {
                        Text("Request Password Reset Code", color = StarAmber)
                    }
                    OutlinedTextField(
                        value = resetCode,
                        onValueChange = { resetCode = it },
                        label = { Text("6-Digit Reset Code") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newResetPassword,
                        onValueChange = { newResetPassword = it },
                        label = { Text("New Password (min 6 chars)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmPasswordReset(resetEmail, resetCode, newResetPassword)
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                ) {
                    Text("Reset Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Delete Account Permanently?", color = CrimsonNeon, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently delete your account, revoke all active device sessions, and reset cloud sync tokens.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon)
                ) {
                    Text("Delete Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Brand Identity Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RobiulBrandHeader()
            }
        }

        // Status Feedback Banner
        if (statusBanner != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyanAccent.copy(alpha = 0.16f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyanAccent, RoundedCornerShape(12.dp))
                        .clickable { viewModel.clearStatusBanner() }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statusBanner ?: "",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Text("Dismiss", color = CyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 1. Complete User Profile & Gamification Header Card (Sections 1, 10, 20, 27)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                    .testTag("profile_user_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .border(2.dp, CrimsonNeon, CircleShape)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.username,
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (user.emailVerified) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Account",
                                        tint = CyanGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${user.email} • ${user.authProvider}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // User Title (Newbie → Fan → Otaku → Elite → Master) & Streak Badge
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = CrimsonNeon.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🎖️ ${user.userTitle} • Lv.${user.level}",
                                        color = CrimsonNeon,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    color = StarAmber.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "🔥 ${user.currentStreakDays} Day Streak",
                                        color = StarAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = CyanAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = user.bio,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // XP Progress Bar & Followers / Following
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP: ${user.xpPoints} / ${(user.level) * 500} (Next Title Tier)",
                            color = CyanAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${user.followersCount} Followers • ${user.followingCount} Following",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { ((user.xpPoints % 500) / 500f).coerceIn(0.1f, 1f) },
                        color = CrimsonNeon,
                        trackColor = SurfaceVariantDark,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Complete Watch Statistics Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark, RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatColumn(value = "${user.episodesWatchedCount}", label = "Episodes")
                        ProfileStatColumn(value = "${user.hoursWatched}h", label = "Hours")
                        ProfileStatColumn(value = "${user.completedCount}", label = "Completed")
                        ProfileStatColumn(value = "⭐ ${user.meanScore}", label = "Mean Score")
                        ProfileStatColumn(value = "${user.favoritesCount}", label = "Favorites")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auth Actions Row (Login/Register, Google Sign-In, Verify Email, Forgot Password, Logout)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isLoggedIn) viewModel.logout() else showAuthDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isLoggedIn) SurfaceVariantDark else CrimsonNeon
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_auth_toggle_btn")
                        ) {
                            Text(
                                text = if (isLoggedIn) "Sign Out" else "Sign In / Register",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { viewModel.signInWithGoogle("robiul.google@gmail.com", "Robiul Google") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Google Sign-In", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showVerifyEmailDialog = true }) {
                            Text(
                                text = if (user.emailVerified) "✓ Email Verified" else "Verify Email",
                                color = if (user.emailVerified) EmeraldSuccess else StarAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        TextButton(onClick = { showForgotPasswordDialog = true }) {
                            Text("Forgot / Reset Password", color = TextSecondary, fontSize = 12.sp)
                        }
                        TextButton(onClick = { showDeleteConfirmDialog = true }) {
                            Text("Delete Account", color = CrimsonNeon, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Cloud Sync Control Center (Section 2: SYNCED / SYNCING / OFFLINE / SYNC ERROR)
        item {
            val statusColor = when (syncState.status) {
                CloudSyncStatus.SYNCED -> EmeraldSuccess
                CloudSyncStatus.SYNCING -> CyanAccent
                CloudSyncStatus.OFFLINE -> StarAmber
                CloudSyncStatus.SYNC_ERROR -> CrimsonNeon
            }
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, statusColor.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = statusColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("☁️ Real-Time Cloud Sync Engine", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${syncState.lastSyncedLabel} • ${syncState.syncedDomainsCount} Domains",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = syncState.status.displayLabel,
                                color = statusColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Text(
                        text = "Synchronizes: Watch history • Continue Watching • Watchlist • Favorites • Ratings • Reviews • Settings • Episode progress • MAL/AniList • Downloads • Custom Collections",
                        color = TextMuted,
                        fontSize = 11.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.cloudSyncManager.performInitialSync(user.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sync Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.cloudSyncManager.setNetworkOnline(!syncState.isOnline, user.id)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (!syncState.isOnline) "Go Online & Flush" else "Test Offline Queue",
                                color = StarAmber,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Multi-Device Session Management, 2FA & Login History (Section 1)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = CyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔐 Active Devices & Security Sessions", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { viewModel.logoutFromAllDevices() }) {
                            Text("Revoke All Others", color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 2FA Toggle & Refresh Token Rotation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Two-Factor Authentication (2FA TOTP)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Protect sign-ins with 6-digit TOTP & suspicious IP detection", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = user.twoFactorEnabled,
                            onCheckedChange = { viewModel.toggleTwoFactor(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldSuccess)
                        )
                    }

                    activeSessions.forEach { session ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(session.deviceName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (session.isCurrentDevice) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("• THIS DEVICE", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Text(
                                    text = "${session.platform} • ${session.locationOrIp} • ${session.lastActiveLabel}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (!session.isCurrentDevice) {
                                TextButton(onClick = { viewModel.revokeSession(session.sessionId) }) {
                                    Text("Revoke", color = CrimsonNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                TextButton(onClick = { viewModel.rotateRefreshToken() }) {
                                    Text("Rotate Token", color = CyanAccent, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Recent Login History & Suspicious Login Detection
                    Text("Recent Login Audit & Suspicious Login Detection:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    loginHistory.take(3).forEach { log ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                if (log.isSuspicious) {
                                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = CrimsonNeon, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = "${log.deviceName} (${log.location})",
                                    color = if (log.isSuspicious) CrimsonNeon else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (log.isSuspicious) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(log.statusNote, color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 4. Badges, Achievements & Favorite Characters (Section 10)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🏅 Unlocked Badges & Favorite Characters", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(user.badges) { badge ->
                            Surface(
                                color = VioletAccent.copy(alpha = 0.18f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "🏆 $badge",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    if (favoriteCharacters.isNotEmpty()) {
                        Text("Favorite Characters (${favoriteCharacters.size}):", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(favoriteCharacters, key = { it.id }) { ch ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                                        .padding(8.dp)
                                ) {
                                    AsyncImage(
                                        model = ch.avatarUrl,
                                        contentDescription = ch.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(ch.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(ch.animeTitle, color = CyanAccent, fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Quick Feature Hub (Schedule, Offline Downloads, AI Anime Quiz, MAL / AniList Sync)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ecosystem & Discovery Tools",
                        color = CrimsonNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FeatureShortcutRow(
                        icon = Icons.Default.CalendarMonth,
                        title = "Simulcast Airing Schedule",
                        subtitle = "Weekly release calendar & episode countdowns",
                        tint = StarAmber,
                        onClick = onScheduleClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FeatureShortcutRow(
                        icon = Icons.Default.Download,
                        title = "Offline Downloads Manager",
                        subtitle = "Queue, pause/resume, Wi-Fi only & season downloads",
                        tint = CyanGlow,
                        onClick = onDownloadsClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FeatureShortcutRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Find My Perfect Anime Quiz",
                        subtitle = "Interactive mood & genre matcher",
                        tint = VioletAccent,
                        onClick = onQuizClick
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FeatureShortcutRow(
                        icon = Icons.Default.CloudSync,
                        title = "MyAnimeList & AniList 2-Way Sync",
                        subtitle = "Import & auto-update external anime trackers",
                        tint = CrimsonNeon,
                        onClick = onMalSyncClick
                    )
                }
            }
        }

        // 6. Playback, Audio Profile & Kids Mode / Content Controls (Sections 23, 24, 25)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Playback, Audio Profile & Kids/Content Controls",
                        color = CrimsonNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Default Stream Quality
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Default Stream Quality", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Preferred resolution on startup", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Box {
                            TextButton(onClick = { showQualityMenu = true }) {
                                Text(text = prefs.defaultQuality, color = CrimsonNeon, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = showQualityMenu,
                                onDismissRequest = { showQualityMenu = false },
                                modifier = Modifier.background(SurfaceVariantDark)
                            ) {
                                listOf("1080p", "720p", "480p", "360p").forEach { q ->
                                    DropdownMenuItem(
                                        text = { Text(q, color = TextPrimary) },
                                        onClick = {
                                            viewModel.updateDefaultQuality(q)
                                            showQualityMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preferred Audio Language (Japanese, English, Hindi, Bengali)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Audio Profile Preference", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Japanese, English, Hindi, or Bengali", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Box {
                            TextButton(onClick = { showAudioMenu = true }) {
                                Text(text = prefs.preferredAudioLanguage, color = CrimsonNeon, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = showAudioMenu,
                                onDismissRequest = { showAudioMenu = false },
                                modifier = Modifier.background(SurfaceVariantDark)
                            ) {
                                listOf("Japanese", "English", "Hindi", "Bengali").forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang, color = TextPrimary) },
                                        onClick = {
                                            viewModel.updatePreferredAudio(lang)
                                            showAudioMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preferred Subtitle Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = TextSecondary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Default Subtitle Track", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Includes native বাংলা (Bangla) subtitles", color = TextMuted, fontSize = 11.sp)
                            }
                        }

                        Box {
                            TextButton(onClick = { showSubMenu = true }) {
                                Text(text = prefs.preferredSubtitleLanguage, color = CrimsonNeon, fontWeight = FontWeight.Bold)
                            }
                            DropdownMenu(
                                expanded = showSubMenu,
                                onDismissRequest = { showSubMenu = false },
                                modifier = Modifier.background(SurfaceVariantDark)
                            ) {
                                listOf("English", "Bengali", "Hindi", "Off").forEach { sub ->
                                    DropdownMenuItem(
                                        text = { Text(sub, color = TextPrimary) },
                                        onClick = {
                                            viewModel.updatePreferredSubtitle(sub)
                                            showSubMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Auto-Skip Intro/Outro
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto-Skip Intro & Outro", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Automatically jump past opening/ending credits", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = prefs.autoSkipIntro,
                            onCheckedChange = { viewModel.toggleSkipIntro(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonNeon)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Spoiler-Free Mode (Section 10)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "🚨 Global Spoiler-Free Mode", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Blur spoiler reviews, comments & ending metadata", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = prefs.spoilerFreeMode,
                            onCheckedChange = { enabled ->
                                viewModel.updateAdvancedPreferences { it.copy(spoilerFreeMode = enabled) }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = StarAmber)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Kids Mode / Family Safe Profile (Section 24)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ChildCare, contentDescription = null, tint = EmeraldSuccess)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "👨‍👩‍👧 Kids / Family Safe Mode (PIN: ${prefs.kidsModePin})", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Age filtering (PG-13) & mature content restriction", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                        Switch(
                            checked = prefs.kidsModeEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateAdvancedPreferences {
                                    it.copy(kidsModeEnabled = enabled, matureContentRestricted = if (enabled) true else it.matureContentRestricted)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EmeraldSuccess)
                        )
                    }
                }
            }
        }

        // Hidden Developer / Admin Mode Gateway (Requires 5 rapid taps + Password + Biometric/PIN)
        item {
            var secretTapCount by remember { mutableIntStateOf(0) }
            var lastTapTimestamp by remember { mutableLongStateOf(0L) }
            var showSecretAuthDialog by remember { mutableStateOf(false) }

            if (showSecretAuthDialog) {
                SecretAdminDialog(
                    onDismiss = {
                        showSecretAuthDialog = false
                        secretTapCount = 0
                    },
                    onSuccess = {
                        showSecretAuthDialog = false
                        secretTapCount = 0
                        onAdminClick()
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTimestamp > 2500L) {
                                secretTapCount = 1
                            } else {
                                secretTapCount++
                            }
                            lastTapTimestamp = now
                            if (secretTapCount >= 5) {
                                showSecretAuthDialog = true
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("hidden_admin_version_trigger")
                ) {
                    RsHackerEmblem(size = 24.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (secretTapCount in 2..4) {
                            "ROBIUL STREAM v2.5.0 (${5 - secretTapCount} taps to Admin Vault)"
                        } else {
                            "ROBIUL STREAM v2.5.0 • Security Core Active"
                        },
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = CrimsonNeon,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun FeatureShortcutRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
