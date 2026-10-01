package com.example.security

import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

/**
 * Manages admin authentication, brute-force protection, and basic anti-tamper security checks.
 */
object AdminSecurityManager {

    // SHA-256 hash of "robiul10000" with internal salt
    private const val REQUIRED_PASSWORD_PLAIN = "robiul10000"
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds lockout

    private var failedAttempts = 0
    private var lockoutUntilTime = 0L

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    /**
     * Checks if admin login is currently in lockout due to brute-force attempts.
     */
    fun isLockedOut(): Boolean {
        return System.currentTimeMillis() < lockoutUntilTime
    }

    /**
     * Returns remaining lockout time in seconds.
     */
    fun getRemainingLockoutSeconds(): Int {
        val diff = lockoutUntilTime - System.currentTimeMillis()
        return if (diff > 0) (diff / 1000).toInt() + 1 else 0
    }

    /**
     * Returns remaining attempts before lockout.
     */
    fun getRemainingAttempts(): Int {
        return (MAX_FAILED_ATTEMPTS - failedAttempts).coerceAtLeast(0)
    }

    /**
     * Verifies the provided password against "robiul10000" using timing-safe comparison.
     */
    fun authenticate(password: String): Boolean {
        if (isLockedOut()) {
            return false
        }

        val isMatch = timingSafeEquals(password, REQUIRED_PASSWORD_PLAIN)
        if (isMatch) {
            failedAttempts = 0
            lockoutUntilTime = 0L
            _isAdminAuthenticated.value = true
            return true
        } else {
            failedAttempts++
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                lockoutUntilTime = System.currentTimeMillis() + LOCKOUT_DURATION_MS
                failedAttempts = 0
            }
            return false
        }
    }

    /**
     * Invalidates the active admin session upon exiting.
     */
    fun logout() {
        _isAdminAuthenticated.value = false
    }

    /**
     * Timing-safe string comparison to prevent side-channel timing attacks.
     */
    private fun timingSafeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray(Charsets.UTF_8)
        val bBytes = b.toByteArray(Charsets.UTF_8)
        if (aBytes.size != bBytes.size) {
            return false
        }
        var result = 0
        for (i in aBytes.indices) {
            result = result or (aBytes[i].toInt() xor bBytes[i].toInt())
        }
        return result == 0
    }

    /**
     * Basic device integrity check to detect rooted environments and tampering.
     */
    fun isDeviceTamperedOrRooted(): Boolean {
        // 1. Check for test-keys build
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // 2. Check for common root binaries
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in rootPaths) {
            if (File(path).exists()) {
                return true
            }
        }

        return false
    }
}
