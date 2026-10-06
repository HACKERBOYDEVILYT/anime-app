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
        val success = AdminSecurityManager.authenticate("robiul10000")
        assertTrue(success)
        assertTrue(AdminSecurityManager.isAdminAuthenticated.value)
    }

    @Test
    fun `test admin RBAC role validation blocks self escalation`() {
        val selfEscalation = AdminSecurityManager.validateRoleChange(
            actorUserId = "u_1",
            targetUserId = "u_1",
            newRole = com.example.data.model.UserRole.SUPER_ADMIN
        )
        assertTrue(selfEscalation.isFailure)
    }
}
