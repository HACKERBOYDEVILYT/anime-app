package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.UserPreferences
import com.example.data.repository.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    val user: StateFlow<User> = userRepository.currentUser

    fun updateDefaultQuality(quality: String) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(defaultQuality = quality))
    }

    fun toggleAutoNext(enabled: Boolean) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(autoNextEpisode = enabled))
    }

    fun toggleAutoPlay(enabled: Boolean) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(autoPlay = enabled))
    }

    fun toggleSkipIntro(enabled: Boolean) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(skipIntro = enabled))
    }

    fun updatePreferredAudio(audio: String) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(preferredAudio = audio))
    }

    fun updatePreferredSubtitle(sub: String) {
        val current = user.value.preferences
        userRepository.updatePreferences(current.copy(preferredSubtitle = sub))
    }
}
