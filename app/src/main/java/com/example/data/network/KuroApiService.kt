package com.example.data.network

import com.example.data.network.model.AnimeDto
import com.example.data.network.model.ApiResponse
import com.example.data.network.model.AuthResponse
import com.example.data.network.model.EpisodeDto
import com.example.data.network.model.LoginRequest
import com.example.data.network.model.PlaybackSessionResponse
import com.example.data.network.model.RegisterRequest
import com.example.data.network.model.ReviewCreateRequest
import com.example.data.network.model.ReviewDto
import com.example.data.network.model.StreamAuthorizationRequest
import com.example.data.network.model.UserDto
import com.example.data.network.model.WatchProgressDto
import com.example.data.network.model.WatchlistSyncDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit Service Interface defining all KuroStream REST API endpoints.
 */
interface KuroApiService {

    // ===================================
    // Anime Catalog & Discovery Endpoints
    // ===================================

    @GET("api/v1/anime/trending")
    suspend fun getTrendingAnime(
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/anime/popular")
    suspend fun getPopularAnime(
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/anime/top-rated")
    suspend fun getTopRatedAnime(
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/anime/seasonal")
    suspend fun getSeasonalAnime(
        @Query("season") season: String? = null,
        @Query("year") year: Int? = null
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/anime/recently-added")
    suspend fun getRecentlyAdded(
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/anime/{id}")
    suspend fun getAnimeById(
        @Path("id") id: String
    ): ApiResponse<AnimeDto>

    @GET("api/v1/anime/{id}/episodes")
    suspend fun getEpisodesForAnime(
        @Path("id") animeId: String
    ): ApiResponse<List<EpisodeDto>>

    @GET("api/v1/anime/{id}/recommendations")
    suspend fun getRecommendations(
        @Path("id") animeId: String
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/search")
    suspend fun searchAnime(
        @Query("q") query: String,
        @Query("genre") genre: String? = null,
        @Query("year") year: Int? = null,
        @Query("type") type: String? = null,
        @Query("status") status: String? = null,
        @Query("sort") sortBy: String = "POPULARITY",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 24
    ): ApiResponse<List<AnimeDto>>

    @GET("api/v1/genres")
    suspend fun getAllGenres(): ApiResponse<List<String>>

    @GET("api/v1/studios")
    suspend fun getAllStudios(): ApiResponse<List<String>>

    // ===================================
    // HLS Stream Authorization Endpoints
    // ===================================

    /**
     * Authorizes and generates signed HLS playback URLs for an episode.
     * Prevents hotlinking, validates subscriptions/tiers, and returns signed master.m3u8,
     * multi-bitrate resolution tracks (1080p, 720p, 480p, 360p), subtitle tracks, and audio dub URLs.
     */
    @POST("api/v1/player/authorize-stream")
    suspend fun authorizePlaybackSession(
        @Header("Authorization") authToken: String? = null,
        @Body request: StreamAuthorizationRequest
    ): ApiResponse<PlaybackSessionResponse>

    /**
     * Retrieves or validates an existing playback session by its ID.
     */
    @GET("api/v1/player/session/{sessionId}")
    suspend fun getPlaybackSession(
        @Path("sessionId") sessionId: String
    ): ApiResponse<PlaybackSessionResponse>

    // ===================================
    // Authentication & Profile Endpoints
    // ===================================

    @POST("api/v1/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): ApiResponse<AuthResponse>

    @POST("api/v1/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): ApiResponse<AuthResponse>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): ApiResponse<UserDto>

    // ===================================
    // Watch Progress & Watchlist Sync
    // ===================================

    @POST("api/v1/player/progress")
    suspend fun syncWatchProgress(
        @Header("Authorization") token: String? = null,
        @Body progress: WatchProgressDto
    ): ApiResponse<Boolean>

    @POST("api/v1/watchlist/sync")
    suspend fun syncWatchlist(
        @Header("Authorization") token: String? = null,
        @Body watchlist: List<WatchlistSyncDto>
    ): ApiResponse<Boolean>

    // ===================================
    // Community Reviews & Ratings Endpoints
    // ===================================

    @GET("api/v1/anime/{id}/reviews")
    suspend fun getAnimeReviews(
        @Path("id") animeId: String
    ): ApiResponse<List<ReviewDto>>

    @POST("api/v1/anime/{id}/reviews")
    suspend fun postReview(
        @Header("Authorization") token: String? = null,
        @Path("id") animeId: String,
        @Body review: ReviewCreateRequest
    ): ApiResponse<ReviewDto>

    @POST("api/v1/reviews/{id}/like")
    suspend fun likeReview(
        @Header("Authorization") token: String? = null,
        @Path("id") reviewId: String
    ): ApiResponse<Boolean>
}
