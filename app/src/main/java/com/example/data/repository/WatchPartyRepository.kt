package com.example.data.repository

import com.example.data.model.Anime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class PartyMember(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isHost: Boolean = false
)

data class PartyChatMessage(
    val id: String,
    val senderName: String,
    val senderAvatar: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystem: Boolean = false
)

data class WatchPartyRoom(
    val roomCode: String,
    val roomName: String,
    val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val episodeNumber: Int,
    val hostName: String,
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 0L,
    val members: List<PartyMember> = emptyList(),
    val messages: List<PartyChatMessage> = emptyList()
)

class WatchPartyRepository(
    private val animeRepository: AnimeRepository
) {
    private val _currentRoom = MutableStateFlow<WatchPartyRoom?>(null)
    val currentRoom: StateFlow<WatchPartyRoom?> = _currentRoom.asStateFlow()

    suspend fun createRoom(animeId: String, episodeNumber: Int, hostName: String): WatchPartyRoom {
        val anime = animeRepository.getAnimeById(animeId) ?: animeRepository.getTrending().first()
        val code = "KR-" + (1000..9999).random()

        val hostMember = PartyMember(
            id = UUID.randomUUID().toString(),
            name = hostName,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
            isHost = true
        )

        val room = WatchPartyRoom(
            roomCode = code,
            roomName = "${anime.titleEnglish} Watch Party",
            animeId = anime.id,
            animeTitle = anime.titleEnglish,
            posterUrl = anime.posterUrl,
            episodeNumber = episodeNumber,
            hostName = hostName,
            members = listOf(
                hostMember,
                PartyMember(UUID.randomUUID().toString(), "TanjiroFan", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120"),
                PartyMember(UUID.randomUUID().toString(), "FrierenMage", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120")
            ),
            messages = listOf(
                PartyChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = "System",
                    senderAvatar = "",
                    message = "🎉 Watch Party room $code created! Share this code with your friends to watch together in sync.",
                    isSystem = true
                ),
                PartyChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = "TanjiroFan",
                    senderAvatar = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120",
                    message = "Let's gooo! Ready for this episode 🔥"
                )
            )
        )
        _currentRoom.value = room
        return room
    }

    suspend fun joinRoom(roomCode: String, userName: String): Boolean {
        val existing = _currentRoom.value
        if (existing != null && existing.roomCode.equals(roomCode.trim(), ignoreCase = true)) {
            val newMember = PartyMember(
                id = UUID.randomUUID().toString(),
                name = userName,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120"
            )
            _currentRoom.update { current ->
                current?.copy(
                    members = current.members + newMember,
                    messages = current.messages + PartyChatMessage(
                        id = UUID.randomUUID().toString(),
                        senderName = "System",
                        senderAvatar = "",
                        message = "👋 $userName joined the Watch Party!",
                        isSystem = true
                    )
                )
            }
            return true
        }

        // If joining via a code not yet created, generate a room on the fly
        val trending = animeRepository.getTrending().firstOrNull() ?: return false
        val room = WatchPartyRoom(
            roomCode = roomCode.uppercase().trim(),
            roomName = "${trending.titleEnglish} Watch Party",
            animeId = trending.id,
            animeTitle = trending.titleEnglish,
            posterUrl = trending.posterUrl,
            episodeNumber = 1,
            hostName = "Senpai",
            members = listOf(
                PartyMember(UUID.randomUUID().toString(), "Senpai", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=120", isHost = true),
                PartyMember(UUID.randomUUID().toString(), userName, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120")
            ),
            messages = listOf(
                PartyChatMessage(
                    id = UUID.randomUUID().toString(),
                    senderName = "System",
                    senderAvatar = "",
                    message = "Connected to room ${roomCode.uppercase().trim()}! Syncing video stream...",
                    isSystem = true
                )
            )
        )
        _currentRoom.value = room
        return true
    }

    fun sendMessage(senderName: String, text: String) {
        val msg = PartyChatMessage(
            id = UUID.randomUUID().toString(),
            senderName = senderName,
            senderAvatar = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120",
            message = text
        )
        _currentRoom.update { current ->
            current?.copy(messages = current.messages + msg)
        }
    }

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        _currentRoom.update { current ->
            current?.copy(isPlaying = isPlaying, currentPositionMs = positionMs)
        }
    }

    fun leaveRoom() {
        _currentRoom.value = null
    }
}
