package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RadioTrack(
    val id: String,
    val title: String,
    val animeOrigin: String,
    val artist: String,
    val category: String, // "Anime OST", "Lo-Fi", "Opening Songs", "Ending Songs", "Study Music", "Battle Music"
    val streamUrl: String,
    val coverUrl: String,
    val bpm: Int,
    val durationLabel: String = "03:42",
    val isFavorite: Boolean = false
)

enum class RadioRepeatMode(val label: String) {
    OFF("Repeat Off"),
    REPEAT_ALL("Repeat All"),
    REPEAT_ONE("Repeat One")
}

class RadioRepository {

    private val initialStations = listOf(
        RadioTrack(
            id = "radio_lofi_1",
            title = "Midnight Study Beats • Tokyo Rain Lo-Fi",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "Evan Call • Chillhop Edit",
            category = "Lo-Fi",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 78,
            durationLabel = "03:45",
            isFavorite = true
        ),
        RadioTrack(
            id = "radio_ost_1",
            title = "Zoltraak • Orchestral Suite",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "Evan Call",
            category = "Anime OST",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 128,
            durationLabel = "04:12",
            isFavorite = true
        ),
        RadioTrack(
            id = "radio_op_1",
            title = "SPECIALZ • Shibuya Opening Theme",
            animeOrigin = "Jujutsu Kaisen Season 2",
            artist = "King Gnu",
            category = "Opening Songs",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            bpm = 132,
            durationLabel = "03:58",
            isFavorite = true
        ),
        RadioTrack(
            id = "radio_ed_1",
            title = "Anytime Anywhere • Creditless Ending",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "milet",
            category = "Ending Songs",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 86,
            durationLabel = "03:50",
            isFavorite = false
        ),
        RadioTrack(
            id = "radio_study_1",
            title = "Konoha Library Acoustic Focus Session",
            animeOrigin = "Naruto Shippuden",
            artist = "Yasuharu Takanashi • Study Mix",
            category = "Study Music",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/13/17405l.jpg",
            bpm = 74,
            durationLabel = "05:20",
            isFavorite = false
        ),
        RadioTrack(
            id = "radio_battle_1",
            title = "DARK ARIA <LV2> • Shadow Monarch Awakening",
            animeOrigin = "Solo Leveling",
            artist = "Hiroyuki Sawano",
            category = "Battle Music",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            bpm = 148,
            durationLabel = "03:36",
            isFavorite = true
        ),
        RadioTrack(
            id = "radio_battle_2",
            title = "Kamado Tanjiro no Uta & Hinokami Suite",
            animeOrigin = "Demon Slayer",
            artist = "Go Shiina & Yuki Kajiura",
            category = "Battle Music",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg",
            bpm = 140,
            durationLabel = "04:45",
            isFavorite = false
        ),
        RadioTrack(
            id = "radio_op_2",
            title = "Drums of Liberation • Gear 5 Theme",
            animeOrigin = "One Piece",
            artist = "Kohei Tanaka",
            category = "Opening Songs",
            streamUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            coverUrl = "https://cdn.myanimelist.net/images/anime/6/73245l.jpg",
            bpm = 136,
            durationLabel = "03:18",
            isFavorite = true
        )
    )

    private val _stations = MutableStateFlow(initialStations)
    val stations: StateFlow<List<RadioTrack>> = _stations.asStateFlow()

    private val _queue = MutableStateFlow(initialStations)
    val queue: StateFlow<List<RadioTrack>> = _queue.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RadioRepeatMode.OFF)
    val repeatMode: StateFlow<RadioRepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _backgroundAudioEnabled = MutableStateFlow(true)
    val backgroundAudioEnabled: StateFlow<Boolean> = _backgroundAudioEnabled.asStateFlow()

    fun getRadioStations(): List<RadioTrack> = _stations.value

    fun toggleFavoriteStation(trackId: String) {
        _stations.update { list ->
            list.map { if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it }
        }
        _queue.update { list ->
            list.map { if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it }
        }
    }

    fun toggleShuffle() {
        val next = !_isShuffleEnabled.value
        _isShuffleEnabled.value = next
        if (next) {
            _queue.update { it.shuffled() }
        } else {
            _queue.value = _stations.value
        }
    }

    fun cycleRepeatMode() {
        _repeatMode.update { mode ->
            when (mode) {
                RadioRepeatMode.OFF -> RadioRepeatMode.REPEAT_ALL
                RadioRepeatMode.REPEAT_ALL -> RadioRepeatMode.REPEAT_ONE
                RadioRepeatMode.REPEAT_ONE -> RadioRepeatMode.OFF
            }
        }
    }

    fun setSleepTimer(minutes: Int?) {
        _sleepTimerMinutes.value = minutes
    }

    fun toggleBackgroundAudio(enabled: Boolean) {
        _backgroundAudioEnabled.value = enabled
    }

    fun removeTrackFromQueue(trackId: String) {
        _queue.update { list -> list.filterNot { it.id == trackId } }
    }

    fun addTrackToQueue(track: RadioTrack) {
        _queue.update { list ->
            if (list.any { it.id == track.id }) list else list + track
        }
    }
}
