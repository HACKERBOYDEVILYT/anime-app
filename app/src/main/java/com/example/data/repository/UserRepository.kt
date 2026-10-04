package com.example.data.repository

import com.example.data.local.dao.AdminScrapedDao
import com.example.data.local.entity.UserAccountEntity
import com.example.data.model.User
import com.example.data.model.UserPreferences
import com.example.data.network.RetrofitClient
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

class UserRepository(
    private val adminScrapedDao: AdminScrapedDao? = null
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private data class StoredAccount(
        val user: User,
        val salt: String,
        val passwordHash: String
    )

    // Real registered accounts cache (synchronized with Room SQLite) — NO fake demo accounts
    private val accounts = mutableMapOf<String, StoredAccount>()

    private val unauthenticatedUser = User(
        id = "",
        username = "Not Signed In",
        email = "Sign in or create a real account",
        avatarUrl = "https://api.dicebear.com/7.x/identicon/png?seed=KuroUser",
        tier = "Unverified",
        watchTimeHours = 0f,
        episodesWatched = 0,
        joinDate = "-"
    )

    private val _currentUser = MutableStateFlow(unauthenticatedUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(null)
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    init {
        // Restore real registered accounts and active session from Room SQLite
        adminScrapedDao?.let { dao ->
            scope.launch {
                dao.getAllUserAccounts().collect { entities ->
                    entities.forEach { entity ->
                        val userObj = User(
                            id = entity.userId,
                            username = entity.username,
                            email = entity.email,
                            avatarUrl = entity.avatarUrl,
                            tier = entity.tier,
                            watchTimeHours = entity.watchTimeHours,
                            episodesWatched = entity.episodesWatched,
                            joinDate = entity.joinDate
                        )
                        accounts[entity.email.lowercase()] = StoredAccount(
                            user = userObj,
                            salt = entity.salt,
                            passwordHash = entity.passwordHash
                        )
                        if (entity.isActiveSession && !_isLoggedIn.value) {
                            val token = AuthSecurityManager.generateSessionToken(userObj.id)
                            _authToken.value = token
                            _currentUser.value = userObj
                            _isLoggedIn.value = true
                            RetrofitClient.setAuthToken(token)
                        }
                    }
                }
            }
        }
    }

    /**
     * Authenticates a real registered user with salted password verification.
     */
    fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val account = accounts[normalizedEmail] ?: return Result.failure(
            IllegalArgumentException("No registered account found for '$normalizedEmail'. Please register first.")
        )

        val isValid = AuthSecurityManager.verifyPassword(password, account.salt, account.passwordHash)
        return if (isValid) {
            val token = AuthSecurityManager.generateSessionToken(account.user.id)
            _authToken.value = token
            _currentUser.value = account.user
            _isLoggedIn.value = true
            RetrofitClient.setAuthToken(token)

            adminScrapedDao?.let { dao ->
                scope.launch {
                    dao.clearAllActiveSessions()
                    dao.setActiveSession(normalizedEmail)
                }
            }
            Result.success(account.user)
        } else {
            Result.failure(IllegalArgumentException("Incorrect password. Please verify and try again."))
        }
    }

    /**
     * Registers a real user account with cryptographic salting and persists it to Room SQLite.
     */
    fun register(username: String, email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        if (username.isBlank()) {
            return Result.failure(IllegalArgumentException("Username cannot be empty."))
        }
        if (!normalizedEmail.contains("@")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (accounts.containsKey(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("An account already exists with this email address."))
        }

        val validationError = AuthSecurityManager.validatePasswordStrength(password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val salt = AuthSecurityManager.generateSalt()
        val hash = AuthSecurityManager.hashPassword(password, salt)
        val joinDateStr = SimpleDateFormat("MMM yyyy", Locale.US).format(Date())
        val newUser = User(
            id = "usr_${System.currentTimeMillis() % 100000}",
            username = username.trim(),
            email = normalizedEmail,
            avatarUrl = "https://api.dicebear.com/7.x/bottts/png?seed=${username.trim()}",
            tier = "Verified Member",
            watchTimeHours = 0f,
            episodesWatched = 0,
            joinDate = joinDateStr
        )

        accounts[normalizedEmail] = StoredAccount(newUser, salt, hash)
        val token = AuthSecurityManager.generateSessionToken(newUser.id)
        _authToken.value = token
        _currentUser.value = newUser
        _isLoggedIn.value = true
        RetrofitClient.setAuthToken(token)

        adminScrapedDao?.let { dao ->
            scope.launch {
                dao.clearAllActiveSessions()
                dao.insertUserAccount(
                    UserAccountEntity(
                        email = normalizedEmail,
                        userId = newUser.id,
                        username = newUser.username,
                        salt = salt,
                        passwordHash = hash,
                        avatarUrl = newUser.avatarUrl,
                        tier = newUser.tier,
                        watchTimeHours = 0f,
                        episodesWatched = 0,
                        joinDate = joinDateStr,
                        isActiveSession = true
                    )
                )
            }
        }

        return Result.success(newUser)
    }

    /**
     * Terminate active user session and return to unauthenticated state.
     */
    fun logout() {
        _isLoggedIn.value = false
        _authToken.value = null
        _currentUser.value = unauthenticatedUser.copy(preferences = _currentUser.value.preferences)
        RetrofitClient.setAuthToken(null)
        adminScrapedDao?.let { dao ->
            scope.launch {
                dao.clearAllActiveSessions()
            }
        }
    }

    fun updatePreferences(newPrefs: UserPreferences) {
        _currentUser.update { it.copy(preferences = newPrefs) }
    }

    fun updateProfile(username: String, email: String, avatarUrl: String) {
        _currentUser.update { it.copy(username = username, email = email, avatarUrl = avatarUrl) }
    }

    fun incrementWatchTime(minutes: Float) {
        _currentUser.update {
            val updated = it.copy(
                watchTimeHours = it.watchTimeHours + (minutes / 60f),
                episodesWatched = it.episodesWatched + 1
            )
            if (_isLoggedIn.value && updated.email.contains("@")) {
                val existing = accounts[updated.email.lowercase()]
                if (existing != null) {
                    accounts[updated.email.lowercase()] = existing.copy(user = updated)
                    adminScrapedDao?.let { dao ->
                        scope.launch {
                            dao.insertUserAccount(
                                UserAccountEntity(
                                    email = updated.email.lowercase(),
                                    userId = updated.id,
                                    username = updated.username,
                                    salt = existing.salt,
                                    passwordHash = existing.passwordHash,
                                    avatarUrl = updated.avatarUrl,
                                    tier = updated.tier,
                                    watchTimeHours = updated.watchTimeHours,
                                    episodesWatched = updated.episodesWatched,
                                    joinDate = updated.joinDate,
                                    isActiveSession = true
                                )
                            )
                        }
                    }
                }
            }
            updated
        }
    }
}
