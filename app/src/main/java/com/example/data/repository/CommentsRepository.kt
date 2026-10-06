package com.example.data.repository

import com.example.data.local.dao.CommentsDao
import com.example.data.local.entity.EpisodeCommentEntity
import com.example.security.AuthSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.UUID

data class CommentReply(
    val id: String,
    val parentCommentId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val isSpoiler: Boolean = false,
    val likesCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class CommentsRepository(
    private val commentsDao: CommentsDao
) {
    private val spoilerKeywords = listOf(
        "spoiler", "dies", "death", "dead", "killed", "ending", "final boss", "betray", "plot twist", "||", ">!"
    )

    private val _repliesByCommentId = MutableStateFlow<Map<String, List<CommentReply>>>(emptyMap())
    val repliesByCommentId: StateFlow<Map<String, List<CommentReply>>> = _repliesByCommentId.asStateFlow()

    fun getComments(animeId: String, epNum: Int): Flow<List<EpisodeCommentEntity>> =
        commentsDao.getCommentsForEpisode(animeId, epNum)

    suspend fun seedInitialCommentsIfEmpty(animeId: String, epNum: Int) = withContext(Dispatchers.IO) {
        commentsDao.deleteFakeDemoComments()
    }

    /**
     * Automatically detects spoiler phrases in comment text ("⚠️ SPOILER — Tap to reveal").
     */
    fun detectContainsSpoiler(text: String): Boolean {
        val lower = text.lowercase()
        return spoilerKeywords.any { lower.contains(it) }
    }

    suspend fun addComment(
        animeId: String,
        epNum: Int,
        userName: String,
        userAvatar: String,
        text: String,
        isSpoiler: Boolean
    ) = withContext(Dispatchers.IO) {
        val cleanText = AuthSecurityManager.sanitizeInput(text, 600)
        if (cleanText.isBlank()) return@withContext
        val autoSpoiler = isSpoiler || detectContainsSpoiler(cleanText)
        val comment = EpisodeCommentEntity(
            id = UUID.randomUUID().toString(),
            animeId = animeId,
            episodeNumber = epNum,
            userName = userName,
            userAvatar = userAvatar,
            text = cleanText,
            isSpoiler = autoSpoiler,
            fireReactions = 0,
            cryReactions = 0,
            shockReactions = 0,
            loveReactions = 0,
            timestamp = System.currentTimeMillis()
        )
        commentsDao.insertComment(comment)
    }

    fun addReply(parentCommentId: String, userName: String, userAvatar: String, text: String, isSpoiler: Boolean = false) {
        val cleanText = AuthSecurityManager.sanitizeInput(text, 400)
        if (cleanText.isBlank()) return
        val autoSpoiler = isSpoiler || detectContainsSpoiler(cleanText)
        val reply = CommentReply(
            id = "rep_${System.currentTimeMillis()}",
            parentCommentId = parentCommentId,
            userName = userName,
            userAvatar = userAvatar,
            text = cleanText,
            isSpoiler = autoSpoiler
        )
        _repliesByCommentId.update { map ->
            val current = map[parentCommentId].orEmpty()
            map + (parentCommentId to (current + reply))
        }
    }

    suspend fun react(commentId: String, reactionType: String) = withContext(Dispatchers.IO) {
        when (reactionType) {
            "FIRE" -> commentsDao.updateFire(commentId, 1, "FIRE")
            "CRY" -> commentsDao.updateCry(commentId, 1, "CRY")
            "SHOCK" -> commentsDao.updateShock(commentId, 1, "SHOCK")
            "LOVE" -> commentsDao.updateLove(commentId, 1, "LOVE")
        }
    }
}
