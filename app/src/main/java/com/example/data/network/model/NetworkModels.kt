package com.example.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Standard Envelope Response from the KuroStream API
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: ApiErrorDto? = null,
    @Json(name = "meta") val meta: ApiMetaDto? = null
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "code") val code: String,
    @Json(name = "message") val message: String,
    @Json(name = "details") val details: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiMetaDto(
    @Json(name = "page") val page: Int? = null,
    @Json(name = "limit") val limit: Int? = null,
    @Json(name = "total") val total: Int? = null,
    @Json(name = "hasMore") val hasMore: Boolean? = null
)

// ========================
// Anime Catalog Data Models
// ========================

@JsonClass(generateAdapter = true)
data class CharacterDto(
    @Json(name = "name") val name: String,
    @Json(name = "role") val role: String,
    @Json(name = "avatarUrl") val avatarUrl: String,
    @Json(name = "voiceActor") val voiceActor: String? = null
)

@JsonClass(generateAdapter = true)
data class AnimeDto(
    @Json(name = "id") val id: String,
    @Json(name = "slug") val slug: String,
    @Json(name = "titleEnglish") val titleEnglish: String,
    @Json(name = "titleRomaji") val titleRomaji: String? = null,
    @Json(name = "titleJapanese") val titleJapanese: String? = null,
    @Json(name = "description") val description: String,
    @Json(name = "posterUrl") val posterUrl: String,
    @Json(name = "bannerUrl") val bannerUrl: String? = null,
    @Json(name = "rating") val rating: Float,
    @Json(name = "score") val score: Int? = null,
    @Json(name = "type") val type: String = "TV",
    @Json(name = "status") val status: String = "FINISHED",
    @Json(name = "episodesCount") val episodesCount: Int,
    @Json(name = "releaseYear") val releaseYear: Int,
    @Json(name = "season") val season: String? = null,
    @Json(name = "durationMinutes") val durationMinutes: Int = 24,
    @Json(name = "studio") val studio: String,
    @Json(name = "producers") val producers: List<String>? = null,
    @Json(name = "genres") val genres: List<String> = emptyList(),
    @Json(name = "tags") val tags: List<String>? = null,
    @Json(name = "trailerUrl") val trailerUrl: String? = null,
    @Json(name = "characters") val characters: List<CharacterDto>? = null,
    @Json(name = "isFeatured") val isFeatured: Boolean = false,
    @Json(name = "isTrending") val isTrending: Boolean = false,
    @Json(name = "isPopular") val isPopular: Boolean = false,
    @Json(name = "isSeasonal") val isSeasonal: Boolean = false,
    @Json(name = "nextEpisodeAirDate") val nextEpisodeAirDate: String? = null
)

@JsonClass(generateAdapter = true)
data class EpisodeDto(
    @Json(name = "id") val id: String,
    @Json(name = "animeId") val animeId: String,
    @Json(name = "episodeNumber") val episodeNumber: Int,
    @Json(name = "title") val title: String,
    @Json(name = "thumbnail") val thumbnail: String,
    @Json(name = "durationSeconds") val durationSeconds: Long = 1440L,
    @Json(name = "airDate") val airDate: String,
    @Json(name = "introStartSec") val introStartSec: Long = 90L,
    @Json(name = "introEndSec") val introEndSec: Long = 180L,
    @Json(name = "outroStartSec") val outroStartSec: Long = 1320L,
    @Json(name = "outroEndSec") val outroEndSec: Long = 1410L,
    @Json(name = "synopsis") val synopsis: String? = null
)

// ===================================
// HLS Stream Authorization Data Models
// ===================================

@JsonClass(generateAdapter = true)
data class QualityStreamDto(
    @Json(name = "quality") val quality: String, // "1080p", "720p", "480p", "360p", "Auto"
    @Json(name = "url") val url: String,         // Signed HLS resolution playlist URL (.m3u8)
    @Json(name = "bitrate") val bitrate: Int? = null,
    @Json(name = "codec") val codec: String? = "avc1.640028,mp4a.40.2"
)

@JsonClass(generateAdapter = true)
data class SubtitleDto(
    @Json(name = "id") val id: String,
    @Json(name = "language") val language: String,
    @Json(name = "label") val label: String,
    @Json(name = "url") val url: String,
    @Json(name = "format") val format: String = "VTT",
    @Json(name = "isDefault") val isDefault: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AudioTrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "language") val language: String,
    @Json(name = "label") val label: String,
    @Json(name = "url") val url: String? = null,
    @Json(name = "isDefault") val isDefault: Boolean = false
)

@JsonClass(generateAdapter = true)
data class StreamAuthorizationRequest(
    @Json(name = "episodeId") val episodeId: String,
    @Json(name = "animeId") val animeId: String,
    @Json(name = "clientSessionId") val clientSessionId: String,
    @Json(name = "preferredQuality") val preferredQuality: String? = "1080p"
)

@JsonClass(generateAdapter = true)
data class PlaybackSessionResponse(
    @Json(name = "sessionId") val sessionId: String,
    @Json(name = "episodeId") val episodeId: String,
    @Json(name = "expiresAt") val expiresAt: Long,
    @Json(name = "masterPlaylistUrl") val masterPlaylistUrl: String, // Signed master.m3u8 URL
    @Json(name = "streamQualities") val streamQualities: List<QualityStreamDto> = emptyList(),
    @Json(name = "subtitles") val subtitles: List<SubtitleDto> = emptyList(),
    @Json(name = "audioTracks") val audioTracks: List<AudioTrackDto> = emptyList(),
    @Json(name = "cdnNode") val cdnNode: String = "Cloudflare Global Edge",
    @Json(name = "drmToken") val drmToken: String? = null
)

// ========================
// User & Auth Data Models
// ========================

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String,
    @Json(name = "avatarUrl") val avatarUrl: String,
    @Json(name = "tier") val tier: String = "Ultra VIP",
    @Json(name = "role") val role: String = "USER",
    @Json(name = "episodesWatched") val episodesWatched: Int = 0,
    @Json(name = "watchTimeHours") val watchTimeHours: Float = 0f
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    @Json(name = "user") val user: UserDto
)

// ======================================
// Watch Progress & Interaction Data Models
// ======================================

@JsonClass(generateAdapter = true)
data class WatchProgressDto(
    @Json(name = "animeId") val animeId: String,
    @Json(name = "episodeId") val episodeId: String,
    @Json(name = "progressMs") val progressMs: Long,
    @Json(name = "durationMs") val durationMs: Long,
    @Json(name = "lastWatchedAt") val lastWatchedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class WatchlistSyncDto(
    @Json(name = "animeId") val animeId: String,
    @Json(name = "status") val status: String,
    @Json(name = "isFavorite") val isFavorite: Boolean = false,
    @Json(name = "updatedAt") val updatedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class ReviewCreateRequest(
    @Json(name = "rating") val rating: Int,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ReviewDto(
    @Json(name = "id") val id: String,
    @Json(name = "animeId") val animeId: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "userName") val userName: String,
    @Json(name = "userAvatar") val userAvatar: String,
    @Json(name = "rating") val rating: Int,
    @Json(name = "content") val content: String,
    @Json(name = "createdAt") val createdAt: Long,
    @Json(name = "likesCount") val likesCount: Int = 0
)
