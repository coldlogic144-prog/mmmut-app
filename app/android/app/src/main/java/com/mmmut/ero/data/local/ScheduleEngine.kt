package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.model.TimetableEntry
import kotlin.math.abs

object ScheduleEngine {

    /**
     * Filter official timetable entries for a specific student profile.
     * Rules:
     * - LECTURE: show if branch, semester, and section match.
     * - TUTORIAL: show only if tutorialGroup matches (or if no group is required).
     * - PRACTICAL: show only if practicalGroup matches (or if no group is required).
     * - Never merge or leak T1/T2 or P1/P2.
     * - Multi-period sessions remain single sessions spanning startPeriod to endPeriod.
     * - Sorted chronologically by start time.
     */
    fun filterEntriesForStudent(
        entries: List<TimetableEntry>,
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): List<TimetableEntry> {
        val bNorm = branchId.trim().lowercase()
        val secNorm = section.trim().uppercase().ifBlank { "A" }
        val tutNorm = tutorialGroup.trim().uppercase()
        val pracNorm = practicalGroup.trim().uppercase()

        return entries.filter { entry ->
            if (entry.branch.lowercase() != bNorm) return@filter false
            if (entry.semester != semester) return@filter false
            if (entry.section.uppercase() != secNorm) return@filter false

            when (entry.classType.uppercase()) {
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
     * Get the full week timetable (Monday -> Saturday) filtered for the student.
     */
    fun getWeek(
        branchId: String,
        semester: Int,
        section: String,
        tutorialGroup: String,
        practicalGroup: String
    ): Map<String, List<TimetableCell>> {
        val filteredEntries = filterEntriesForStudent(
            OfficialTimetableData.ALL_ENTRIES,
            branchId,
            semester,
            section,
            tutorialGroup,
            practicalGroup
        )

        val weekMap = mutableMapOf<String, MutableList<TimetableCell>>()
        AcademicData.DAYS.forEach { day -> weekMap[day] = mutableListOf() }

        filteredEntries.forEach { entry ->
            weekMap[entry.day]?.add(entry.toCell())
        }

        // Sort each day's cells chronologically
        val result = mutableMapOf<String, List<TimetableCell>>()
        weekMap.forEach { (day, cells) ->
            result[day] = cells.sortedWith(compareBy({ timeToMinutes(it.start) }, { periodOrder(it.startPeriod) }))
        }
        return result
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
        return getWeek(branchId, semester, section, tutorialGroup, practicalGroup)[day] ?: emptyList()
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

    fun tokenDocId(token: String): String {
        var hash = 0
        for (c in token) hash = ((hash shl 5) - hash + c.code)
        return "android_" + abs(hash).toString(36)
    }
}
