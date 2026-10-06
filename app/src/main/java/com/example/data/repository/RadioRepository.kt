package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

typealias RadioTrack = RadioStation

data class RadioStation(
    val id: String,
    val title: String,
    val animeOrigin: String = "Frieren: Beyond Journey's End",
    val artist: String,
    val category: String = "Lo-Fi", // "Anime OST", "Lo-Fi", "Opening Songs", "Ending Songs", "Study Music", "Battle Music"
    val genre: String = category,
    val moodTag: String = category,
    val nowPlayingTrack: String = title,
    val serverProtocol: String = "320kbps AAC • HLS",
    val streamUrl: String = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
    val coverUrl: String,
    val bpm: Int = 90,
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
        RadioStation(
            id = "radio_lofi_1",
            title = "Midnight Study Beats • Tokyo Rain Lo-Fi",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "Evan Call • Chillhop Edit",
            category = "Lo-Fi",
            genre = "Lo-Fi Chillhop",
            moodTag = "Lo-Fi",
            nowPlayingTrack = "Zoltraak Rainy Study Mix",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 78,
            durationLabel = "03:45",
            isFavorite = true
        ),
        RadioStation(
            id = "radio_ost_1",
            title = "Zoltraak • Orchestral Suite",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "Evan Call",
            category = "Anime OST",
            genre = "Orchestral OST",
            moodTag = "Anime OST",
            nowPlayingTrack = "Beyond Journey's End Main Theme",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 128,
            durationLabel = "04:12",
            isFavorite = true
        ),
        RadioStation(
            id = "radio_op_1",
            title = "SPECIALZ • Shibuya Opening Theme",
            animeOrigin = "Jujutsu Kaisen Season 2",
            artist = "King Gnu",
            category = "Opening Songs",
            genre = "J-Rock / OP",
            moodTag = "Opening Songs",
            nowPlayingTrack = "King Gnu - SPECIALZ (TV Size)",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1792/138022l.jpg",
            bpm = 132,
            durationLabel = "03:58",
            isFavorite = true
        ),
        RadioStation(
            id = "radio_ed_1",
            title = "Anytime Anywhere • Creditless Ending",
            animeOrigin = "Frieren: Beyond Journey's End",
            artist = "milet",
            category = "Ending Songs",
            genre = "Ballad / ED",
            moodTag = "Ending Songs",
            nowPlayingTrack = "milet - Anytime Anywhere",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
            bpm = 86,
            durationLabel = "03:50",
            isFavorite = false
        ),
        RadioStation(
            id = "radio_study_1",
            title = "Konoha Library Acoustic Focus Session",
            animeOrigin = "Naruto Shippuden",
            artist = "Yasuharu Takanashi • Study Mix",
            category = "Study Music",
            genre = "Acoustic Focus",
            moodTag = "Study Music",
            nowPlayingTrack = "Gentle Breeze & Rain in Konoha",
            coverUrl = "https://cdn.myanimelist.net/images/anime/13/17405l.jpg",
            bpm = 74,
            durationLabel = "05:20",
            isFavorite = false
        ),
        RadioStation(
            id = "radio_battle_1",
            title = "DARK ARIA <LV2> • Shadow Monarch Awakening",
            animeOrigin = "Solo Leveling",
            artist = "Hiroyuki Sawano",
            category = "Battle Music",
            genre = "Epic Battle OST",
            moodTag = "Battle Music",
            nowPlayingTrack = "SawanoHiroyuki[nZk] - DARK ARIA",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1801/142390l.jpg",
            bpm = 148,
            durationLabel = "03:36",
            isFavorite = true
        ),
        RadioStation(
            id = "radio_battle_2",
            title = "Kamado Tanjiro no Uta & Hinokami Suite",
            animeOrigin = "Demon Slayer",
            artist = "Go Shiina & Yuki Kajiura",
            category = "Battle Music",
            genre = "Symphonic Action",
            moodTag = "Battle Music",
            nowPlayingTrack = "Hinokami Kagura Dance Theme",
            coverUrl = "https://cdn.myanimelist.net/images/anime/1286/99889l.jpg",
            bpm = 140,
            durationLabel = "04:45",
            isFavorite = false
        ),
        RadioStation(
            id = "radio_op_2",
            title = "Drums of Liberation • Gear 5 Theme",
            animeOrigin = "One Piece",
            artist = "Kohei Tanaka",
            category = "Opening Songs",
            genre = "Shounen Anthem",
            moodTag = "Opening Songs",
            nowPlayingTrack = "Overtaken & Gear 5 Suite",
            coverUrl = "https://cdn.myanimelist.net/images/anime/6/73245l.jpg",
            bpm = 136,
            durationLabel = "03:18",
            isFavorite = true
        )
    )

    private val _stations = MutableStateFlow(initialStations)
    val stations: StateFlow<List<RadioStation>> = _stations.asStateFlow()

    private val _currentStation = MutableStateFlow(initialStations.first())
    val currentStation: StateFlow<RadioStation> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(true)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _queue = MutableStateFlow(initialStations)
    val queue: StateFlow<List<RadioStation>> = _queue.asStateFlow()

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled: StateFlow<Boolean> = _isShuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RadioRepeatMode.OFF)
    val repeatMode: StateFlow<RadioRepeatMode> = _repeatMode.asStateFlow()

    private val _sleepTimerMinutes = MutableStateFlow<Int?>(null)
    val sleepTimerMinutes: StateFlow<Int?> = _sleepTimerMinutes.asStateFlow()

    private val _backgroundAudioEnabled = MutableStateFlow(true)
    val backgroundAudioEnabled: StateFlow<Boolean> = _backgroundAudioEnabled.asStateFlow()

    fun getAllStations(): List<RadioStation> = _stations.value

    fun getRadioStations(): List<RadioStation> = _stations.value

    fun selectStation(station: RadioStation) {
        _currentStation.value = station
        _isPlaying.value = true
    }

    fun togglePlayPause() {
        _isPlaying.update { !it }
    }

    fun toggleFavoriteStation(trackId: String) {
        _stations.update { list ->
            list.map { if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it }
        }
        _queue.update { list ->
            list.map { if (it.id == trackId) it.copy(isFavorite = !it.isFavorite) else it }
        }
        if (_currentStation.value.id == trackId) {
            _currentStation.update { it.copy(isFavorite = !it.isFavorite) }
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

    fun addTrackToQueue(track: RadioStation) {
        _queue.update { list ->
            if (list.any { it.id == track.id }) list else list + track
        }
    }
}
