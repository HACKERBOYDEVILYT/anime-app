package com.example.data.repository

import com.example.data.model.User
import com.example.data.model.UserPreferences
import com.example.data.network.RetrofitClient
import com.example.security.AuthSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UserRepository {

    private data class StoredAccount(
        val user: User,
        val salt: String,
        val passwordHash: String
    )

    // In-memory persistent accounts database with initial default test account
    private val accounts = mutableMapOf<String, StoredAccount>()

    init {
        // Initialize default demo account (securely salted)
        val defaultUser = User(
            id = "usr_001",
            username = "OtakuMaster",
            email = "otaku@kurostream.app",
            avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
            tier = "Ultra VIP",
            watchTimeHours = 38.5f,
            episodesWatched = 76,
            joinDate = "Oct 2025"
        )
        val salt = AuthSecurityManager.generateSalt()
        val hash = AuthSecurityManager.hashPassword("robiul10000", salt)
        accounts[defaultUser.email.lowercase()] = StoredAccount(defaultUser, salt, hash)
    }

    private val _currentUser = MutableStateFlow(accounts.values.first().user)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _authToken = MutableStateFlow<String?>(AuthSecurityManager.generateSessionToken("usr_001"))
    val authToken: StateFlow<String?> = _authToken.asStateFlow()

    /**
     * Authenticates a user with salted password verification.
     */
    fun login(email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        val account = accounts[normalizedEmail] ?: return Result.failure(
            IllegalArgumentException("No account found with this email address.")
        )

        val isValid = AuthSecurityManager.verifyPassword(password, account.salt, account.passwordHash)
        return if (isValid) {
            val token = AuthSecurityManager.generateSessionToken(account.user.id)
            _authToken.value = token
            _currentUser.value = account.user
            _isLoggedIn.value = true
            RetrofitClient.setAuthToken(token)
            Result.success(account.user)
        } else {
            Result.failure(IllegalArgumentException("Incorrect password. Please verify and try again."))
        }
    }

    /**
     * Registers a new user account with cryptographic salting and hashing.
     */
    fun register(username: String, email: String, password: String): Result<User> {
        val normalizedEmail = email.trim().lowercase()
        if (accounts.containsKey(normalizedEmail)) {
            return Result.failure(IllegalArgumentException("An account already exists with this email address."))
        }

        val validationError = AuthSecurityManager.validatePasswordStrength(password)
        if (validationError != null) {
            return Result.failure(IllegalArgumentException(validationError))
        }

        val salt = AuthSecurityManager.generateSalt()
        val hash = AuthSecurityManager.hashPassword(password, salt)
        val newUser = User(
            id = "usr_${System.currentTimeMillis() % 100000}",
            username = username.trim(),
            email = normalizedEmail,
            avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400",
            tier = "Ultra VIP",
            watchTimeHours = 0f,
            episodesWatched = 0,
            joinDate = "Just now"
        )

        accounts[normalizedEmail] = StoredAccount(newUser, salt, hash)
        val token = AuthSecurityManager.generateSessionToken(newUser.id)
        _authToken.value = token
        _currentUser.value = newUser
        _isLoggedIn.value = true
        RetrofitClient.setAuthToken(token)

        return Result.success(newUser)
    }

    /**
     * Terminate active user session.
     */
    fun logout() {
        _isLoggedIn.value = false
        _authToken.value = null
        RetrofitClient.setAuthToken(null)
    }

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
