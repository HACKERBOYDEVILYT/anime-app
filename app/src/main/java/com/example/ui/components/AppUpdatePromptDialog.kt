package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.repository.RepositoryUpdateState
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
 * Automatic Repository Update Prompt Dialog.
 * Appears automatically when a new version or commit is pushed to the GitHub repository.
 */
@Composable
fun AppUpdatePromptDialog(
    updateState: RepositoryUpdateState,
    onUpdateNowClick: () -> Unit,
    onOpenWebPortalClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            if (!updateState.forceUpdate) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !updateState.forceUpdate,
            dismissOnClickOutside = !updateState.forceUpdate
        )
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    brush = Brush.horizontalGradient(listOf(CrimsonNeon, VioletAccent, CyanGlow)),
                    shape = RoundedCornerShape(18.dp)
                )
                .testTag("repo_auto_update_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Badge & Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(CrimsonNeon.copy(alpha = 0.18f), CircleShape)
                                .border(1.dp, CrimsonNeon, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdateAlt,
                                contentDescription = null,
                                tint = CrimsonNeon,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Repository Update Available!",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "GitHub Repo: ${updateState.repositorySlug}",
                                color = CyanGlow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (updateState.forceUpdate) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonNeon.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "REQUIRED",
                                color = CrimsonNeon,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Version Comparison Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Installed Version", color = TextMuted, fontSize = 10.sp)
                        Text(
                            text = "v${updateState.installedVersionName} (#${updateState.installedVersionCode})",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(text = "➔", color = CrimsonNeon, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "New Repo Version", color = StarAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "v${updateState.latestVersionName} (#${updateState.latestVersionCode})",
                            color = Color(0xFF00E676),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Changelog Box
                Text(
                    text = "What's New in Repository & Live Website:",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = updateState.changelog,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action: Download & Update App Now
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateState.apkDownloadUrl))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                        onUpdateNowClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonNeon),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("repo_update_now_btn")
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Update App Now (v${updateState.latestVersionName})",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Row: Open Unified Website + Later
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenWebPortalClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = CyanGlow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open Live Website",
                            color = CyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (!updateState.forceUpdate) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("repo_update_later_btn")
                        ) {
                            Text(
                                text = "Later",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
