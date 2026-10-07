package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.model.TimetableEntry
import kotlin.math.abs

object ScheduleEngine {

    /**
     * CANONICAL CENTRAL TIMETABLE FILTER FUNCTION:
     * getTimetableForStudent(studentProfile, allEntries)
     *
     * This is the single source of truth for student timetable generation.
     * All screens (Home, Today, Week, Current class, Next class, Details) use this function.
     *
     * Strict filtering rules:
     * 1. Filter branch (case-insensitive)
     * 2. Filter semester
     * 3. Filter section (case-insensitive)
     * 4. Filter tutorial group:
     *    - T1 -> only T1 students
     *    - T2 -> only T2 students
     *    - null -> applicable to all students in that branch/sem/sec
     * 5. Filter practical group:
     *    - P1 -> only P1 students
     *    - P2 -> only P2 students
     *    - null -> applicable to all students in that branch/sem/sec
     * 6. Group rules:
     *    - NEVER merge groups
     *    - NEVER display both groups to the same student
     * 7. Multi-period practicals:
     *    - Preserved as single continuous sessions with actual duration from the PDF
     * 8. Sorting:
     *    - Chronologically sorted by start time (and period order).
     */
    fun getTimetableForStudent(
        studentProfile: StudentProfile,
        allEntries: List<TimetableEntry> = OfficialTimetableData.ALL_ENTRIES
    ): List<TimetableEntry> {
        return getTimetableForStudent(
            branchId = studentProfile.branchId,
            semester = studentProfile.semester,
            section = studentProfile.section,
            tutorialGroup = studentProfile.tutorialGroup,
            practicalGroup = studentProfile.practicalGroup,
            allEntries = allEntries
        )
    }

    data class StudentSchedule(
        val entries: List<TimetableEntry>,
        val weekMap: Map<String, List<TimetableCell>>
    )

    private val scheduleCache = java.util.concurrent.ConcurrentHashMap<String, StudentSchedule>()

    fun clearCache() {
        scheduleCache.clear()
    }

    private fun cacheKey(branchId: String, semester: Int, section: String, tutorialGroup: String, practicalGroup: String): String {
        return "${canonicalBranchId(branchId)}|$semester|${canonicalSection(section)}|${tutorialGroup.trim().uppercase()}|${practicalGroup.trim().uppercase()}"
    }

    /**
     * Get or derive cached student schedule (entries + sorted weekly cells).
     */
    fun getStudentSchedule(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        allEntries: List<TimetableEntry> = OfficialTimetableData.ALL_ENTRIES
    ): StudentSchedule {
        val isDefault = allEntries === OfficialTimetableData.ALL_ENTRIES
        if (isDefault) {
            val key = cacheKey(branchId, semester, section, tutorialGroup, practicalGroup)
            val cached = scheduleCache[key]
            if (cached != null) return cached
        }

        val filteredEntries = filterEntriesDirect(branchId, semester, section, tutorialGroup, practicalGroup, allEntries)
        val weekMap = mutableMapOf<String, MutableList<TimetableCell>>()
        AcademicData.DAYS.forEach { day -> weekMap[day] = mutableListOf() }
        filteredEntries.forEach { entry -> weekMap[entry.day]?.add(entry.toCell()) }

        val sortedWeek = mutableMapOf<String, List<TimetableCell>>()
        weekMap.forEach { (day, cells) ->
            sortedWeek[day] = cells.sortedWith(compareBy({ timeToMinutes(it.start) }, { periodOrder(it.startPeriod) }))
        }

        val schedule = StudentSchedule(filteredEntries, sortedWeek)
        if (isDefault) {
            val key = cacheKey(branchId, semester, section, tutorialGroup, practicalGroup)
            scheduleCache[key] = schedule
        }
        return schedule
    }

    /**
     * Parameterized overload of getTimetableForStudent.
     */
    fun getTimetableForStudent(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        allEntries: List<TimetableEntry> = OfficialTimetableData.ALL_ENTRIES
    ): List<TimetableEntry> {
        return getStudentSchedule(branchId, semester, section, tutorialGroup, practicalGroup, allEntries).entries
    }

