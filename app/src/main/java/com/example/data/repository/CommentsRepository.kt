package com.example.data.repository

import com.example.data.local.dao.CommentsDao
import com.example.data.local.entity.EpisodeCommentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class CommentsRepository(
    private val commentsDao: CommentsDao
) {
    fun getComments(animeId: String, epNum: Int): Flow<List<EpisodeCommentEntity>> =
        commentsDao.getCommentsForEpisode(animeId, epNum)

    suspend fun seedInitialCommentsIfEmpty(animeId: String, epNum: Int) = withContext(Dispatchers.IO) {
        val existing = commentsDao.getCommentsForEpisode(animeId, epNum).firstOrNull()
        if (existing.isNullOrEmpty()) {
            val sampleComments = listOf(
                EpisodeCommentEntity(
                    id = UUID.randomUUID().toString(),
                    animeId = animeId,
                    episodeNumber = epNum,
                    userName = "KuroFan99",
                    userAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
                    text = "The animation during the climax battle was absolute cinema! Madhouse / MAPPA never misses! 🔥",
                    isSpoiler = false,
                    fireReactions = 45,
                    cryReactions = 2,
                    shockReactions = 18,
                    loveReactions = 62
                ),
                EpisodeCommentEntity(
                    id = UUID.randomUUID().toString(),
                    animeId = animeId,
                    episodeNumber = epNum,
                    userName = "OtakuSenpai",
                    userAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120",
                    text = "That plot twist near the ending completely blew my mind! Did NOT see that betrayal coming at all! 😱",
                    isSpoiler = true,
                    fireReactions = 28,
                    cryReactions = 7,
                    shockReactions = 84,
                    loveReactions = 19
                ),
                EpisodeCommentEntity(
                    id = UUID.randomUUID().toString(),
                    animeId = animeId,
                    episodeNumber = epNum,
                    userName = "SakuraBlossom",
                    userAvatar = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120",
                    text = "The soundtrack when they said their final goodbyes made me tear up so hard... 10/10 episode! 😭❤️",
                    isSpoiler = false,
                    fireReactions = 14,
                    cryReactions = 95,
                    shockReactions = 5,
                    loveReactions = 110
                )
            )
            commentsDao.insertAll(sampleComments)
        }
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
            fireReactions = 1,
            cryReactions = 0,
            shockReactions = 0,
            loveReactions = 1,
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
