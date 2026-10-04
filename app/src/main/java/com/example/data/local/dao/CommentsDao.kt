package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.EpisodeCommentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentsDao {
    @Query("SELECT * FROM episode_comments WHERE animeId = :animeId AND episodeNumber = :epNum ORDER BY timestamp DESC")
    fun getCommentsForEpisode(animeId: String, epNum: Int): Flow<List<EpisodeCommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: EpisodeCommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<EpisodeCommentEntity>)

    @Query("UPDATE episode_comments SET fireReactions = fireReactions + :delta, userReaction = :userReaction WHERE id = :commentId")
    suspend fun updateFire(commentId: String, delta: Int, userReaction: String?)

    @Query("UPDATE episode_comments SET cryReactions = cryReactions + :delta, userReaction = :userReaction WHERE id = :commentId")
    suspend fun updateCry(commentId: String, delta: Int, userReaction: String?)

    @Query("UPDATE episode_comments SET shockReactions = shockReactions + :delta, userReaction = :userReaction WHERE id = :commentId")
    suspend fun updateShock(commentId: String, delta: Int, userReaction: String?)

    @Query("UPDATE episode_comments SET loveReactions = loveReactions + :delta, userReaction = :userReaction WHERE id = :commentId")
    suspend fun updateLove(commentId: String, delta: Int, userReaction: String?)

    @Query("DELETE FROM episode_comments WHERE userName IN ('KuroFan99', 'OtakuSenpai', 'SakuraBlossom')")
    suspend fun deleteFakeDemoComments()
}