    private fun filterEntriesDirect(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        allEntries: List<TimetableEntry>
    ): List<TimetableEntry> {
        val bNorm = canonicalBranchId(branchId)
        val secNorm = canonicalSection(section)
        val tutNorm = tutorialGroup.trim().uppercase()
        val pracNorm = practicalGroup.trim().uppercase()

        return allEntries.filter { entry ->
            // 1. Filter branch
            if (canonicalBranchId(entry.branch) != bNorm) return@filter false
            // 2. Filter semester
            if (entry.semester != semester) return@filter false
            // 3. Filter section
            if (canonicalSection(entry.section) != secNorm) return@filter false

            // 4 & 5. Filter tutorial and practical groups according to MMMUT rules
            when (entry.type.uppercase()) {
                "LECTURE" -> {
                    val tMatch = entry.tutorialGroup == null || tutNorm == "N/A" || entry.tutorialGroup.equals(tutNorm, ignoreCase = true)
                    val pMatch = entry.practicalGroup == null || pracNorm == "N/A" || entry.practicalGroup.equals(pracNorm, ignoreCase = true)
                    tMatch && pMatch
                }
                "TUTORIAL" -> {
                    if (entry.tutorialGroup == null) {
                        true
                    } else if (tutNorm == "N/A" || tutNorm.isBlank()) {
                        false
                    } else {
                        entry.tutorialGroup.equals(tutNorm, ignoreCase = true)
                    }
                }
                "PRACTICAL" -> {
                    if (entry.practicalGroup == null) {
                        true
                    } else if (pracNorm == "N/A" || pracNorm.isBlank()) {
                        false
                    } else {
                        entry.practicalGroup.equals(pracNorm, ignoreCase = true)
                    }
                }
                else -> {
                    val tMatch = entry.tutorialGroup == null || tutNorm == "N/A" || entry.tutorialGroup.equals(tutNorm, ignoreCase = true)
                    val pMatch = entry.practicalGroup == null || pracNorm == "N/A" || entry.practicalGroup.equals(pracNorm, ignoreCase = true)
                    tMatch && pMatch
                }
            }
        }.sortedWith(compareBy({ timeToMinutes(it.startTime) }, { periodOrder(it.startPeriod) }))
    }

    /**
     * Backward-compatible alias for getTimetableForStudent.
     */
    fun filterEntriesForStudent(
        entries: List<TimetableEntry>,
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): List<TimetableEntry> {
        return getTimetableForStudent(
            branchId = branchId,
            semester = semester,
            section = section,
            tutorialGroup = tutorialGroup,
            practicalGroup = practicalGroup,
            allEntries = entries
        )
    }

    /**
     * Get the full week timetable (Monday -> Saturday) filtered for the student.
     */
    fun getWeek(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): Map<String, List<TimetableCell>> {
        return getStudentSchedule(branchId, semester, section, tutorialGroup, practicalGroup).weekMap
    }

    fun getWeek(studentProfile: StudentProfile): Map<String, List<TimetableCell>> {
        return getWeek(
            branchId = studentProfile.branchId,
            semester = studentProfile.semester,
            section = studentProfile.section,
            tutorialGroup = studentProfile.tutorialGroup,
            practicalGroup = studentProfile.practicalGroup
        )
    }

    /**
     * Get today's classes filtered for the student.
     */
    fun getToday(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String,
        dayName: String? = null
    ): List<TimetableCell> {
        val day = dayName ?: com.mmmut.ero.util.TimeUtils.todayName() ?: "Monday"
        return getStudentSchedule(branchId, semester, section, tutorialGroup, practicalGroup).weekMap[day] ?: emptyList()
    }

    fun getToday(
        studentProfile: StudentProfile,
        dayName: String? = null
    ): List<TimetableCell> {
        return getToday(
            branchId = studentProfile.branchId,
            semester = studentProfile.semester,
            section = studentProfile.section,
            tutorialGroup = studentProfile.tutorialGroup,
            practicalGroup = studentProfile.practicalGroup,
            dayName = dayName
        )
    }

    /**
     * Compute current active class from student's filtered timetable.
     */
    fun getCurrentClass(todayCells: List<TimetableCell>, nowTimeStr: String): TimetableCell? {
        return todayCells.firstOrNull { cell ->
            cell.subjectCode != "—" &&
            !cell.type.equals("Free", ignoreCase = true) &&
            nowTimeStr >= cell.start &&
            nowTimeStr <= cell.end
        }
    }

