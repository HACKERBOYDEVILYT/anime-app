package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RadioStation(
    val id: String,
    val title: String,
    val genre: String,
    val nowPlayingTrack: String,
    val artist: String,
    val streamUrl: String,
    val coverUrl: String,
    val serverProtocol: String,
    val moodTag: String
)

class RadioRepository {
    private val stations = listOf(
        RadioStation(
            id = "station_listen_moe_jpop",
            title = "LISTEN.moe Official Anime & J-Pop Radio 🎌",
            genre = "24/7 Live Anime OP/ED & J-Pop",
            nowPlayingTrack = "Live 24/7 Direct Stream (listen.moe)",
            artist = "LISTEN.moe Official Server",
            streamUrl = "https://listen.moe/stream",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            serverProtocol = "Icecast / Ogg / MP3 Live",
            moodTag = "24/7 Live Anime Radio"
        ),
        RadioStation(
            id = "station_plaza_one",
            title = "Nightwave Plaza Anime Synth & Future Funk 🌆",
            genre = "24/7 Live Vaporwave / Anime Citypop",
            nowPlayingTrack = "Live 24/7 Direct Stream (plaza.one)",
            artist = "Nightwave Plaza Official Server",
            streamUrl = "https://radio.plaza.one/mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1818/126431l.jpg",
            serverProtocol = "128kbps MP3 Live Server",
            moodTag = "Night Drive & Chill"
        ),
        RadioStation(
            id = "station_hls_ost",
            title = "Kuro 1080p Orchestral & Action Stream 🔥",
            genre = "1080p HLS Master Stream",
            nowPlayingTrack = "Direct Multi-Bitrate HLS Stream",
            artist = "Unified Streaming HLS CDN",
            streamUrl = "https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            serverProtocol = "HLS .m3u8 Adaptive",
            moodTag = "Action & Hype"
        ),
        RadioStation(
            id = "station_listen_moe_fallback",
            title = "LISTEN.moe Secondary Anime Stream 🎹",
            genre = "24/7 Anime & Game OST",
            nowPlayingTrack = "Live Direct Fallback Stream",
            artist = "LISTEN.moe CDN Node 2",
            streamUrl = "https://listen.moe/fallback",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            serverProtocol = "Direct Audio Stream",
            moodTag = "Relax & Focus"
        )
    )

    private val _currentStation = MutableStateFlow<RadioStation>(stations.first())
    val currentStation: StateFlow<RadioStation> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun getAllStations(): List<RadioStation> = stations

    fun selectStation(station: RadioStation) {
        _currentStation.value = station
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.update { !it }
    }
}
