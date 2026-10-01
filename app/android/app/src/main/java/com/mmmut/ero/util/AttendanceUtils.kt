package com.mmmut.ero.util

import com.mmmut.ero.data.local.SubjectDef
import kotlin.math.ceil
import kotlin.math.floor

sealed interface LeaveInfo {
    data object None : LeaveInfo
    data class CanSkip(val count: Int) : LeaveInfo
    data class MustAttend(val count: Int) : LeaveInfo
}

data class SubjectAttendanceStats(
    val subjectCode: String,
    val subjectName: String,
    val present: Int,
    val absent: Int
) {
    val total: Int get() = present + absent
    val pct: Double get() = if (total == 0) 0.0 else (present * 100.0 / total)
    val status: String get() = if (pct >= 75.0 || total == 0) "On Track" else "Needs Attention"
}

object AttendanceUtils {
    fun computeLeaveInfo(present: Int, absent: Int, targetPct: Double): LeaveInfo {
        val total = present + absent
        if (total == 0) return LeaveInfo.None
        val target = targetPct / 100.0
        val current = present.toDouble() / total
        return if (current >= target) {
            val x = floor(present / target - total + 1e-9).toInt()
            LeaveInfo.CanSkip(maxOf(0, x))
        } else {
            val y = ceil((target * total - present) / (1 - target) - 1e-9).toInt()
            LeaveInfo.MustAttend(maxOf(1, y))
        }
    }

    fun summarize(map: Map<String, Map<String, String>>): Triple<Int, Int, Double> {
        var p = 0; var a = 0
        map.values.forEach { day -> day.values.forEach { s ->
            when (s) { "present" -> p++; "absent" -> a++ }
        } }
        val pct = if (p + a == 0) 0.0 else p * 100.0 / (p + a)
        return Triple(p, a, pct)
    }

    fun subjectBreakdown(
        map: Map<String, Map<String, String>>,
        subjects: List<SubjectDef>
    ): List<SubjectAttendanceStats> {
        val pMap = mutableMapOf<String, Int>()
        val aMap = mutableMapOf<String, Int>()

        map.values.forEach { day ->
            day.forEach { (key, status) ->
                val code = if (key.contains("::")) key.substringAfter("::") else key
                when (status) {
                    "present" -> pMap[code] = (pMap[code] ?: 0) + 1
                    "absent" -> aMap[code] = (aMap[code] ?: 0) + 1
                }
            }
        }

        return subjects.map { s ->
            val p = pMap[s.code] ?: 0
            val a = aMap[s.code] ?: 0
            SubjectAttendanceStats(s.code, s.name, p, a)
        }
    }

    fun historyForSubject(
        map: Map<String, Map<String, String>>,
        subjectCode: String
    ): List<Triple<String, String, String>> {
        val list = mutableListOf<Triple<String, String, String>>()
        map.forEach { (date, dayMap) ->
            dayMap.forEach { (key, status) ->
                val code = if (key.contains("::")) key.substringAfter("::") else key
                val period = if (key.contains("::")) key.substringBefore("::") else ""
                if (code.equals(subjectCode, ignoreCase = true)) {
                    list.add(Triple(date, period, status))
                }
            }
        }
        return list.sortedByDescending { it.first }
    }
}