    /**
     * Compute next upcoming class from student's filtered timetable.
     */
    fun getNextClass(todayCells: List<TimetableCell>, nowTimeStr: String): TimetableCell? {
        return todayCells.firstOrNull { cell ->
            cell.subjectCode != "—" &&
            !cell.type.equals("Free", ignoreCase = true) &&
            nowTimeStr < cell.start
        }
    }

    /**
     * Legacy filter method preserved for backwards compatibility with any remaining callers.
     */
    fun filterCellsForStudent(
        cells: List<TimetableCell>,
        tutorialGroup: String,
        practicalGroup: String
    ): List<TimetableCell> {
        val tutNorm = tutorialGroup.trim().uppercase()
        val pracNorm = practicalGroup.trim().uppercase()

        return cells.filter { cell ->
            val isTut = cell.type.equals("Tutorial", ignoreCase = true)
            val isPrac = cell.type.equals("Practical", ignoreCase = true)

            when {
                isTut -> cell.tutorialGroup == null || cell.tutorialGroup.equals(tutNorm, ignoreCase = true)
                isPrac -> cell.practicalGroup == null || cell.practicalGroup.equals(pracNorm, ignoreCase = true)
                else -> {
                    val tMatch = cell.tutorialGroup == null || cell.tutorialGroup.equals(tutNorm, ignoreCase = true)
                    val pMatch = cell.practicalGroup == null || cell.practicalGroup.equals(pracNorm, ignoreCase = true)
                    tMatch && pMatch
                }
            }
        }.sortedWith(compareBy({ timeToMinutes(it.start) }, { periodOrder(it.startPeriod) }))
    }

    fun dayCells(branch: Branch, section: String, day: String): List<TimetableCell> {
        return getWeek(branch.id, 1, section, "T1", "P1")[day] ?: emptyList()
    }

    private fun periodOrder(period: String): Int = when (period.trim().uppercase()) {
        "I" -> 1
        "II" -> 2
        "III" -> 3
        "IV" -> 4
        "V" -> 5
        "VI" -> 6
        "VII" -> 7
        "VIII" -> 8
        else -> 9
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.trim().split(":")
        if (parts.size != 2) return 0
        return (parts[0].toIntOrNull() ?: 0) * 60 + (parts[1].toIntOrNull() ?: 0)
    }

    /**
     * Canonical Branch Normalization:
     * Maps any official or shorthand display name (e.g., "B.Tech (Electronics & Comm. Engineering)",
     * "ECE", "ECD", "Electronics & Communication Engineering") to standard internal branch id.
     */
    fun canonicalBranchId(raw: String): String {
        val s = raw.trim().lowercase()
        return when {
            // ECE (IoT)
            s == "eceiot" || s == "iot" || s.contains("iot") -> "eceiot"
            // ECE
            s == "ece" || s == "ecd" || s.contains("electronics") -> "ece"
            // Electrical
            s == "ee" || s == "eed" || s.contains("electrical") -> "ee"
            // Mechanical
            s == "me" || s == "med" || s.contains("mechanical") -> "me"
            // Civil
            s == "civil" || s == "ced" || s.contains("civil") -> "civil"
            // CSE
            s == "cse" || s == "csd" || s.contains("computer") -> "cse"
            // IT
            s == "it" || s == "itc" || s.contains("information technology") -> "it"
            // Chemical
            s == "chemical" || s == "chd" || s.contains("chemical") -> "chemical"
            // BBA
            s == "bba" || s.contains("bba") || s.contains("management studies") -> "bba"
            // B.Pharm
            s == "bpharm" || s.contains("pharm") -> "bpharm"
            else -> s
        }
    }

    /**
     * Canonical Section Normalization:
     * Normalizes inputs like "SEC-A", "Section A", "SEC A", "a" to "A".
     */
    fun canonicalSection(raw: String): String {
        val clean = raw.trim().uppercase()
            .replace("SECTION", "")
            .replace("SEC", "")
            .replace("-", "")
            .replace(" ", "")
            .trim()
        return when (clean) {
            "A", "1" -> "A"
            "B", "2" -> "B"
            "C", "3" -> "C"
            "D", "4" -> "D"
            else -> if (clean.isNotEmpty()) clean.take(1) else "A"
        }
    }

    fun tokenDocId(token: String): String {
        var hash = 0
        for (c in token) hash = ((hash shl 5) - hash + c.code)
        return "android_" + abs(hash).toString(36)
    }
}
