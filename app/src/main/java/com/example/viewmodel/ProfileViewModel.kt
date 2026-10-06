package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.model.UserPreferences
import com.example.data.model.UserRole
import com.example.data.model.WatchlistItem
import com.example.data.repository.DownloadsRepository
import com.example.data.repository.GamificationAndSocialRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WatchRepository
import com.example.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    val userRepository: UserRepository,
    private val watchRepository: WatchRepository? = null,
    val gamificationRepository: GamificationAndSocialRepository = GamificationAndSocialRepository(),
    val cloudSyncManager: CloudSyncManager = CloudSyncManager(),
    val downloadsRepository: DownloadsRepository? = null
) : ViewModel() {

    val user: StateFlow<User> = userRepository.currentUser
        .map { it ?: userRepository.getCurrentUserSnapshot() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = userRepository.getCurrentUserSnapshot()
        )

    val isLoggedIn: StateFlow<Boolean> = userRepository.isLoggedIn

    val watchlistItems: StateFlow<List<WatchlistItem>> = (watchRepository?.getAllWatchlist()
        ?: MutableStateFlow(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    fun login(email: String, passwordPlain: String): Result<User> {
        val uname = email.substringBefore("@").replaceFirstChar { it.uppercase() }.ifBlank { "Robiul" }
        viewModelScope.launch {
            val res = userRepository.login(uname, email, passwordPlain)
            _statusBannerMessage.value = res.fold(
                onSuccess = { "✅ Signed in & restored cloud state across 13 domains." },
                onFailure = { "⚠️ ${it.localizedMessage}" }
            )
        }
        return Result.success(userRepository.getCurrentUserSnapshot())
    }

    fun login(username: String, email: String, passwordPlain: String) {
        viewModelScope.launch {
            val res = userRepository.login(username, email, passwordPlain)
            _statusBannerMessage.value = res.fold(
                onSuccess = { "✅ Signed in & restored cloud state across 13 domains." },
                onFailure = { "⚠️ ${it.localizedMessage}" }
            )
        }
    }

    fun register(username: String, email: String, passwordPlain: String): Result<User> {
        registerAccount(username, email, passwordPlain)
        return Result.success(userRepository.getCurrentUserSnapshot())
    }

    fun registerAccount(username: String, email: String, passwordPlain: String) {
        viewModelScope.launch {
            val res = userRepository.registerAccount(username, email, passwordPlain)
            _statusBannerMessage.value = res.fold(
                onSuccess = { code -> "✅ Account created! Verification code sent: $code" },
                onFailure = { "⚠️ ${it.localizedMessage}" }
            )
        }
    }

    fun verifyEmailCode(email: String, code: String) {
        val ok = userRepository.verifyEmailCode(email, code)
        _statusBannerMessage.value = if (ok) {
            "✅ Email verified! +50 XP awarded."
        } else {
            "⚠️ Invalid verification code. Please check the code and try again."
        }
    }

    fun sendVerificationCode(email: String) {
        val code = userRepository.sendEmailVerificationCode(email)
        _statusBannerMessage.value = "📩 Verification code sent to $email: $code"
    }

    fun signInWithGoogle(email: String, displayName: String) {
        viewModelScope.launch {
            userRepository.signInWithGoogle(email, displayName)
            _statusBannerMessage.value = "✅ Signed in with Google ($email) & synced cloud profile."
        }
    }

    fun requestPasswordReset(email: String) {
        val code = userRepository.requestPasswordResetCode(email)
        _statusBannerMessage.value = "🔑 Password reset code sent to $email: $code"
    }

    fun confirmPasswordReset(email: String, code: String, newPasswordPlain: String) {
        val res = userRepository.resetPasswordWithCode(email, code, newPasswordPlain)
        _statusBannerMessage.value = res.fold(
            onSuccess = { "✅ Password reset complete & refresh token rotated." },
            onFailure = { "⚠️ ${it.localizedMessage}" }
        )
    }

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
            _statusBannerMessage.value = "Signed out of current device."
        }
    }

    fun logoutFromAllDevices() {
        viewModelScope.launch {
            userRepository.logoutFromAllDevices()
            _statusBannerMessage.value = "🛡️ Logged out from all other devices & revoked remote sessions."
        }
    }

    fun revokeSession(sessionId: String) {
        userRepository.revokeDeviceSession(sessionId)
        _statusBannerMessage.value = "Revoked device session $sessionId."
    }

    fun rotateRefreshToken() {
        val token = userRepository.rotateCurrentRefreshToken()
        _statusBannerMessage.value = "🔄 Refresh token rotated (${token.take(18)}...)."
    }

    fun toggleTwoFactor(enabled: Boolean) {
        val code = userRepository.toggleTwoFactor(enabled)
        _statusBannerMessage.value = if (enabled) {
            "🔐 2FA enabled! Active TOTP verification code: $code"
        } else {
            "2FA disabled for this account."
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            userRepository.deleteAccountPermanently()
            _statusBannerMessage.value = "Account deleted and sessions wiped."
        }
    }

    fun updateProfile(username: String, bio: String, avatarUrl: String? = null) {
        viewModelScope.launch {
            userRepository.updateProfile(username, bio, avatarUrl)
            _statusBannerMessage.value = "✅ Profile updated & synced to cloud."
        }
    }

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            userRepository.updateRole(role)
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.updatePreference(darkTheme = enabled)
        }
    }

    fun togglePreferDub(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.updatePreference(preferDub = enabled)
        }
    }

    fun setDefaultQuality(quality: String) {
        viewModelScope.launch {
            userRepository.updatePreference(defaultQuality = quality)
        }
    }

    fun updateDefaultQuality(quality: String) {
        setDefaultQuality(quality)
    }

    fun toggleAutoPlayNext(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.updatePreference(autoPlayNext = enabled)
        }
    }

    fun toggleAutoNext(enabled: Boolean) {
        toggleAutoPlayNext(enabled)
    }

    fun toggleSkipIntro(enabled: Boolean) {
        userRepository.updateAdvancedPreferences { it.copy(autoSkipIntro = enabled) }
    }

    fun updatePreferredAudio(lang: String) {
        userRepository.updateAdvancedPreferences { it.copy(preferredAudioLanguage = lang) }
    }

    fun updatePreferredSubtitle(lang: String) {
        userRepository.updateAdvancedPreferences { it.copy(preferredSubtitleLanguage = lang) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            userRepository.updatePreference(notificationsEnabled = enabled)
        }
    }

    fun updateAdvancedPreferences(transform: (UserPreferences) -> UserPreferences) {
        userRepository.updateAdvancedPreferences(transform)
        _statusBannerMessage.value = "✅ Preferences saved & synced."
    }

    fun claimChallengeQuest(questId: String) {
        val xp = gamificationRepository.claimChallengeReward(questId)
        if (xp > 0) {
            userRepository.awardUserXp(xp, "Claimed Challenge Quest")
            _statusBannerMessage.value = "🏆 Claimed +$xp XP from Challenge Quest!"
        }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            watchRepository?.clearWatchHistory()
            _statusBannerMessage.value = "Watch history cleared."
        }
    }
}
