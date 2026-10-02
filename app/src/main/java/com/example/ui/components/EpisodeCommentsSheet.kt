package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.EpisodeCommentEntity
import com.example.data.repository.CommentsRepository
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
fun EpisodeCommentsSheet(
    animeId: String,
    episodeNumber: Int,
    commentsRepository: CommentsRepository,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val comments by commentsRepository.getComments(animeId, episodeNumber).collectAsStateWithLifecycle(emptyList())
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var newCommentText by remember { mutableStateOf("") }
    var isSpoilerPost by remember { mutableStateOf(false) }
    val revealedSpoilers = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(animeId, episodeNumber) {
        commentsRepository.seedInitialCommentsIfEmpty(animeId, episodeNumber)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = null,
                        tint = CrimsonNeon,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Episode $episodeNumber Discussion",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "${comments.size} comments",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Episode Quick Reactions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionChip(emoji = "🔥", label = "Hype", count = 342) {
                    scope.launch { comments.firstOrNull()?.let { commentsRepository.react(it.id, "FIRE") } }
                }
                ReactionChip(emoji = "😭", label = "Sad", count = 189) {
                    scope.launch { comments.firstOrNull()?.let { commentsRepository.react(it.id, "CRY") } }
                }
                ReactionChip(emoji = "😱", label = "Shock", count = 520) {
                    scope.launch { comments.firstOrNull()?.let { commentsRepository.react(it.id, "SHOCK") } }
                }
                ReactionChip(emoji = "❤️", label = "Love", count = 812) {
                    scope.launch { comments.firstOrNull()?.let { commentsRepository.react(it.id, "LOVE") } }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(comments, key = { it.id }) { comment ->
                    val isRevealed = revealedSpoilers[comment.id] == true
                    CommentItem(
                        comment = comment,
                        isRevealed = isRevealed,
                        onRevealSpoiler = { revealedSpoilers[comment.id] = true },
                        onReact = { type ->
                            scope.launch { commentsRepository.react(comment.id, type) }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // New comment input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Join the discussion on Ep $episodeNumber...", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        singleLine = false,
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                scope.launch {
                                    commentsRepository.addComment(
                                        animeId = animeId,
                                        epNum = episodeNumber,
                                        userName = "Ayan",
                                        userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                                        text = newCommentText.trim(),
                                        isSpoiler = isSpoilerPost
                                    )
                                    newCommentText = ""
                                    isSpoilerPost = false
                                }
                            }
                        },
                        enabled = newCommentText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Post Comment",
                            tint = if (newCommentText.isNotBlank()) CrimsonNeon else TextMuted
                        )
                    }
                }

                // Spoiler Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Checkbox(
                        checked = isSpoilerPost,
                        onCheckedChange = { isSpoilerPost = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = CrimsonNeon,
                            uncheckedColor = TextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mark comment as spoiler",
                        color = if (isSpoilerPost) CrimsonNeon else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSpoilerPost) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReactionChip(emoji: String, label: String, count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = SurfaceVariantDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "$count", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CommentItem(
    comment: EpisodeCommentEntity,
    isRevealed: Boolean,
    onRevealSpoiler: () -> Unit,
    onReact: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(comment.userAvatar)
                        .crossfade(true)
                        .build(),
                    contentDescription = comment.userName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.userName,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                if (comment.isSpoiler) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(CrimsonNeon.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SPOILER",
                            color = CrimsonNeon,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (comment.isSpoiler && !isRevealed) {
                // Frosted Spoiler Barrier
                Surface(
                    onClick = onRevealSpoiler,
                    shape = RoundedCornerShape(6.dp),
                    color = SurfaceVariantDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CrimsonNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Comment contains spoiler — Tap to reveal",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Text(
                    text = comment.text,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reaction buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔥 ${comment.fireReactions}",
                    color = if (comment.userReaction == "FIRE") CrimsonNeon else TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onReact("FIRE") }
                )
                Text(
                    text = "😭 ${comment.cryReactions}",
                    color = if (comment.userReaction == "CRY") CrimsonNeon else TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onReact("CRY") }
                )
                Text(
                    text = "😱 ${comment.shockReactions}",
                    color = if (comment.userReaction == "SHOCK") CrimsonNeon else TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onReact("SHOCK") }
                )
                Text(
                    text = "❤️ ${comment.loveReactions}",
                    color = if (comment.userReaction == "LOVE") CrimsonNeon else TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onReact("LOVE") }
                )
            }
        }
    }
}
