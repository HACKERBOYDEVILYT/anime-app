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
    fun `test admin authentication with correct password`() {
        assertFalse(AdminSecurityManager.isAdminAuthenticated.value)
        val success = AdminSecurityManager.authenticate("robiul10000")
        assertTrue(success)
        assertTrue(AdminSecurityManager.isAdminAuthenticated.value)

        // Logout
        AdminSecurityManager.logout()
        assertFalse(AdminSecurityManager.isAdminAuthenticated.value)
    }

    @Test
    fun `test admin authentication with wrong password fails`() {
        val wrongAttempt = AdminSecurityManager.authenticate("wrong_pass_123")
        assertFalse(wrongAttempt)
        assertFalse(AdminSecurityManager.isAdminAuthenticated.value)
    }
}
