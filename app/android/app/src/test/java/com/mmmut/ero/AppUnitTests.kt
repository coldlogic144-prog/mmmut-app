package com.mmmut.ero

import com.mmmut.ero.data.auth.AuthEmail
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.local.SyllabusData
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

class TimetableArchitectureTest {

    private fun makeProfile(
        branchId: String = "civil",
        semester: Int = 1,
        section: String = "A",
        tutorialGroup: String = "T1",
        practicalGroup: String = "P1"
    ) = com.mmmut.ero.data.model.StudentProfile(
        uid = "test-uid",
        name = "Test Student",
        branchId = branchId,
        semester = semester,
        section = section,
        tutorialGroup = tutorialGroup,
        practicalGroup = practicalGroup
    )

    @Test
    fun profile1_civil_I_A_T1_P1_verifications() {
        val p = makeProfile(tutorialGroup = "T1", practicalGroup = "P1")
        val timetable = ScheduleEngine.getTimetableForStudent(p)

        // Must contain lectures
        assertTrue(timetable.any { it.type == "LECTURE" && it.subjectCode == "BSM-131" })
        assertTrue(timetable.any { it.type == "LECTURE" && it.subjectCode == "BSM-110" })
        assertTrue(timetable.any { it.type == "LECTURE" && it.subjectCode == "BIT-103" })

        // Must contain T1 tutorials
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T1" && it.subjectCode == "BSM-110" })
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T1" && it.subjectCode == "BHS-101" })

        // MUST NOT contain T2 tutorials
        assertTrue(timetable.none { it.type == "TUTORIAL" && it.tutorialGroup == "T2" })

        // Must contain P1 practicals
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P1" && it.subjectCode == "BCE-121" })
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P1" && it.subjectCode == "BSM-131" })
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P1" && it.subjectCode == "BIT-103" })

        // MUST NOT contain P2 practicals
        assertTrue(timetable.none { it.type == "PRACTICAL" && it.practicalGroup == "P2" })
    }

    @Test
    fun profile2_civil_I_A_T1_P2_verifications() {
        val p = makeProfile(tutorialGroup = "T1", practicalGroup = "P2")
        val timetable = ScheduleEngine.getTimetableForStudent(p)

        // Must contain T1, MUST NOT contain T2
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T1" })
        assertTrue(timetable.none { it.type == "TUTORIAL" && it.tutorialGroup == "T2" })

        // Must contain P2, MUST NOT contain P1
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P2" && it.subjectCode == "BSM-131" })
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P2" && it.subjectCode == "BIT-103" })
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P2" && it.subjectCode == "BCE-121" })
        assertTrue(timetable.none { it.type == "PRACTICAL" && it.practicalGroup == "P1" })
    }

    @Test
    fun profile3_civil_I_A_T2_P1_verifications() {
        val p = makeProfile(tutorialGroup = "T2", practicalGroup = "P1")
        val timetable = ScheduleEngine.getTimetableForStudent(p)

        // Must contain T2, MUST NOT contain T1
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T2" && it.subjectCode == "BSM-110" })
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T2" && it.subjectCode == "BHS-101" })
        assertTrue(timetable.none { it.type == "TUTORIAL" && it.tutorialGroup == "T1" })

        // Must contain P1, MUST NOT contain P2
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P1" })
        assertTrue(timetable.none { it.type == "PRACTICAL" && it.practicalGroup == "P2" })
    }

    @Test
    fun profile4_civil_I_A_T2_P2_verifications() {
        val p = makeProfile(tutorialGroup = "T2", practicalGroup = "P2")
        val timetable = ScheduleEngine.getTimetableForStudent(p)

        // Must contain T2, MUST NOT contain T1
        assertTrue(timetable.any { it.type == "TUTORIAL" && it.tutorialGroup == "T2" })
        assertTrue(timetable.none { it.type == "TUTORIAL" && it.tutorialGroup == "T1" })

        // Must contain P2, MUST NOT contain P1
        assertTrue(timetable.any { it.type == "PRACTICAL" && it.practicalGroup == "P2" })
        assertTrue(timetable.none { it.type == "PRACTICAL" && it.practicalGroup == "P1" })
    }

