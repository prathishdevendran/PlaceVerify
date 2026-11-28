package com.example.placeverify

import com.example.placeverify.data.roleFromClaims
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthorizedRoleTest {
    @Test fun privilegedRolesRequireRecognizedClaims() {
        assertEquals("STUDENT", roleFromClaims(null))
        assertEquals("STUDENT", roleFromClaims("admin"))
        assertEquals("STUDENT", roleFromClaims(true))
        assertEquals("PLACEMENT_OFFICER", roleFromClaims("PLACEMENT_OFFICER"))
        assertEquals("SUPER_ADMIN", roleFromClaims("SUPER_ADMIN"))
    }
}
