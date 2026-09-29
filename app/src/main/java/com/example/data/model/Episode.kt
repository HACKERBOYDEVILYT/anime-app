package com.example.data.model

data class EpisodeSource(
    val id: String,
    val quality: String, // "1080p", "720p", "480p", "360p", "Auto"
    val streamUrl: String,
    val isHls: Boolean = true,
    val cdnNode: String = "Cloudflare Global CDN"
)

data class EpisodeSubtitle(
    val id: String,
    val language: String, // "en", "ja", "es", "fr", "bn", "hi", "ar"
    val label: String,    // "English", "Japanese", "Spanish", "French", "Bangla", "Hindi", "Arabic"
    val url: String,
    val isDefault: Boolean = false
)

data class EpisodeAudio(
    val id: String,
    val language: String, // "ja", "en", "hi", "bn", "es"
    val label: String,    // "Japanese [Original]", "English [Dub]", "Hindi [Dub]", "Bangla [Dub]", "Spanish [Dub]"
    val isDefault: Boolean = false
)

data class Episode(
    val id: String,
    val animeId: String,
    val episodeNumber: Int,
    val title: String,
    val thumbnail: String,
    val durationSeconds: Long = 1440L, // default ~24 min
    val airDate: String,
    val introStartSec: Long = 90L,
    val introEndSec: Long = 180L,
    val outroStartSec: Long = 1320L,
    val outroEndSec: Long = 1410L,
    val synopsis: String = "",
    val sources: List<EpisodeSource> = emptyList(),
    val subtitles: List<EpisodeSubtitle> = emptyList(),
    val audioTracks: List<EpisodeAudio> = emptyList()
)