    @Test
    fun simultaneousClassesPreservedInternally() {
        // In the master official timetable dataset, multiple classes MUST coexist at the same day & time
        val wedAfternoon = com.mmmut.ero.data.local.OfficialTimetableData.ALL_ENTRIES.filter {
            it.branch == "civil" && it.section == "A" && it.day == "Wednesday" && it.startTime == "14:00"
        }
        assertEquals(2, wedAfternoon.size)
        assertTrue(wedAfternoon.any { it.practicalGroup == "P1" && it.subjectCode == "BCE-121" && it.room == "L-104" })
        assertTrue(wedAfternoon.any { it.practicalGroup == "P2" && it.subjectCode == "BSM-131" && it.room == "Physics Lab" })

        val thuAfternoon = com.mmmut.ero.data.local.OfficialTimetableData.ALL_ENTRIES.filter {
            it.branch == "civil" && it.section == "A" && it.day == "Thursday" && it.startTime == "14:00"
        }
        assertEquals(2, thuAfternoon.size)
        assertTrue(thuAfternoon.any { it.practicalGroup == "P1" && it.subjectCode == "BSM-131" })
        assertTrue(thuAfternoon.any { it.practicalGroup == "P2" && it.subjectCode == "BCE-121" })
    }

    @Test
    fun multiPeriodPracticalsPreserved() {
        val p2 = makeProfile(tutorialGroup = "T1", practicalGroup = "P2")
        val timetable = ScheduleEngine.getTimetableForStudent(p2)

        // Thursday morning BIT-103 practical is a single continuous session 09:10 - 10:50 (Period I-II)
        val bitPrac = timetable.first { it.subjectCode == "BIT-103" && it.type == "PRACTICAL" }
        assertEquals("09:10", bitPrac.startTime)
        assertEquals("10:50", bitPrac.endTime)
        assertEquals("I", bitPrac.startPeriod)
        assertEquals("II", bitPrac.endPeriod)

        // Wednesday afternoon BSM-131 practical is a single continuous session 14:00 - 15:30 (Period V-VI)
        val phyPrac = timetable.first { it.subjectCode == "BSM-131" && it.type == "PRACTICAL" }
        assertEquals("14:00", phyPrac.startTime)
        assertEquals("15:30", phyPrac.endTime)
        assertEquals("V", phyPrac.startPeriod)
        assertEquals("VI", phyPrac.endPeriod)
    }

    @Test
    fun branchAndSectionFilteringWorks() {
        val civilA = makeProfile(branchId = "civil", section = "A")
        val entriesA = ScheduleEngine.getTimetableForStudent(civilA)
        assertTrue(entriesA.all { it.branch.equals("civil", ignoreCase = true) })
        assertTrue(entriesA.all { it.section.equals("A", ignoreCase = true) })

        val civilB = makeProfile(branchId = "civil", section = "B")
        val entriesB = ScheduleEngine.getTimetableForStudent(civilB)
        assertTrue(entriesB.all { it.branch.equals("civil", ignoreCase = true) })
        assertTrue(entriesB.all { it.section.equals("B", ignoreCase = true) })
    }

    @Test
    fun currentAndNextClassDetection() {
        val p = makeProfile(tutorialGroup = "T1", practicalGroup = "P2")
        val mondayCells = ScheduleEngine.getToday(p, "Monday")

        // 09:30 is during Period I (09:10 - 10:00) BSM-131
        val current = ScheduleEngine.getCurrentClass(mondayCells, "09:30")
        assertNotNull(current)
        assertEquals("BSM-131", current?.subjectCode)

        // Next class after 09:30 is Period II (10:00 - 10:50) BSM-110
        val next = ScheduleEngine.getNextClass(mondayCells, "09:30")
        assertNotNull(next)
        assertEquals("BSM-110", next?.subjectCode)
    }

    @Test
    fun generateDebugReport() {
        val testProfiles = listOf(
            makeProfile(tutorialGroup = "T1", practicalGroup = "P1"),
            makeProfile(tutorialGroup = "T1", practicalGroup = "P2"),
            makeProfile(tutorialGroup = "T2", practicalGroup = "P1"),
            makeProfile(tutorialGroup = "T2", practicalGroup = "P2")
        )

        for (profile in testProfiles) {
            val label = "Student: ${profile.branchId.uppercase()} / Sem ${profile.semester} / Sec ${profile.section} / ${profile.tutorialGroup} / ${profile.practicalGroup}"
            println("==================================================")
            println(label)
            println("==================================================")

            val weekGrid = ScheduleEngine.getWeek(profile)
            AcademicData.DAYS.forEach { day ->
                val dayList = weekGrid[day] ?: emptyList()
                if (dayList.isNotEmpty()) {
                    println("\n${day.uppercase()}")
                    dayList.forEach { c ->
                        val groupStr = c.tutorialGroup ?: c.practicalGroup ?: "ALL"
                        println("${c.start}–${c.end} | ${c.subjectCode} | ${c.type.uppercase()} | Group: $groupStr | Room: ${c.room} | Teacher: ${c.teacher}")
                    }
                }
            }
        }
    }

