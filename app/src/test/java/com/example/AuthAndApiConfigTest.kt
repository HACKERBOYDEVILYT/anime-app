package com.example

import com.example.data.network.RetrofitClient
import com.example.data.repository.AdminRepository
import com.example.data.repository.LocalLicensedMediaProvider
import com.example.data.repository.UserRepository
import com.example.security.AuthSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAndApiConfigTest {

    @Test
    fun `test secure password salting and verification`() {
        val salt = AuthSecurityManager.generateSalt()
        val hash = AuthSecurityManager.hashPassword("SuperSecret123", salt)

        // Verify correct password matches
        val isValid = AuthSecurityManager.verifyPassword("SuperSecret123", salt, hash)
        assertTrue(isValid)

        // Verify incorrect password fails
        val isInvalid = AuthSecurityManager.verifyPassword("WrongPassword", salt, hash)
        assertFalse(isInvalid)
    }

    @Test
    fun `test user repository secure login and register`() {
        val userRepo = UserRepository()

        // 0. Ensure fresh install has no account logged in by default
        assertFalse(userRepo.isLoggedIn.value)

        // 1. Test demo login
        val loginResult = userRepo.login("otaku@kurostream.app", "Robiul#2026")
        assertTrue(loginResult.isSuccess)
        assertTrue(userRepo.isLoggedIn.value)
        assertNotNull(userRepo.authToken.value)

        // 2. Test registration
        val registerResult = userRepo.register("Shinobi99", "shinobi@kuro.stream", "Secret#456")
        assertTrue(registerResult.isSuccess)
        assertEquals("Shinobi99", registerResult.getOrNull()?.username)
        assertEquals("shinobi@kuro.stream", userRepo.currentUser.value.email)

        // 3. Test logout
        userRepo.logout()
        assertFalse(userRepo.isLoggedIn.value)
    }

    @Test
    fun `test admin dynamic API configuration and real-time base URL switching`() {
        val mediaProvider = LocalLicensedMediaProvider()
        val adminRepo = AdminRepository(mediaProvider)

        val initialCount = adminRepo.apiConfigs.value.size
        assertTrue(initialCount >= 4)

        // 1. Add Custom API Endpoint
        adminRepo.addApiConfig(
            name = "Singapore Edge Mirror",
            baseUrl = "https://sg-edge.kurostream.app/",
            category = "Streaming HLS",
            apiKey = "token_sg_123"
        )

        val updatedList = adminRepo.apiConfigs.value
        assertEquals(initialCount + 1, updatedList.size)
        val customApi = updatedList.first { it.name == "Singapore Edge Mirror" }
        assertEquals("https://sg-edge.kurostream.app/", customApi.baseUrl)

        // 2. Toggle custom API active state
        adminRepo.setActiveApi(customApi.id)

        // 3. Delete custom API
        adminRepo.deleteApiConfig(customApi.id)
        assertEquals(initialCount, adminRepo.apiConfigs.value.size)
    }
}
