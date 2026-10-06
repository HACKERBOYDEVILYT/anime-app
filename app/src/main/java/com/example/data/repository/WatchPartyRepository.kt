package com.example.data.repository

import com.example.data.local.dao.WatchPartyDao
import com.example.data.local.entity.PartyChatMessageEntity
import com.example.data.local.entity.WatchPartyRoomEntity
import com.example.security.AuthSecurityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.util.UUID

data class PartyParticipant(
    val userId: String,
    val username: String,
    val avatarEmoji: String,
    val isHost: Boolean = false,
    val isReady: Boolean = true,
    val latencyMs: Int = 22
)

data class PartyRoomSyncState(
    val roomId: String,
    val isPrivate: Boolean = false,
    val inviteLink: String,
    val isPlaying: Boolean = true,
    val playbackTimestampSec: Int = 852, // 14:12
    val activeReactionBurst: String? = null,
    val participants: List<PartyParticipant> = listOf(
        PartyParticipant("u_host", "Robiul", "👑", isHost = true, isReady = true, latencyMs = 18),
        PartyParticipant("u_p2", "AkiraVortex", "⚡", isHost = false, isReady = true, latencyMs = 24),
        PartyParticipant("u_p3", "HinataSakura", "🌸", isHost = false, isReady = true, latencyMs = 31),
        PartyParticipant("u_p4", "GojoInfinity", "🤞", isHost = false, isReady = true, latencyMs = 27)
    )
)

class WatchPartyRepository(
    private val watchPartyDao: WatchPartyDao
) {
    val activeRooms: Flow<List<WatchPartyRoomEntity>> = watchPartyDao.getActiveRooms()

    private val _roomSyncStates = MutableStateFlow<Map<String, PartyRoomSyncState>>(emptyMap())
    val roomSyncStates: StateFlow<Map<String, PartyRoomSyncState>> = _roomSyncStates.asStateFlow()

    fun getRoomMessages(roomId: String): Flow<List<PartyChatMessageEntity>> =
        watchPartyDao.getMessagesForRoom(roomId)

    suspend fun seedInitialRoomsIfEmpty() = withContext(Dispatchers.IO) {
        watchPartyDao.deleteFakeDemoRooms()
    }

    fun getOrCreateSyncState(room: WatchPartyRoomEntity): PartyRoomSyncState {
        val current = _roomSyncStates.value[room.roomId]
        if (current != null) return current
        val created = PartyRoomSyncState(
            roomId = room.roomId,
            isPrivate = room.roomName.contains("[Private]", ignoreCase = true),
            inviteLink = "https://robiul.anime/party/${room.roomCode}"
        )
        _roomSyncStates.update { it + (room.roomId to created) }
        return created
    }

    suspend fun createRoom(
        roomName: String,
        animeId: String,
        animeTitle: String,
        episodeNumber: Int,
        posterUrl: String,
        hostName: String,
        isPrivate: Boolean = false
    ): WatchPartyRoomEntity = withContext(Dispatchers.IO) {
        val cleanName = AuthSecurityManager.sanitizeInput(roomName, 80).ifBlank { "$animeTitle Watch Party" }
        val cleanHost = AuthSecurityManager.sanitizeInput(hostName, 40).ifBlank { "Robiul" }
        val code = "KR-" + (1000..9999).random()
        val taggedName = if (isPrivate && !cleanName.contains("[Private]")) "[Private] $cleanName" else cleanName
        val newRoom = WatchPartyRoomEntity(
            roomId = "room_${UUID.randomUUID().toString().take(8)}",
            roomCode = code,
            roomName = taggedName,
            animeId = animeId,
            animeTitle = animeTitle,
            episodeNumber = episodeNumber,
            posterUrl = posterUrl,
            hostName = cleanHost,
            viewerCount = 1,
            currentPositionText = "00:00",
            isLive = true
        )
        watchPartyDao.insertRoom(newRoom)
        _roomSyncStates.update { map ->
            map + (
                newRoom.roomId to PartyRoomSyncState(
                    roomId = newRoom.roomId,
                    isPrivate = isPrivate,
                    inviteLink = "https://robiul.anime/party/$code",
                    isPlaying = true,
                    playbackTimestampSec = 0
                )
            )
        }
        watchPartyDao.insertMessage(
            PartyChatMessageEntity(
                id = UUID.randomUUID().toString(),
                roomId = newRoom.roomId,
                senderName = "PartyBot 🤖",
                senderAvatar = "🎉",
                message = "Room '$taggedName' created! Share code $code or link https://robiul.anime/party/$code",
                timestamp = System.currentTimeMillis()
            )
        )
        newRoom
    }

    fun toggleHostPlayPause(roomId: String, hostName: String = "Robiul") {
        _roomSyncStates.update { map ->
            val current = map[roomId] ?: PartyRoomSyncState(roomId = roomId, inviteLink = "https://robiul.anime/party/$roomId")
            val nextPlaying = !current.isPlaying
            map + (roomId to current.copy(isPlaying = nextPlaying))
        }
    }

    suspend fun hostSeekTo(roomId: String, targetSeconds: Int, hostName: String = "Robiul") = withContext(Dispatchers.IO) {
        val safeSec = targetSeconds.coerceAtLeast(0)
        val mins = safeSec / 60
        val secs = safeSec % 60
        val formatted = String.format("%02d:%02d", mins, secs)
        _roomSyncStates.update { map ->
            val current = map[roomId] ?: PartyRoomSyncState(roomId = roomId, inviteLink = "https://robiul.anime/party/$roomId")
            map + (roomId to current.copy(playbackTimestampSec = safeSec))
        }
        watchPartyDao.insertMessage(
            PartyChatMessageEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                senderName = "Host Sync ⚡",
                senderAvatar = "👑",
                message = "$hostName synced all participants to $formatted",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun sendEmojiReaction(roomId: String, senderName: String, emoji: String) = withContext(Dispatchers.IO) {
        _roomSyncStates.update { map ->
            val current = map[roomId] ?: PartyRoomSyncState(roomId = roomId, inviteLink = "https://robiul.anime/party/$roomId")
            map + (roomId to current.copy(activeReactionBurst = "$senderName reacted $emoji"))
        }
        watchPartyDao.insertMessage(
            PartyChatMessageEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                senderName = senderName,
                senderAvatar = emoji,
                message = "Reacted with $emoji",
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun removeParticipant(roomId: String, userId: String) {
        _roomSyncStates.update { map ->
            val current = map[roomId] ?: return@update map
            map + (roomId to current.copy(participants = current.participants.filterNot { it.userId == userId && !it.isHost }))
        }
    }

    suspend fun sendMessage(roomId: String, senderName: String, avatar: String, text: String) = withContext(Dispatchers.IO) {
        val cleanText = AuthSecurityManager.sanitizeInput(text, 500)
        if (cleanText.isBlank()) return@withContext
        watchPartyDao.insertMessage(
            PartyChatMessageEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                senderName = AuthSecurityManager.sanitizeInput(senderName, 40).ifBlank { "Guest" },
                senderAvatar = avatar,
                message = cleanText,
                timestamp = System.currentTimeMillis()
            )
        )
    }
}
