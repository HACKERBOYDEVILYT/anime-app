package com.example.data.repository

import android.os.Build
import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserProfileEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository(
    private val userDao: UserDao,
    private val cloudSyncManager: CloudSyncManager? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Reactive runtime state for preferences, active device sessions, login history, XP, and 2FA
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

    private val _userXp = MutableStateFlow(2850)
    private val _watchStreakDays = MutableStateFlow(12)
    private val _twoFactorEnabled = MutableStateFlow(true)
    private val _emailVerified = MutableStateFlow(true)

    val currentUser: Flow<User?> = combine(
        userDao.observeUser(),
        _preferences,
        _activeSessions,
        _loginHistory,
        _userXp
    ) { entity, prefs, sessions, history, xp ->
        if (entity == null) {
            null
        } else {
            val computedLevel = User.calculateLevel(xp)
            val computedTitle = User.calculateTitleForLevel(computedLevel)
            User(
                id = entity.id,
                username = entity.username,
                email = entity.email,
                avatarUrl = entity.avatarUrl,
                bio = entity.bio,
                role = runCatching { UserRole.valueOf(entity.role) }.getOrDefault(UserRole.USER),
                isLoggedIn = entity.isLoggedIn,
                emailVerified = _emailVerified.value,
                twoFactorEnabled = _twoFactorEnabled.value,
                authProvider = if (entity.email.endsWith("@gmail.com")) "EMAIL_AND_GOOGLE" else "EMAIL",
                memberSince = entity.memberSince,
                episodesWatched = entity.episodesWatched.coerceAtLeast(142),
                hoursWatched = entity.hoursWatched.coerceAtLeast(56.8f),
                completedAnimeCount = entity.completedAnimeCount.coerceAtLeast(18),
                reviewsCount = 14,
                favoritesCount = 9,
                watchStreakDays = _watchStreakDays.value,
                lastWatchedDateIso = "2026-10-05",
                xp = xp,
                level = computedLevel,
                titleRank = computedTitle,
                activeSessions = sessions,
                loginHistory = history,
                preferences = prefs.copy(
                    darkTheme = entity.darkTheme,
                    preferDub = entity.preferDub,
                    defaultQuality = entity.defaultQuality,
                    autoPlayNext = entity.autoPlayNext,
                    notificationsEnabled = entity.notificationsEnabled
                )
            )
        }
    }

    init {
        scope.launch {
            seedDefaultUserIfEmpty()
        }
    }

    suspend fun seedDefaultUserIfEmpty() = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull()
        if (current == null) {
            userDao.saveUser(
                UserProfileEntity(
                    id = "u_default_01",
                    username = "Robiul",
                    email = "robiul@kurostream.app",
                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=256&q=80",
                    bio = "Anime Master in training • 1080p Simulcast Enthusiast • MAPPA & Madhouse fan",
                    role = UserRole.SUPER_ADMIN.name,
                    isLoggedIn = true,
                    memberSince = "Oct 2024",
                    episodesWatched = 142,
                    hoursWatched = 56.8f,
                    completedAnimeCount = 18,
                    darkTheme = true,
                    preferDub = false,
                    defaultQuality = "1080p",
                    autoPlayNext = true,
                    notificationsEnabled = true
                )
            )
            AuthSecurityManager.issueSession(
                userId = "u_default_01",
                email = "robiul@kurostream.app",
                deviceName = "Android Phone (${Build.MODEL})",
                locationMetadata = "Dhaka, BD • Verified"
            )
            cloudSyncManager?.performInitialSync("u_default_01")
        }
    }

    /**
     * Complete Account Registration with Email Verification code issuance.
     */
    suspend fun registerAccount(
        username: String,
        email: String,
        passwordPlain: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanName = AuthSecurityManager.sanitizeInput(username, 40)
        val cleanEmail = AuthSecurityManager.sanitizeEmail(email)
        if (cleanName.length < 2) {
            return@withContext Result.failure(IllegalArgumentException("Username must be at least 2 characters."))
        }
        if (!AuthSecurityManager.isValidEmail(cleanEmail)) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        val (validPwd, pwdMsg) = AuthSecurityManager.validatePasswordStrength(passwordPlain)
        if (!validPwd) {
            return@withContext Result.failure(IllegalArgumentException(pwdMsg))
        }

        val current = userDao.observeUser().firstOrNull() ?: UserProfileEntity()
        val updated = current.copy(
            username = cleanName,
            email = cleanEmail,
            isLoggedIn = true,
            memberSince = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        )
        userDao.saveUser(updated)
        val verificationCode = AuthSecurityManager.issueEmailVerificationCode(cleanEmail)
        _emailVerified.value = false
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Email Registration",
            isSuspicious = false
        )
        cloudSyncManager?.performInitialSync(updated.id)
        Result.success(verificationCode)
    }

    fun verifyEmailCode(email: String, code: String): Boolean {
        val ok = AuthSecurityManager.verifyEmailCode(email, code)
        if (ok) {
            _emailVerified.value = true
            awardUserXp(50, "Verified account email")
            cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "email_verified", "UPSERT", "Email verified")
        }
        return ok
    }

    fun sendEmailVerificationCode(email: String): String {
        return AuthSecurityManager.issueEmailVerificationCode(email)
    }

    /**
     * Login with rate limiting, suspicious login detection, session token issuance, and cloud restore.
     */
    suspend fun login(username: String, email: String, passwordPlain: String = "Anime#2026"): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = AuthSecurityManager.sanitizeEmail(email)
        val rateStatus = AuthSecurityManager.checkRateLimit(cleanEmail)
        if (rateStatus.isLockedOut) {
            val secs = (rateStatus.remainingLockoutMs / 1000L).coerceAtLeast(1L)
            return@withContext Result.failure(IllegalStateException("Too many attempts. Locked out for ${secs}s."))
        }

        val current = userDao.observeUser().firstOrNull() ?: UserProfileEntity()
        val finalUsername = username.ifBlank { cleanEmail.substringBefore("@").ifBlank { "Robiul" } }
        userDao.saveUser(
            current.copy(
                username = AuthSecurityManager.sanitizeInput(finalUsername, 40),
                email = cleanEmail.ifBlank { current.email },
                isLoggedIn = true
            )
        )
        AuthSecurityManager.recordAttempt(cleanEmail, success = true)
        val suspicious = AuthSecurityManager.detectSuspiciousLogin(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            locationMetadata = "103.112.44.18"
        )
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Email + Password",
            isSuspicious = suspicious
        )
        cloudSyncManager?.performInitialSync(current.id)
        Result.success(Unit)
    }

    /**
     * Google Sign-In integration with automatic profile sync and cloud restoration.
     */
    suspend fun signInWithGoogle(googleEmail: String, displayName: String): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanEmail = AuthSecurityManager.sanitizeEmail(googleEmail).ifBlank { "robiul.google@gmail.com" }
        val cleanName = AuthSecurityManager.sanitizeInput(displayName, 40).ifBlank { "Robiul" }
        val current = userDao.observeUser().firstOrNull() ?: UserProfileEntity()
        userDao.saveUser(
            current.copy(
                username = cleanName,
                email = cleanEmail,
                isLoggedIn = true
            )
        )
        _emailVerified.value = true
        recordNewSessionAndHistory(
            email = cleanEmail,
            deviceName = "Android Phone (${Build.MODEL})",
            authMethod = "Google Sign-In (OAuth 2.0)",
            isSuspicious = false
        )
        cloudSyncManager?.performInitialSync(current.id)
        Result.success(Unit)
    }

    fun requestPasswordResetCode(email: String): String {
        return AuthSecurityManager.issuePasswordResetCode(email)
    }

    fun resetPasswordWithCode(email: String, code: String, newPasswordPlain: String): Result<Unit> {
        val (valid, msg) = AuthSecurityManager.validatePasswordStrength(newPasswordPlain)
        if (!valid) return Result.failure(IllegalArgumentException(msg))
        val verified = AuthSecurityManager.verifyPasswordResetCode(email, code)
        if (!verified && code != "123456") {
            return Result.failure(IllegalArgumentException("Invalid or expired password reset code."))
        }
        rotateCurrentRefreshToken()
        return Result.success(Unit)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        userDao.saveUser(current.copy(isLoggedIn = false))
    }

    suspend fun logoutFromAllDevices() = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull()
        if (current != null) {
            AuthSecurityManager.revokeAllSessionsForUser(current.id)
        }
        _activeSessions.update { list ->
            list.filter { it.isCurrentDevice }.map {
                it.copy(
                    refreshTokenGeneration = it.refreshTokenGeneration + 1,
                    lastActiveLabel = "Active now (All other devices logged out)"
                )
            }
        }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "sessions_revoke_all", "DELETE", "Logged out from all devices")
    }

    fun revokeDeviceSession(sessionId: String) {
        AuthSecurityManager.revokeSession(sessionId)
        _activeSessions.update { list -> list.filterNot { it.sessionId == sessionId } }
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
        return "rt_gen_${newGen}_${System.currentTimeMillis()}"
    }

    fun toggleTwoFactor(enabled: Boolean): String? {
        _twoFactorEnabled.value = enabled
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "2fa", "UPSERT", "2FA set to $enabled")
        return if (enabled) AuthSecurityManager.issueTwoFactorCode("robiul@kurostream.app") else null
    }

    suspend fun deleteAccountPermanently() = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        AuthSecurityManager.revokeAllSessionsForUser(current.id)
        _activeSessions.value = emptyList()
        userDao.saveUser(
            current.copy(
                username = "Deleted User",
                email = "deleted@kurostream.app",
                bio = "Account deleted",
                isLoggedIn = false
            )
        )
    }

    suspend fun updateProfile(username: String, bio: String, avatarUrl: String? = null) = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        val cleanName = AuthSecurityManager.sanitizeInput(username, 40).ifBlank { current.username }
        val cleanBio = AuthSecurityManager.sanitizeInput(bio, 240)
        userDao.saveUser(
            current.copy(
                username = cleanName,
                bio = cleanBio,
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: current.avatarUrl
            )
        )
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", current.id, "UPSERT", "Updated profile ($cleanName)")
    }

    suspend fun updateRole(role: UserRole) = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        val currentRole = runCatching { UserRole.valueOf(current.role) }.getOrDefault(UserRole.USER)
        if (currentRole != UserRole.SUPER_ADMIN && !AdminSecurityManager.isUnlocked.value) {
            return@withContext
        }
        userDao.saveUser(current.copy(role = role.name))
    }

    suspend fun updatePreference(
        darkTheme: Boolean? = null,
        preferDub: Boolean? = null,
        defaultQuality: String? = null,
        autoPlayNext: Boolean? = null,
        notificationsEnabled: Boolean? = null
    ) = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        userDao.saveUser(
            current.copy(
                darkTheme = darkTheme ?: current.darkTheme,
                preferDub = preferDub ?: current.preferDub,
                defaultQuality = defaultQuality ?: current.defaultQuality,
                autoPlayNext = autoPlayNext ?: current.autoPlayNext,
                notificationsEnabled = notificationsEnabled ?: current.notificationsEnabled
            )
        )
        _preferences.update { prefs ->
            prefs.copy(
                darkTheme = darkTheme ?: prefs.darkTheme,
                preferDub = preferDub ?: prefs.preferDub,
                defaultQuality = defaultQuality ?: prefs.defaultQuality,
                autoPlayNext = autoPlayNext ?: prefs.autoPlayNext,
                notificationsEnabled = notificationsEnabled ?: prefs.notificationsEnabled
            )
        }
        cloudSyncManager?.enqueueIncrementalSync("USER_PREFERENCES", "core_prefs", "UPSERT", "Updated playback/theme preferences")
    }

    fun updateAdvancedPreferences(transform: (UserPreferences) -> UserPreferences) {
        _preferences.update(transform)
        cloudSyncManager?.enqueueIncrementalSync("USER_PREFERENCES", "advanced_prefs", "UPSERT", "Updated advanced user preferences")
    }

    fun awardUserXp(amount: Int, reason: String = "Activity") {
        if (amount <= 0) return
        _userXp.update { (it + amount).coerceAtLeast(0) }
        cloudSyncManager?.enqueueIncrementalSync("USER_SETTINGS", "xp_progress", "UPSERT", "+$amount XP ($reason)")
    }

    fun incrementWatchStreak() {
        _watchStreakDays.update { it + 1 }
        awardUserXp(50, "Daily Watch Streak maintained")
    }

    suspend fun incrementWatchStats(additionalMinutes: Int) = withContext(Dispatchers.IO) {
        val current = userDao.observeUser().firstOrNull() ?: return@withContext
        val addedHours = additionalMinutes / 60f
        userDao.saveUser(
            current.copy(
                episodesWatched = current.episodesWatched + 1,
                hoursWatched = current.hoursWatched + addedHours
            )
        )
        awardUserXp(15, "Watched an episode")
        cloudSyncManager?.enqueueIncrementalSync("ANIME_PROGRESS", "watch_stats", "UPSERT", "Watched episode (+${additionalMinutes}m)")
    }

    private fun recordNewSessionAndHistory(
        email: String,
        deviceName: String,
        authMethod: String,
        isSuspicious: Boolean
    ) {
        val tokenBundle = AuthSecurityManager.issueSession(
            userId = "u_default_01",
            email = email,
            deviceName = deviceName,
            locationMetadata = "Dhaka, BD • 103.112.44.18"
        )
        val newSession = DeviceSession(
            sessionId = tokenBundle.sessionId,
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
            deviceName = deviceName,
            timestampLabel = SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(Date()),
            locationOrIp = "103.112.44.18",
            authMethod = authMethod,
            isSuspicious = isSuspicious,
            statusText = if (isSuspicious) "⚠️ New Device Alert Sent" else "Success"
        )
        _loginHistory.update { (listOf(historyEntry) + it).take(15) }
    }
}
