package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Handles cryptographic hashing, salted password verification, and authentication tokens.
 */
object AuthSecurityManager {

    private val random = SecureRandom()

    /**
     * Generates a cryptographically random hex salt.
     */
    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes a SHA-256 hash of password + salt.
     */
    fun hashPassword(password: String, salt: String): String {
        val combined = "$salt:$password"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies a password against the stored salt and hash.
     */
    fun verifyPassword(password: String, salt: String, storedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return timingSafeEquals(computedHash, storedHash)
    }

    /**
     * Generates a secure session authorization token.
     */
    fun generateSessionToken(userId: String): String {
        val entropy = ByteArray(24)
        random.nextBytes(entropy)
        val tokenPayload = entropy.joinToString("") { "%02x".format(it) }
        return "kuro_${userId}_$tokenPayload"
    }

    /**
     * Validates password strength (minimum length).
     */
    fun validatePasswordStrength(password: String): String? {
        return when {
            password.length < 6 -> "Password must be at least 6 characters long."
            else -> null
        }
    }

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
}
