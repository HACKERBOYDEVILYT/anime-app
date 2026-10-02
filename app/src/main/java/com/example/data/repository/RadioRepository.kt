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
    val listenersCount: Int,
    val moodTag: String
)

class RadioRepository {
    private val stations = listOf(
        RadioStation(
            id = "station_lofi",
            title = "Tokyo Midnight Lo-Fi ☕",
            genre = "Chillhop / Lo-Fi Beats",
            nowPlayingTrack = "Spirited Coffee in Shibuya",
            artist = "Kuro Chill Records",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
            listenersCount = 2840,
            moodTag = "Relax & Study"
        ),
        RadioStation(
            id = "station_battle",
            title = "Shonen Battle OST Hype 🔥",
            genre = "Epic Orchestral / Rock",
            nowPlayingTrack = "Domain Expansion Climax",
            artist = "Symphonic Sorcerers",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            coverUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=400",
            listenersCount = 4910,
            moodTag = "Energy & Workout"
        ),
        RadioStation(
            id = "station_piano",
            title = "Melancholic Anime Piano 🎹",
            genre = "Emotional Acoustic Piano",
            nowPlayingTrack = "A Journey Under Starlit Skies",
            artist = "Madhouse Chamber Trio",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            coverUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?w=400",
            listenersCount = 1750,
            moodTag = "Nostalgic & Tears"
        ),
        RadioStation(
            id = "station_synth",
            title = "Cyber Citypop & Synth 🌆",
            genre = "Neo-Retro 80s / Synthwave",
            nowPlayingTrack = "Akira Neon Highway 1988",
            artist = "Kuro Synthwave Collective",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            coverUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=400",
            listenersCount = 3120,
            moodTag = "Night Drive"
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