    @Test
    fun branchNormalizationTest() {
        assertEquals("ece", ScheduleEngine.canonicalBranchId("ECE"))
        assertEquals("ece", ScheduleEngine.canonicalBranchId("Electronics & Communication Engineering"))
        assertEquals("ece", ScheduleEngine.canonicalBranchId("B.Tech (Electronics & Comm. Engineering)"))
        assertEquals("ece", ScheduleEngine.canonicalBranchId("ECD"))

        assertEquals("ee", ScheduleEngine.canonicalBranchId("EE"))
        assertEquals("ee", ScheduleEngine.canonicalBranchId("Electrical Engineering"))
        assertEquals("ee", ScheduleEngine.canonicalBranchId("B.Tech (Electrical Engineering)"))
        assertEquals("ee", ScheduleEngine.canonicalBranchId("EED"))

        assertEquals("me", ScheduleEngine.canonicalBranchId("ME"))
        assertEquals("me", ScheduleEngine.canonicalBranchId("Mechanical Engineering"))
        assertEquals("me", ScheduleEngine.canonicalBranchId("B.Tech (Mechanical Engineering)"))
        assertEquals("me", ScheduleEngine.canonicalBranchId("MED"))

        assertEquals("it", ScheduleEngine.canonicalBranchId("IT"))
        assertEquals("it", ScheduleEngine.canonicalBranchId("Information Technology"))
        assertEquals("it", ScheduleEngine.canonicalBranchId("B.Tech (Information Technology)"))
        assertEquals("it", ScheduleEngine.canonicalBranchId("ITC"))

        assertEquals("chemical", ScheduleEngine.canonicalBranchId("Chemical"))
        assertEquals("chemical", ScheduleEngine.canonicalBranchId("Chemical Engineering"))
        assertEquals("chemical", ScheduleEngine.canonicalBranchId("B.Tech (Chemical Engineering)"))
        assertEquals("chemical", ScheduleEngine.canonicalBranchId("CHD"))

        assertEquals("cse", ScheduleEngine.canonicalBranchId("CSE"))
        assertEquals("cse", ScheduleEngine.canonicalBranchId("Computer Science & Engineering"))
        assertEquals("cse", ScheduleEngine.canonicalBranchId("B.Tech (Computer Sc. & Engineering)"))
        assertEquals("cse", ScheduleEngine.canonicalBranchId("CSD"))

        assertEquals("eceiot", ScheduleEngine.canonicalBranchId("B.Tech,ECE(IOT)"))
        assertEquals("eceiot", ScheduleEngine.canonicalBranchId("IOT"))

        assertEquals("bba", ScheduleEngine.canonicalBranchId("BBA"))
        assertEquals("bba", ScheduleEngine.canonicalBranchId(":BBA"))

        assertEquals("bpharm", ScheduleEngine.canonicalBranchId("B.Pharm"))
        assertEquals("bpharm", ScheduleEngine.canonicalBranchId(":B.Pharm"))
    }

    @Test
    fun sectionNormalizationTest() {
        assertEquals("A", ScheduleEngine.canonicalSection("SEC-A"))
        assertEquals("A", ScheduleEngine.canonicalSection("Section A"))
        assertEquals("A", ScheduleEngine.canonicalSection("SEC A"))
        assertEquals("A", ScheduleEngine.canonicalSection("a"))
        assertEquals("B", ScheduleEngine.canonicalSection("SEC-B"))
        assertEquals("B", ScheduleEngine.canonicalSection("Section B"))
        assertEquals("C", ScheduleEngine.canonicalSection("SEC-C"))
        assertEquals("D", ScheduleEngine.canonicalSection("SEC-D"))
    }

    @Test
    fun testAllBranchesAndSectionsLoadAccurateData() {
        val testCases = listOf(
            Triple("civil", "A", 24),
            Triple("civil", "B", 24),
            Triple("ece", "A", 25),
            Triple("ece", "B", 25),
            Triple("ece", "C", 25),
            Triple("ee", "A", 26),
            Triple("ee", "B", 26),
            Triple("me", "A", 25),
            Triple("me", "B", 25),
            Triple("it", "A", 24),
            Triple("it", "B", 24),
            Triple("chemical", "A", 24),
            Triple("cse", "A", 24),
            Triple("cse", "B", 24),
            Triple("cse", "C", 24),
            Triple("cse", "D", 24),
            Triple("eceiot", "A", 25),
            Triple("bba", "A", 27),
            Triple("bba", "B", 27),
            Triple("bpharm", "A", 36)
        )

        for ((bId, sec, expectedMasterCount) in testCases) {
            val master = com.mmmut.ero.data.local.OfficialTimetableData.ALL_ENTRIES.filter {
                ScheduleEngine.canonicalBranchId(it.branch) == bId &&
                ScheduleEngine.canonicalSection(it.section) == sec
            }
            assertEquals("Branch $bId Sec $sec total entries mismatch", expectedMasterCount, master.size)

            val studentP = makeProfile(branchId = bId, section = sec, tutorialGroup = "T1", practicalGroup = "P1")
            val studentTimetable = ScheduleEngine.getTimetableForStudent(studentP)
            assertTrue("Branch $bId Sec $sec student timetable must not be empty", studentTimetable.isNotEmpty())
            assertTrue("Branch $bId Sec $sec entries must only belong to section $sec", studentTimetable.all { ScheduleEngine.canonicalSection(it.section) == sec })
            assertTrue("Branch $bId Sec $sec entries must only belong to branch $bId", studentTimetable.all { ScheduleEngine.canonicalBranchId(it.branch) == bId })
        }
    }

