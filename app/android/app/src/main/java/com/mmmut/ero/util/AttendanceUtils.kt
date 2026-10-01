package com.mmmut.ero.util

sealed interface LeaveInfo {
    data object None : LeaveInfo
    data class CanSkip(val count: Int) : LeaveInfo
    data class MustAttend(val count: Int) : LeaveInfo
}

object AttendanceUtils {
    fun computeLeaveInfo(present: Int, absent: Int, targetPct: Double): LeaveInfo {
        val total = present + absent
        if (total == 0) return LeaveInfo.None
        val target = targetPct / 100.0
        val current = present.toDouble() / total
        return if (current >= target) {
            val x = kotlin.math.floor(present / target - total + 1e-9).toInt()
            LeaveInfo.CanSkip(maxOf(0, x))
        } else {
            val y = kotlin.math.ceil((target * total - present) / (1 - target) - 1e-9).toInt()
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
}
