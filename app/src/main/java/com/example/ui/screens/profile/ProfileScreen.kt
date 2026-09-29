package com.example.ui.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CrimsonNeon
import com.example.ui.theme.CyanGlow
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
    modifier: Modifier = Modifier
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val prefs = user.preferences

    var showQualityMenu by remember { mutableStateOf(false) }
    var showAudioMenu by remember { mutableStateOf(false) }
    var showSubMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile_user_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.username,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.username,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified VIP",
                                    tint = CyanGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = user.email,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(CrimsonNeon.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = user.tier,
                                    color = CrimsonNeon,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row: Episodes & Watch Time
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${user.episodesWatched}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Episodes Watched", color = TextMuted, fontSize = 11.sp)
                        }
                        Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.DarkGray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = String.format("%.1fh", user.watchTimeHours), color = CrimsonNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Watch Time", color = TextMuted, fontSize = 11.sp)
                        }
                        Box(modifier = Modifier.size(1.dp, 30.dp).background(Color.DarkGray))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = user.joinDate, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Member Since", color = TextMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Admin Access Card
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = VioletAccent.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onAdminClick)
                    .testTag("profile_admin_portal_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(VioletAccent, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Platform Admin Dashboard", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Analytics, Content Catalog, Transcoding & Moderation", color = TextSecondary, fontSize = 11.sp)
                    }

                    Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = VioletAccent, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Playback & Video Preferences
        item {
            Text(text = "Playback & Stream Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Default Quality
        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showQualityMenu = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, contentDescription = null, tint = CrimsonNeon)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Default Video Quality", color = TextPrimary, fontSize = 14.sp)
                        }
                        Text(text = prefs.defaultQuality, color = CrimsonNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        DropdownMenu(
                            expanded = showQualityMenu,
                            onDismissRequest = { showQualityMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            listOf("Auto", "1080p", "720p", "480p").forEach { q ->
                                DropdownMenuItem(
                                    text = { Text(text = q, color = TextPrimary) },
                                    onClick = {
                                        viewModel.updateDefaultQuality(q)
                                        showQualityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Auto-Next Episode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Auto-Next Episode", color = TextPrimary, fontSize = 14.sp)
                            Text(text = "Play subsequent episode when current finishes", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = prefs.autoNextEpisode,
                            onCheckedChange = { viewModel.toggleAutoNext(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonNeon)
                        )
                    }

                    // Skip Intro Automatically Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Auto-Skip Opening Themes", color = TextPrimary, fontSize = 14.sp)
                            Text(text = "Automatically jump past anime intro sequences", color = TextMuted, fontSize = 11.sp)
                        }
                        Switch(
                            checked = prefs.skipIntro,
                            onCheckedChange = { viewModel.toggleSkipIntro(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = CrimsonNeon)
                        )
                    }
                }
            }
        }

        // Language & Audio Preferences
        item {
            Text(text = "Audio & Subtitles", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAudioMenu = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = CrimsonNeon)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Preferred Audio Track", color = TextPrimary, fontSize = 14.sp)
                        }
                        Text(text = prefs.preferredAudio, color = TextSecondary, fontSize = 12.sp)

                        DropdownMenu(
                            expanded = showAudioMenu,
                            onDismissRequest = { showAudioMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            listOf("Japanese [Original]", "English [Dub]", "Hindi [Dub]", "Bangla [Dub]", "Spanish [Dub]").forEach { aud ->
                                DropdownMenuItem(
                                    text = { Text(text = aud, color = TextPrimary) },
                                    onClick = {
                                        viewModel.updatePreferredAudio(aud)
                                        showAudioMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSubMenu = true }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Subtitles, contentDescription = null, tint = CrimsonNeon)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Preferred Subtitles", color = TextPrimary, fontSize = 14.sp)
                        }
                        Text(text = prefs.preferredSubtitle, color = TextSecondary, fontSize = 12.sp)

                        DropdownMenu(
                            expanded = showSubMenu,
                            onDismissRequest = { showSubMenu = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            listOf("English", "Japanese", "Spanish", "French", "Bangla", "Hindi", "Arabic").forEach { sub ->
                                DropdownMenuItem(
                                    text = { Text(text = sub, color = TextPrimary) },
                                    onClick = {
                                        viewModel.updatePreferredSubtitle(sub)
                                        showSubMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
