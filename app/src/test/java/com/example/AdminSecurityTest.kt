package com.example

import com.example.security.AdminSecurityManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AdminSecurityTest {

    @Before
    fun setup() {
        AdminSecurityManager.logout()
    }

    @Test
    fun `test admin authentication with robiul1000 password`() {
        assertFalse(AdminSecurityManager.isAdminAuthenticated.value)
        assertFalse(AdminSecurityManager.authenticate("wrong_pass"))
        assertFalse(AdminSecurityManager.isAdminAuthenticated.value)

        val success = AdminSecurityManager.authenticate("robiul1000")
        assertTrue(success)
        assertTrue(AdminSecurityManager.isAdminAuthenticated.value)
    }

    @Test
    fun `test brute force lockout after 5 failed attempts`() {
        repeat(5) {
            assertFalse(AdminSecurityManager.authenticate("hacker_guess_$it"))
        }
        assertTrue(AdminSecurityManager.isLockedOut())
        // Even with valid password during lockout window, access is denied
        assertFalse(AdminSecurityManager.authenticate("robiul1000"))
    }

    @Test
    fun `test anti ddos and waf blocks malicious sql and xss payloads`() {
        val safeResult = AdminSecurityManager.inspectAndSanitizeInput("https://robiulislam.b-cdn.net/anime/ep1.m3u8", "streamUrl")
        assertTrue(safeResult.isSuccess)

        val sqliResult = AdminSecurityManager.inspectAndSanitizeInput("1' UNION SELECT * FROM users --", "search")
        assertTrue(sqliResult.isFailure)

        val xssResult = AdminSecurityManager.inspectAndSanitizeInput("<script>alert(1)</script>", "title")
        assertTrue(xssResult.isFailure)

        val ddosCheck = AdminSecurityManager.checkDdosAndRateLimit("192.168.1.50", "/api/catalog")
        assertTrue(ddosCheck.isSuccess)
    }

    @Test
    fun `test admin RBAC role validation blocks self escalation`() {
        AdminSecurityManager.authenticate("robiul1000")
        val selfEscalation = AdminSecurityManager.validateRoleChange(
            actorUserId = "u_1",
            targetUserId = "u_1",
            newRole = com.example.data.model.UserRole.SUPER_ADMIN
        )
        assertTrue(selfEscalation.isFailure)
    }
}

