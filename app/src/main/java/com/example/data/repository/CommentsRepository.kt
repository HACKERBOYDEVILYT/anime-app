package com.example.data.repository

import com.example.data.local.dao.CommentsDao
import com.example.data.local.entity.EpisodeCommentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

class CommentsRepository(
    private val commentsDao: CommentsDao
) {
    fun getComments(animeId: String, epNum: Int): Flow<List<EpisodeCommentEntity>> =
        commentsDao.getCommentsForEpisode(animeId, epNum)

    suspend fun seedInitialCommentsIfEmpty(animeId: String, epNum: Int) = withContext(Dispatchers.IO) {
        // Purge any legacy fake demo comments; only real user comments are stored
        commentsDao.deleteFakeDemoComments()
    }

    suspend fun addComment(
        animeId: String,
        epNum: Int,
        userName: String,
        userAvatar: String,
        text: String,
        isSpoiler: Boolean
    ) = withContext(Dispatchers.IO) {
        val comment = EpisodeCommentEntity(
            id = UUID.randomUUID().toString(),
            animeId = animeId,
            episodeNumber = epNum,
            userName = userName,
            userAvatar = userAvatar,
            text = text,
            isSpoiler = isSpoiler,
            fireReactions = 0,
            cryReactions = 0,
            shockReactions = 0,
            loveReactions = 0,
            timestamp = System.currentTimeMillis()
        )
        commentsDao.insertComment(comment)
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
