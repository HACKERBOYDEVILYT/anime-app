package com.example.data.repository

import com.example.security.AuthSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class PartyMember(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isHost: Boolean = false,
    val latencyMs: Int = 22
) {
    val userId: String get() = id
    val username: String get() = name
}

typealias PartyParticipant = PartyMember

data class PartyChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderName: String,
    val senderAvatar: String = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
    val message: String,
    val isSystem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class WatchPartyRoom(
    val roomId: String,
    val roomCode: String,
    val roomName: String,
    val animeId: String,
    val animeTitle: String,
    val episodeNumber: Int,
    val posterUrl: String,
    val hostName: String,
    val isPrivate: Boolean = false,
    val inviteLink: String = "https://robiul.anime/party/$roomCode",
    val isPlaying: Boolean = true,
    val currentPositionMs: Long = 852_000L,
    val activeReactionBurst: String? = null,
    val members: List<PartyMember> = emptyList(),
    val messages: List<PartyChatMessage> = emptyList()
)

class WatchPartyRepository(
    private val animeRepository: AnimeRepository? = null
) {
    private val _currentRoom = MutableStateFlow<WatchPartyRoom?>(null)
    val currentRoom: StateFlow<WatchPartyRoom?> = _currentRoom.asStateFlow()

    private val _publicRooms = MutableStateFlow(
        listOf(
            WatchPartyRoom(
                roomId = "room_pub_1",
                roomCode = "KR-7824",
                roomName = "Frieren Ep 28 Simulcast Party",
                animeId = "anime_1",
                animeTitle = "Frieren: Beyond Journey's End",
                episodeNumber = 1,
                posterUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                hostName = "Robiul",
                isPrivate = false,
                isPlaying = true,
                currentPositionMs = 420_000L,
                members = listOf(
                    PartyMember("u_host", "Robiul", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", isHost = true, latencyMs = 18),
                    PartyMember("u_2", "AkiraVortex", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", isHost = false, latencyMs = 24),
                    PartyMember("u_3", "HinataSakura", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=200", isHost = false, latencyMs = 29)
                ),
                messages = listOf(
                    PartyChatMessage(
                        senderName = "System",
                        message = "Synchronized playback active • Invite link: https://robiul.anime/party/KR-7824",
                        isSystem = true
                    ),
                    PartyChatMessage(
                        senderName = "AkiraVortex",
                        senderAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                        message = "The animation quality in 1080p is insane 🔥"
                    )
                )
            )
        )
    )
    val publicRooms: StateFlow<List<WatchPartyRoom>> = _publicRooms.asStateFlow()

    suspend fun createRoom(
        animeId: String,
        episodeNumber: Int,
        hostName: String,
        isPrivate: Boolean = false
    ): WatchPartyRoom {
        val anime = animeRepository?.getAnimeById(animeId)
        val title = anime?.titleEnglish ?: "Frieren: Beyond Journey's End"
        val poster = anime?.bannerUrl?.ifBlank { anime.posterUrl }
            ?: "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg"
        val code = "KR-" + (1000..9999).random()
        val room = WatchPartyRoom(
            roomId = "room_${UUID.randomUUID().toString().take(8)}",
            roomCode = code,
            roomName = "${if (isPrivate) "[Private] " else ""}$title • Ep $episodeNumber Party",
            animeId = animeId,
            animeTitle = title,
            episodeNumber = episodeNumber,
            posterUrl = poster,
            hostName = hostName,
            isPrivate = isPrivate,
            inviteLink = "https://robiul.anime/party/$code",
            isPlaying = true,
            currentPositionMs = 0L,
            members = listOf(
                PartyMember("u_host", hostName, "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", isHost = true, latencyMs = 16),
                PartyMember("u_p2", "AkiraVortex", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", isHost = false, latencyMs = 24),
                PartyMember("u_p3", "GojoInfinity", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200", isHost = false, latencyMs = 28)
            ),
            messages = listOf(
                PartyChatMessage(
                    senderName = "System",
                    message = "Watch Party created! Room Code: $code • Share Link: https://robiul.anime/party/$code",
                    isSystem = true
                )
            )
        )
        _currentRoom.value = room
        if (!isPrivate) {
            _publicRooms.update { listOf(room) + it }
        }
        return room
    }

    suspend fun joinRoom(roomCode: String, guestName: String): Boolean {
        val cleanCode = roomCode.trim().uppercase()
        if (cleanCode.length < 3) return false
        val matched = _publicRooms.value.find { it.roomCode.equals(cleanCode, ignoreCase = true) }
        val room = if (matched != null) {
            val newMember = PartyMember(
                id = "u_${System.currentTimeMillis()}",
                name = guestName,
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                isHost = false
            )
            matched.copy(
                members = matched.members + newMember,
                messages = matched.messages + PartyChatMessage(
                    senderName = "System",
                    message = "$guestName joined the Watch Party!",
                    isSystem = true
                )
            )
        } else {
            WatchPartyRoom(
                roomId = "room_joined_${cleanCode}",
                roomCode = cleanCode,
                roomName = "Live Party • $cleanCode",
                animeId = "anime_1",
                animeTitle = "Frieren: Beyond Journey's End",
                episodeNumber = 1,
                posterUrl = "https://cdn.myanimelist.net/images/anime/1015/138006l.jpg",
                hostName = "Robiul",
                inviteLink = "https://robiul.anime/party/$cleanCode",
                isPlaying = true,
                currentPositionMs = 310_000L,
                members = listOf(
                    PartyMember("u_host", "Robiul", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200", isHost = true),
                    PartyMember("u_guest", guestName, "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", isHost = false)
                ),
                messages = listOf(
                    PartyChatMessage(
                        senderName = "System",
                        message = "Connected to Room $cleanCode • Playback synced with host!",
                        isSystem = true
                    )
                )
            )
        }
        _currentRoom.value = room
        return true
    }

    fun leaveRoom() {
        _currentRoom.value = null
    }

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        _currentRoom.update { room ->
            room?.copy(
                isPlaying = isPlaying,
                currentPositionMs = positionMs,
                messages = room.messages + PartyChatMessage(
                    senderName = "System",
                    message = if (isPlaying) "▶️ Host resumed playback for all members" else "⏸️ Host paused playback for all members",
                    isSystem = true
                )
            )
        }
    }

    fun hostSeekTo(positionMs: Long) {
        val totalSec = (positionMs / 1000L).coerceAtLeast(0L)
        val formatted = String.format("%02d:%02d", totalSec / 60, totalSec % 60)
        _currentRoom.update { room ->
            room?.copy(
                currentPositionMs = positionMs,
                messages = room.messages + PartyChatMessage(
                    senderName = "System",
                    message = "⏩ Host synced everyone to $formatted",
                    isSystem = true
                )
            )
        }
    }

    fun sendEmojiReaction(senderName: String, emoji: String) {
        _currentRoom.update { room ->
            room?.copy(
                activeReactionBurst = "$senderName reacted $emoji",
                messages = room.messages + PartyChatMessage(
                    senderName = senderName,
                    message = "Reacted with $emoji"
                )
            )
        }
    }

    fun removeParticipant(memberId: String) {
        _currentRoom.update { room ->
            room?.copy(
                members = room.members.filterNot { it.id == memberId && !it.isHost }
            )
        }
    }

    fun sendMessage(senderName: String, text: String) {
        val cleanText = AuthSecurityManager.sanitizeInput(text, 500)
        if (cleanText.isBlank()) return
        _currentRoom.update { room ->
            room?.copy(
                messages = room.messages + PartyChatMessage(
                    senderName = senderName,
                    message = cleanText
                )
            )
        }
    }
}
