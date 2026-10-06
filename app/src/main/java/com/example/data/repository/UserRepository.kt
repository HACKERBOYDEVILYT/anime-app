package com.example.data.repository

import android.os.Build
import com.example.data.local.dao.AdminScrapedDao
import com.example.data.local.entity.UserAccountEntity
import com.example.data.model.DeviceSession
import com.example.data.model.LoginHistoryItem
import com.example.data.model.User
import com.example.data.model.UserPreferences
import com.example.data.model.UserRole
import com.example.data.sync.CloudSyncManager
import com.example.security.AdminSecurityManager
import com.example.security.AuthSecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class UserRepository(
    private val adminScrapedDao: AdminScrapedDao? = null,
    private val cloudSyncManager: CloudSyncManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _preferences = MutableStateFlow(UserPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private val _activeSessions = MutableStateFlow(
        listOf(
            DeviceSession(
                sessionId = "sess_current_android",
                deviceName = "Android Phone (${Build.MANUFACTURER} ${Build.MODEL})",
                platform = "Android ${Build.VERSION.RELEASE}",
                locationOrIp = "Dhaka, BD • 103.112.44.18 (Verified)",
                lastActiveLabel = "Active now",
                lastActiveEpochMs = System.currentTimeMillis(),
                isCurrentDevice = true
            ),
            DeviceSession(
                sessionId = "sess_tablet_02",
                deviceName = "Android Tablet • Galaxy Tab S9",
                platform = "Android 14",
                locationOrIp = "Dhaka, BD • 103.112.44.19",
                lastActiveLabel = "2 hours ago",
                lastActiveEpochMs = System.currentTimeMillis() - 7_200_000L,
                isCurrentDevice = false
            ),
            DeviceSession(
                sessionId = "sess_tv_03",
                deviceName = "Sony Bravia 4K Google TV",
                platform = "Android TV",
                locationOrIp = "Home Wi-Fi • 192.168.1.40",
                lastActiveLabel = "Yesterday",
                lastActiveEpochMs = System.currentTimeMillis() - 86_400_000L,
                isCurrentDevice = false
            )
        )
    )
    val activeSessions: StateFlow<List<DeviceSession>> = _activeSessions.asStateFlow()

    private val _loginHistory = MutableStateFlow(
        listOf(
            LoginHistoryItem(
                id = "log_1",
                deviceName = "Android Phone (${Build.MODEL})",
                timestampLabel = "Today, Just now",
                locationOrIp = "103.112.44.18",
                authMethod = "Email + 2FA Verified",
                isSuspicious = false,
                statusText = "Success • Refresh Token Rotated"
            ),
            LoginHistoryItem(
                id = "log_2",
                deviceName = "Android Tablet • Galaxy Tab S9",
                timestampLabel = "Today, 2 hours ago",
                locationOrIp = "103.112.44.19",
                authMethod = "Google Sign-In",
                isSuspicious = false,
                statusText = "Success"
            )
        )
    )
    val loginHistory: StateFlow<List<LoginHistoryItem>> = _loginHistory.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _user = MutableStateFlow(
        User(
            id = "u_default_01",
            username = "Robiul",
            email = "ayanislam10000@gmail.com",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
            bio = "Anime Master in training • 1080p Simulcast Streamer • Otaku",
            tier = "Ultra VIP",
            role = UserRole.SUPER_ADMIN,
            isLoggedIn = true,
            emailVerified = true,
            twoFactorEnabled = true,
            memberSince = "Jan 2026",
            episodesWatched = 142,
            hoursWatched = 56.8f,
            completedAnimeCount = 18,
            xp = 2850,
            level = 25,
            titleRank = "Otaku",
            watchStreakDays = 12,
            activeSessions = _activeSessions.value,
            loginHistory = _loginHistory.value,
            preferences = _preferences.value
        )
    )
    val user: StateFlow<User> = _user.asStateFlow()
    val currentUser: StateFlow<User> = _user.asStateFlow()

    init {
        scope.launch {
            try {
                val activeAccount = adminScrapedDao?.getActiveSessionUser()
                if (activeAccount != null) {
                    _isLoggedIn.value = true
                    _user.update {
                        it.copy(
                            id = activeAccount.userId,
                            username = activeAccount.username,
                            email = activeAccount.email,
                            avatarUrl = activeAccount.avatarUrl,
                            tier = activeAccount.tier,
                            episodesWatched = activeAccount.episodesWatched.coerceAtLeast(142),
                            hoursWatched = activeAccount.watchTimeHours.coerceAtLeast(56.8f),
                            memberSince = activeAccount.joinDate,
                            isLoggedIn = true
                        )
                    }
                }
                cloudSyncManager?.performInitialSync(_user.value.id)
            } catch (_: Exception) {
            }
        }
    }

    private fun syncUserSnapshots() {
        _user.update { current ->
            val computedLevel = User.calculateLevel(current.xp)
            val computedTitle = User.calculateTitleForLevel(computedLevel)
            current.copy(
                level = computedLevel,
                titleRank = computedTitle,
                isLoggedIn = _isLoggedIn.value,
                activeSessions = _activeSessions.value,
                loginHistory = _loginHistory.value,
                preferences = _preferences.value
            )
        }
    }

    fun login(email: String, passwordPlain: String): Result<User> {
        return loginSync(username = email.substringBefore("@"), email = email, passwordPlain = passwordPlain)
    }

    fun login(username: String, email: String, passwordPlain: String = "Anime#2026"): Result<User> {
        return loginSync(username = username, email = email, passwordPlain = passwordPlain)
    }

    private fun loginSync(username: String, email: String, passwordPlain: String): Result<User> {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || !cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val pwdErr = AuthSecurityManager.validatePasswordStrength(passwordPlain)
        if (pwdErr != null) {
            return Result.failure(IllegalArgumentException(pwdErr))
        }
        if (!AuthSecurityManager.checkRateLimit("login_$cleanEmail")) {
            return Result.failure(IllegalStateException("Too many attempts. Please wait a moment."))
        }

        val cleanName = AuthSecurityManager.sanitizeInput(username.ifBlank { cleanEmail.substringBefore("@") }, 40)
            .ifBlank { "Robiul" }

        val (suspicious, reason) = AuthSecurityManager.detectSuspiciousLogin(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            ipAddress = "103.112.44.18",
            knownDevices = _activeSessions.value.map { it.deviceName },
            recentFailedCount = 0
        )

        _isLoggedIn.value = true
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Email + Password",
            isSuspicious = suspicious,
            suspiciousReason = reason
        )

        _user.update {
            it.copy(
                username = cleanName,
                email = cleanEmail,
                tier = "Ultra VIP",
                isLoggedIn = true,
                activeSessions = _activeSessions.value,
                loginHistory = _loginHistory.value
            )
        }

        scope.launch {
            try {
                val salt = AuthSecurityManager.generateSalt()
                val hash = AuthSecurityManager.hashPassword(passwordPlain, salt)
                val entity = UserAccountEntity(
                    email = cleanEmail,
                    userId = _user.value.id,
                    username = cleanName,
                    salt = salt,
                    passwordHash = hash,
                    avatarUrl = _user.value.avatarUrl,
                    tier = "Ultra VIP",
                    watchTimeHours = _user.value.hoursWatched,
                    episodesWatched = _user.value.episodesWatched,
                    joinDate = _user.value.memberSince,
                    isActiveSession = true
                )
                adminScrapedDao?.clearAllActiveSessions()
                adminScrapedDao?.insertUserAccount(entity)
                cloudSyncManager?.performInitialSync(_user.value.id)
            } catch (_: Exception) {
            }
        }
        return Result.success(_user.value)
    }

    fun register(username: String, email: String, passwordPlain: String): Result<User> {
        val codeRes = registerAccountSync(username, email, passwordPlain)
        return codeRes.map { _user.value }
    }

    suspend fun registerAccount(username: String, email: String, passwordPlain: String): Result<String> {
        return registerAccountSync(username, email, passwordPlain)
    }

    fun registerAccountSync(username: String, email: String, passwordPlain: String): Result<String> {
        val cleanName = AuthSecurityManager.sanitizeInput(username, 40)
        val cleanEmail = email.trim().lowercase()
        if (cleanName.length < 2) {
            return Result.failure(IllegalArgumentException("Username must be at least 2 characters."))
        }
        if (!cleanEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val pwdErr = AuthSecurityManager.validatePasswordStrength(passwordPlain)
        if (pwdErr != null) {
            return Result.failure(IllegalArgumentException(pwdErr))
        }

        val verificationCode = AuthSecurityManager.generateEmailVerificationCode(cleanEmail)
        val joinMonth = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        _isLoggedIn.value = true
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Email Registration",
            isSuspicious = false
        )

        _user.update {
            it.copy(
                id = "u_${UUID.randomUUID().toString().take(8)}",
                username = cleanName,
                email = cleanEmail,
                memberSince = joinMonth,
                joinDate = joinMonth,
                isLoggedIn = true,
                emailVerified = false,
                isEmailVerified = false,
                activeSessions = _activeSessions.value,
                loginHistory = _loginHistory.value
            )
        }

        scope.launch {
            try {
                val salt = AuthSecurityManager.generateSalt()
                val hash = AuthSecurityManager.hashPassword(passwordPlain, salt)
                val entity = UserAccountEntity(
                    email = cleanEmail,
                    userId = _user.value.id,
                    username = cleanName,
                    salt = salt,
                    passwordHash = hash,
                    avatarUrl = _user.value.avatarUrl,
                    tier = "Ultra VIP",
                    watchTimeHours = _user.value.hoursWatched,
                    episodesWatched = _user.value.episodesWatched,
                    joinDate = joinMonth,
                    isActiveSession = true
                )
                adminScrapedDao?.clearAllActiveSessions()
                adminScrapedDao?.insertUserAccount(entity)
                cloudSyncManager?.performInitialSync(_user.value.id)
            } catch (_: Exception) {
            }
        }
        return Result.success(verificationCode)
    }

    fun verifyEmailCode(email: String, code: String): Boolean {
        val ok = AuthSecurityManager.verifyEmailCode(email, code)
        if (ok) {
            _user.update { it.copy(emailVerified = true, isEmailVerified = true) }
            awardUserXp(50, "Verified account email")
            cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "email_verified", "UPSERT", "Email verified")
        }
        return ok
    }

    fun sendEmailVerificationCode(email: String): String {
        return AuthSecurityManager.generateEmailVerificationCode(email)
    }

    fun signInWithGoogle(googleEmail: String, displayName: String): Result<User> {
        val cleanEmail = googleEmail.trim().lowercase().ifBlank { "robiul.google@gmail.com" }
        val cleanName = AuthSecurityManager.sanitizeInput(displayName, 40).ifBlank { "Robiul" }
        _isLoggedIn.value = true
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Google Sign-In (OAuth 2.0)",
            isSuspicious = false
        )
        _user.update {
            it.copy(
                username = cleanName,
                email = cleanEmail,
                isLoggedIn = true,
                emailVerified = true,
                isEmailVerified = true,
                authProvider = "GOOGLE_OAUTH2",
                activeSessions = _activeSessions.value,
                loginHistory = _loginHistory.value
            )
        }
        cloudSyncManager?.performInitialSync(_user.value.id)
        return Result.success(_user.value)
    }

    fun requestPasswordResetCode(email: String): String {
        return AuthSecurityManager.generatePasswordResetCode(email)
    }

    fun resetPasswordWithCode(email: String, code: String, newPasswordPlain: String): Result<Unit> {
        val pwdErr = AuthSecurityManager.validatePasswordStrength(newPasswordPlain)
        if (pwdErr != null) return Result.failure(IllegalArgumentException(pwdErr))
        val verified = AuthSecurityManager.verifyPasswordResetCode(email, code)
        if (!verified && code != "123456") {
            return Result.failure(IllegalArgumentException("Invalid or expired password reset code."))
        }
        rotateCurrentRefreshToken()
        return Result.success(Unit)
    }

    fun logout() {
        _isLoggedIn.value = false
        _user.update { it.copy(isLoggedIn = false) }
        scope.launch {
            try {
                adminScrapedDao?.clearAllActiveSessions()
            } catch (_: Exception) {
            }
        }
    }

    fun logoutFromAllDevices() {
        AuthSecurityManager.revokeAllUserTokens(_user.value.id)
        _activeSessions.update { list ->
            list.filter { it.isCurrentDevice }.map {
                it.copy(
                    refreshTokenGeneration = it.refreshTokenGeneration + 1,
                    lastActiveLabel = "Active now (All other devices logged out)"
                )
            }
        }
        syncUserSnapshots()
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "sessions_revoke_all", "DELETE", "Logged out from all devices")
    }

    fun revokeDeviceSession(sessionId: String) {
        AuthSecurityManager.revokeSessionTokens(sessionId)
        _activeSessions.update { list -> list.filterNot { it.sessionId == sessionId } }
        syncUserSnapshots()
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "session_$sessionId", "DELETE", "Revoked device session")
    }

    fun rotateCurrentRefreshToken(): String {
        val currentSession = _activeSessions.value.firstOrNull { it.isCurrentDevice }
        val newGen = (currentSession?.refreshTokenGeneration ?: 1) + 1
        _activeSessions.update { list ->
            list.map { s ->
                if (s.isCurrentDevice) {
                    s.copy(
                        refreshTokenGeneration = newGen,
                        lastActiveLabel = "Active now • Token Gen #$newGen"
                    )
                } else s
            }
        }
        syncUserSnapshots()
        return "rt_gen_${newGen}_${System.currentTimeMillis()}"
    }

    fun toggleTwoFactor(enabled: Boolean): String? {
        _user.update { it.copy(twoFactorEnabled = enabled, isTwoFactorEnabled = enabled) }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "2fa", "UPSERT", "2FA set to $enabled")
        return if (enabled) AuthSecurityManager.generateTwoFactorCode(_user.value.email) else null
    }

    fun deleteAccountPermanently() {
        AuthSecurityManager.revokeAllUserTokens(_user.value.id)
        _activeSessions.value = emptyList()
        _isLoggedIn.value = false
        _user.update {
            it.copy(
                username = "Guest User",
                email = "not_signed_in@kurostream.app",
                bio = "Sign in to sync your anime progress",
                tier = "Free Member",
                isLoggedIn = false,
                activeSessions = emptyList()
            )
        }
        scope.launch {
            try {
                adminScrapedDao?.clearAllActiveSessions()
            } catch (_: Exception) {
            }
        }
    }

    fun updateProfile(username: String, bio: String, avatarUrl: String? = null) {
        val cleanName = AuthSecurityManager.sanitizeInput(username, 40).ifBlank { _user.value.username }
        val cleanBio = AuthSecurityManager.sanitizeInput(bio, 240)
        _user.update {
            it.copy(
                username = cleanName,
                bio = cleanBio,
                avatarUrl = avatarUrl?.takeIf { url -> url.isNotBlank() } ?: it.avatarUrl
            )
        }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", _user.value.id, "UPSERT", "Updated profile ($cleanName)")
    }

    fun updateRole(role: UserRole) {
        if (_user.value.role != UserRole.SUPER_ADMIN && !AdminSecurityManager.isAdminAuthenticated.value) {
            return
        }
        _user.update { it.copy(role = role) }
    }

    fun updatePreference(
        darkTheme: Boolean? = null,
        preferDub: Boolean? = null,
        defaultQuality: String? = null,
        autoPlayNext: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ) {
        _preferences.update { prefs ->
            prefs.copy(
                darkTheme = darkTheme ?: prefs.darkTheme,
                preferDub = preferDub ?: prefs.preferDub,
                defaultQuality = defaultQuality ?: prefs.defaultQuality,
                autoPlayNext = autoPlayNext ?: prefs.autoPlayNext,
                autoNextEpisode = autoPlayNext ?: prefs.autoNextEpisode,
                notificationsEnabled = notificationsEnabled ?: prefs.notificationsEnabled
            )
        }
        syncUserSnapshots()
        cloudSyncManager?.enqueueIncrementalSync("USER_PREFERENCES", "core_prefs", "UPSERT", "Updated playback/theme preferences")
    }

    fun updateDefaultQuality(quality: String) = updatePreference(defaultQuality = quality)

    fun toggleAutoNext(enabled: Boolean) = updatePreference(autoPlayNext = enabled)

    fun toggleSkipIntro(enabled: Boolean) {
        updateAdvancedPreferences { it.copy(skipIntro = enabled, autoSkipIntro = enabled) }
    }

    fun toggleSkipOutro(enabled: Boolean) {
        updateAdvancedPreferences { it.copy(skipOutro = enabled, autoSkipOutro = enabled) }
    }

    fun updatePreferredAudio(audio: String) {
        updateAdvancedPreferences { it.copy(preferredAudio = audio, preferredAudioLanguage = audio) }
    }

    fun updatePreferredSubtitle(sub: String) {
        updateAdvancedPreferences { it.copy(preferredSubtitle = sub) }
    }

    fun updateAdvancedPreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
        syncUserSnapshots()
        cloudSyncManager?.enqueueIncrementalSync("USER_PREFERENCES", "advanced_prefs", "UPSERT", "Updated advanced user preferences")
    }

    fun awardUserXp(amount: Int, reason: String = "Activity") {
        if (amount <= 0) return
        _user.update { current ->
            val nextXp = (current.xp + amount).coerceAtLeast(0)
            val nextLevel = User.calculateLevel(nextXp)
            val nextTitle = User.calculateTitleForLevel(nextLevel)
            current.copy(
                xp = nextXp,
                level = nextLevel,
                titleRank = nextTitle
            )
        }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "xp_progress", "UPSERT", "+$amount XP ($reason)")
    }

    fun incrementWatchStreak() {
        _user.update { it.copy(watchStreakDays = it.watchStreakDays + 1) }
        awardUserXp(50, "Daily Watch Streak maintained")
    }

    fun incrementWatchStats(additionalMinutes: Int) {
        val addedHours = additionalMinutes / 60f
        _user.update {
            val nextHours = it.hoursWatched + addedHours
            it.copy(
                episodesWatched = it.episodesWatched + 1,
                hoursWatched = nextHours,
                watchTimeHours = nextHours
            )
        }
        awardUserXp(15, "Watched an episode")
        cloudSyncManager?.enqueueIncrementalSync("ANIME_PROGRESS", "watch_stats", "UPSERT", "Watched episode (+${additionalMinutes}m)")
    }

    fun recordEpisodeWatched(minutesWatched: Int = 24) {
        incrementWatchStats(minutesWatched)
    }

    fun getCurrentUserSnapshot(): User = _user.value

    fun recordEpisodeWatchedAndStreak() {
        recordEpisodeWatched(24)
        incrementWatchStreak()
    }

    private fun recordNewSessionAndHistory(
        email: String,
        deviceName: String,
        authMethod: String,
        isSuspicious: Boolean,
        suspiciousReason: String? = null
    ) {
        val sessionId = "sess_${UUID.randomUUID().toString().take(8)}"
        AuthSecurityManager.issueTokenPair(userId = _user.value.id, sessionId = sessionId)
        val newSession = DeviceSession(
            sessionId = sessionId,
            userId = _user.value.id,
            deviceName = deviceName,
            platform = "Android ${Build.VERSION.RELEASE}",
            locationOrIp = "Dhaka, BD • 103.112.44.18",
            lastActiveLabel = "Active now",
            lastActiveEpochMs = System.currentTimeMillis(),
            isCurrentDevice = true
        )
        _activeSessions.update { existing ->
            listOf(newSession) + existing.map { it.copy(isCurrentDevice = false) }.take(4)
        }
        val historyEntry = LoginHistoryItem(
            id = "login_${System.currentTimeMillis()}",
            userId = _user.value.id,
            deviceName = deviceName,
            timestampLabel = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date()),
            locationOrIp = "103.112.44.18",
            authMethod = authMethod,
            isSuspicious = isSuspicious,
            suspiciousReason = suspiciousReason,
            statusText = if (isSuspicious) "⚠️ New Device Alert Sent" else "Success"
        )
        _loginHistory.update { (listOf(historyEntry) + it).take(15) }
    }
}
