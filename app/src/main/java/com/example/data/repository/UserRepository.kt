package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserRepository {
    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    fun updatePreferences(newPrefs: UserPreferences) {
        _currentUser.update { it.copy(preferences = newPrefs) }
    }

    fun updateProfile(username: String, email: String, avatarUrl: String) {
        _currentUser.update { it.copy(username = username, email = email, avatarUrl = avatarUrl) }
    }

    fun incrementWatchTime(minutes: Float) {
        _currentUser.update {
            it.copy(
                watchTimeHours = it.watchTimeHours + (minutes / 60f),
                episodesWatched = it.episodesWatched + 1
            )
        }
    }
}
