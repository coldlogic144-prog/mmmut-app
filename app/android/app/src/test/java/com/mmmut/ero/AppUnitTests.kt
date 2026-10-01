package com.mmmut.ero

import com.mmmut.ero.data.auth.AuthEmail
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.repository.tokenDocId as repoTokenDocId
import com.mmmut.ero.util.TokenId
import com.mmmut.ero.notifications.NotificationDeepLink
import com.mmmut.ero.util.AttendanceUtils
import com.mmmut.ero.util.LeaveInfo
import org.junit.Assert.*
import org.junit.Test

class AuthEmailTest {
    @Test fun emailMapping_matchesWeb() {
        assertEquals("rahul@mmmut.local", AuthEmail.toEmail("rahul"))
        assertEquals("rahul@mmmut.local", AuthEmail.toEmail("Rahul"))
        assertEquals("a@mmmut.local", AuthEmail.toEmail("a@mmmut.local"))
    }
    @Test fun validation_matchesWeb() {
        assertNotNull(AuthEmail.validateUsername("ab"))
        assertNotNull(AuthEmail.validateUsername("BAD!NAME"))
        assertNull(AuthEmail.validateUsername("rahul.cse26"))
        assertNotNull(AuthEmail.validatePassword("12345"))
        assertNull(AuthEmail.validatePassword("123456"))
    }
    @Test fun friendlyErrors() {
        assertEquals("That username is already taken.", AuthEmail.friendlyAuthError("auth/email-already-in-use", "signup"))
        assertEquals("Incorrect username or password.", AuthEmail.friendlyAuthError("auth/wrong-password", "login"))
        assertEquals("No account with that username. Try signing up.", AuthEmail.friendlyAuthError("auth/user-not-found", "login"))
    }
}

class RosterLogicTest {
    @Test fun rollPattern_is10Digits() {
        assertTrue(AcademicData.ROLL_NUMBER_PATTERN.matches("2026011001"))
        assertFalse(AcademicData.ROLL_NUMBER_PATTERN.matches("abc"))
        assertFalse(AcademicData.ROLL_NUMBER_PATTERN.matches("123"))
    }
    @Test fun branchMapping_matchesWeb() {
        assertEquals("cse", AcademicData.rosterBranchToId("CSD"))
        assertEquals("civil", AcademicData.rosterBranchToId("CED"))
        assertEquals("eceiot", AcademicData.rosterBranchToId("iot"))
        assertEquals("it", AcademicData.rosterBranchToId("ITC"))
    }
}

class ScheduleEngineTest {
    @Test fun buildWeekGrid_coversAllDaysAndPeriods() {
        val branch = AcademicDataExtra.getBranch("cse")
        val grid = ScheduleEngine.buildWeekGrid(branch, "A")
        assertEquals(5, grid.size)
        grid.values.forEach { day -> assertEquals(8, day.size) }
    }
    @Test fun buildWeekGrid_isDeterministic() {
        val branch = AcademicDataExtra.getBranch("ee")
        val a = ScheduleEngine.buildWeekGrid(branch, "B")
        val b = ScheduleEngine.buildWeekGrid(branch, "B")
        assertEquals(a, b)
    }
    @Test fun tokenDocId_stableAndPrefixed() {
        val t = "fake-token-123"
        assertEquals(TokenId.docId(t), TokenId.docId(t))
        assertTrue(TokenId.docId(t).startsWith("android_"))
        assertEquals(TokenId.docId(t), repoTokenDocId(t))
    }
}

class AttendanceUtilsTest {
    @Test fun emptyMap_returnsNone() {
        assertTrue(AttendanceUtils.computeLeaveInfo(0, 0, 75.0) is LeaveInfo.None)
    }
    @Test fun highAttendance_canSkip() {
        val r = AttendanceUtils.computeLeaveInfo(15, 1, 75.0)
        assertTrue(r is LeaveInfo.CanSkip)
    }
    @Test fun lowAttendance_mustAttend() {
        val r = AttendanceUtils.computeLeaveInfo(3, 5, 75.0)
        assertTrue(r is LeaveInfo.MustAttend)
    }
    @Test fun summarize_counts() {
        val (p, a, pct) = AttendanceUtils.summarize(mapOf("2026-09-30" to mapOf("I::X" to "present", "II::Y" to "absent")))
        assertEquals(1, p); assertEquals(1, a); assertEquals(50.0, pct, 0.001)
    }
}

class DeepLinkTest {
    @Test fun routes_neverJustHome() {
        assertEquals("notice/abc", NotificationDeepLink.routeFor("academic", "abc"))
        assertEquals("academics", NotificationDeepLink.routeFor("examination", "x"))
        assertEquals("hostel", NotificationDeepLink.routeFor("hostel", ""))
        assertEquals("notice/z", NotificationDeepLink.routeFor("emergency", "z"))
    }
    @Test fun parses_mmmut_scheme() {
        assertEquals("notice/abc", NotificationDeepLink.parseUri("mmmut://notice/abc"))
        assertEquals("hostel", NotificationDeepLink.parseUri("mmmut://hostel"))
        assertNull(NotificationDeepLink.parseUri("https://example.com"))
    }
}