    @Test
    fun testEceGroupIsolation() {
        // ECE Sem 1 Sec C: T1 and P2
        val pC_T1_P2 = makeProfile(branchId = "ece", section = "C", tutorialGroup = "T1", practicalGroup = "P2")
        val ttC = ScheduleEngine.getTimetableForStudent(pC_T1_P2)

        // Must receive ECE Sec C lectures
        assertTrue(ttC.any { it.type == "LECTURE" && it.subjectCode == "BSM-110" })
        assertTrue(ttC.any { it.type == "LECTURE" && it.subjectCode == "BSM-140" })
        assertTrue(ttC.any { it.type == "LECTURE" && it.subjectCode == "BEE-110" })

        // Must receive T1 tutorials, NEVER T2
        assertTrue(ttC.any { it.type == "TUTORIAL" && it.tutorialGroup == "T1" })
        assertTrue(ttC.none { it.type == "TUTORIAL" && it.tutorialGroup == "T2" })

        // Must receive P2 practicals, NEVER P1
        assertTrue(ttC.any { it.type == "PRACTICAL" && it.practicalGroup == "P2" })
        assertTrue(ttC.none { it.type == "PRACTICAL" && it.practicalGroup == "P1" })

        // Must never receive ECE Sec A, Sec B, or Civil
        assertTrue(ttC.none { ScheduleEngine.canonicalSection(it.section) != "C" })
        assertTrue(ttC.none { ScheduleEngine.canonicalBranchId(it.branch) != "ece" })
    }

    @Test
    fun testMultiPeriodPracticalsAcrossBranches() {
        // ECE Sec C: Mon P1 practical BEC-106 is I-IV (09:10 - 12:30)
        val eceC_P1 = makeProfile(branchId = "ece", section = "C", tutorialGroup = "T1", practicalGroup = "P1")
        val ttEceC = ScheduleEngine.getTimetableForStudent(eceC_P1)
        val becPrac = ttEceC.first { it.subjectCode == "BEC-106" && it.type == "PRACTICAL" && it.day == "Monday" }
        assertEquals("09:10", becPrac.startTime)
        assertEquals("12:30", becPrac.endTime)
        assertEquals("I", becPrac.startPeriod)
        assertEquals("IV", becPrac.endPeriod)

        // Mechanical Sec A: Tue P2 practical BME-104 is I-IV (09:10 - 12:30)
        val meA_P2 = makeProfile(branchId = "me", section = "A", tutorialGroup = "T1", practicalGroup = "P2")
        val ttMeA = ScheduleEngine.getTimetableForStudent(meA_P2)
        val bmePrac = ttMeA.first { it.subjectCode == "BME-104" && it.type == "PRACTICAL" && it.day == "Tuesday" }
        assertEquals("09:10", bmePrac.startTime)
        assertEquals("12:30", bmePrac.endTime)
        assertEquals("I", bmePrac.startPeriod)
        assertEquals("IV", bmePrac.endPeriod)
    }
}

class TokenIdTest {
    @Test fun tokenDocId_stableAndPrefixed() {
        val t = "fake-token-123"
        assertEquals(TokenId.docId(t), TokenId.docId(t))
        assertTrue(TokenId.docId(t).startsWith("android_"))
        assertEquals(TokenId.docId(t), repoTokenDocId(t))
    }
}

class SyllabusDataTest {
    @Test fun syllabus_loadsExactUnits() {
        val sub = SyllabusData.getSyllabusForSubject("civil", "BSM-110", "Engineering Mathematics I", emptySet())
        assertEquals(4, sub.units.size)
        assertEquals("Unit I", sub.units[0].unitNumber)
        assertTrue(sub.units[0].topics.size >= 5)
    }

    @Test fun syllabus_tracksCompletion() {
        val completedSet = setOf("t|civil|BSM-110|0|0", "t|civil|BSM-110|0|1")
        val sub = SyllabusData.getSyllabusForSubject("civil", "BSM-110", "Engineering Mathematics I", completedSet)
        assertEquals(2, sub.completedTopics)
        assertTrue(sub.percent > 0.0)
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
