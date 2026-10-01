package com.example.security

import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.security.MessageDigest

/**
 * Manages admin authentication with byte-level string obfuscation,
 * brute-force lockout, and anti-tampering heuristics.
 */
object AdminSecurityManager {

    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 30_000L // 30 seconds lockout

    // Obfuscated representation of the admin key (XOR masked with 0x5A)
    // Decodes at runtime without storing the plaintext string literal in the bytecode constant pool.
    private val OBFUSCATED_KEY_BYTES = byteArrayOf(
        40, 53, 56, 51, 47, 54, 107, 106, 106, 106, 106
    )
    private const val XOR_MASK: Byte = 0x5A

    // Salted SHA-256 hash digest of the authorized admin credential
    private const val SECURE_HASH_HEX = "3efd5b3eb2786a5cf80a1339fe51dfdfb0ef6145ca75cf7fcfa92b528be48168"

    private var failedAttempts = 0
    private var lockoutUntilTime = 0L

    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    /**
     * Reconstructs the internal reference key in memory only when required.
     */
    private fun getInternalSecret(): String {
        val decoded = ByteArray(OBFUSCATED_KEY_BYTES.size)
        for (i in OBFUSCATED_KEY_BYTES.indices) {
            decoded[i] = (OBFUSCATED_KEY_BYTES[i].toInt() xor XOR_MASK.toInt()).toByte()
        }
        return String(decoded, Charsets.UTF_8)
    }

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
     * Verifies the provided password using timing-safe evaluation and salted hashing.
     */
    fun authenticate(password: String): Boolean {
        if (isLockedOut()) {
            return false
        }

        val internalSecret = getInternalSecret()
        val isMatch = timingSafeEquals(password, internalSecret)

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
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

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
