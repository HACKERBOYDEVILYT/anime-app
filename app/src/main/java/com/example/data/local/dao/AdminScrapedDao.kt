package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AdMobConfigEntity
import com.example.data.local.entity.ApiEndpointEntity
import com.example.data.local.entity.ScrapedVideoEntity
import com.example.data.local.entity.UserAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminScrapedDao {
    // Scraped Videos & Free Storage Streams
    @Query("SELECT * FROM scraped_videos ORDER BY createdAt DESC")
    fun getAllScrapedVideos(): Flow<List<ScrapedVideoEntity>>

    @Query("SELECT * FROM scraped_videos WHERE animeId = :animeId ORDER BY episodeNumber ASC")
    suspend fun getScrapedVideosForAnime(animeId: String): List<ScrapedVideoEntity>

    @Query("SELECT * FROM scraped_videos WHERE animeId = :animeId AND episodeNumber = :episodeNumber")
    suspend fun getScrapedVideosForEpisode(animeId: String, episodeNumber: Int): List<ScrapedVideoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScrapedVideo(entity: ScrapedVideoEntity)

    @Query("DELETE FROM scraped_videos WHERE id = :id")
    suspend fun deleteScrapedVideo(id: String)

    // Real User Accounts (No Fake Demo Accounts)
    @Query("SELECT * FROM user_accounts ORDER BY username ASC")
    fun getAllUserAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE isActiveSession = 1 LIMIT 1")
    suspend fun getActiveSessionUser(): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity)

    @Query("UPDATE user_accounts SET isActiveSession = 0")
    suspend fun clearAllActiveSessions()

    @Query("UPDATE user_accounts SET isActiveSession = 1 WHERE email = :email")
    suspend fun setActiveSession(email: String)

    // Multi-Server API Endpoints
    @Query("SELECT * FROM api_endpoints")
    fun getAllApiEndpoints(): Flow<List<ApiEndpointEntity>>

    @Query("SELECT * FROM api_endpoints")
    suspend fun getAllApiEndpointsOnce(): List<ApiEndpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiEndpoint(endpoint: ApiEndpointEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllApiEndpoints(endpoints: List<ApiEndpointEntity>)

    @Query("DELETE FROM api_endpoints WHERE id = :id")
    suspend fun deleteApiEndpoint(id: String)

    // Google AdMob Account Configuration
    @Query("SELECT * FROM admob_config WHERE id = 'primary_admob_account' LIMIT 1")
    fun getAdMobConfig(): Flow<AdMobConfigEntity?>

    @Query("SELECT * FROM admob_config WHERE id = 'primary_admob_account' LIMIT 1")
    suspend fun getAdMobConfigOnce(): AdMobConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAdMobConfig(config: AdMobConfigEntity)
}
