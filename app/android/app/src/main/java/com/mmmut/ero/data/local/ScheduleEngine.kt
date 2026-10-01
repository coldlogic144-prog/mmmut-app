package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.TimetableCell
import kotlin.math.abs

// NOTE: unit-test JVM build has no Android SDK; TimetableCell import intentionally unused in pure logic.
data class GridUnit(val code: String, val name: String, val type: String)

object ScheduleEngine {
    private fun hashSeed(s: String): () -> Double {
        var h = 1779033703 xor s.length
        for (c in s) {
            h = h xor c.code
            h = imul(h, 3432918353.toInt())
            h = (h shl 13) or (h ushr 19)
        }
        return {
            h = imul(h xor (h ushr 16), 2246822519.toInt())
            h = imul(h xor (h ushr 13), 3266489917.toInt())
            h = h xor (h ushr 16)
            (h ushr 0).toLong().toDouble() / 4294967296.0
        }
    }

    private fun imul(a: Int, b: Int): Int = (a.toLong() * b.toLong()).toInt()

    private fun <T> seededShuffle(list: List<T>, rng: () -> Double): MutableList<T> {
        val a = list.toMutableList()
        if (a.size <= 1) return a
        for (i in a.size - 1 downTo 1) {
            val j = (rng() * (i + 1)).toInt().coerceIn(0, i)
            val t = a[i]; a[i] = a[j]; a[j] = t
        }
        return a
    }

    fun buildWeekGrid(branch: Branch, section: String): Map<String, Map<String, GridUnit>> {
        val rng = hashSeed(branch.id + "::" + section)
        val grid = mutableMapOf<String, MutableMap<String, GridUnit?>>()
        AcademicData.DAYS.forEach { d ->
            grid[d] = mutableMapOf()
            AcademicData.TEACH_PERIODS.forEach { p -> grid[d]!![p.key] = null }
        }
        val dayHas = mutableMapOf<String, MutableSet<String>>()
        AcademicData.DAYS.forEach { dayHas[it] = mutableSetOf() }
        var units = mutableListOf<GridUnit>()
        branch.subjects.forEach { s ->
            repeat(s.l) { units.add(GridUnit(s.code, s.name, "Lecture")) }
            repeat(s.t) { units.add(GridUnit(s.code, s.name, "Tutorial")) }
        }
        units = seededShuffle(units, rng)
        val morning = mutableListOf<Pair<String, String>>()
        AcademicData.DAYS.forEach { d -> listOf("I", "II", "III", "IV").forEach { k -> morning.add(d to k) } }
        val shuffledMorning = seededShuffle(morning, rng)
        val remaining = units.toMutableList()
        for ((day, key) in shuffledMorning) {
            if (remaining.isEmpty()) break
            var idx = remaining.indexOfFirst { !dayHas[day]!!.contains(it.code) }
            if (idx == -1) idx = 0
            val u = remaining.removeAt(idx)
            grid[day]!![key] = u
            dayHas[day]!!.add(u.code)
        }
        var blocks = mutableListOf<GridUnit>()
        branch.subjects.forEach { s ->
            val count = maxOf(0, Math.round(s.p / 2.0).toInt())
            repeat(count) { blocks.add(GridUnit(s.code, s.name, "Practical")) }
        }
        blocks = seededShuffle(blocks, rng)
        val pairs = mutableListOf<Pair<String, List<String>>>()
        AcademicData.DAYS.forEach { d ->
            pairs.add(d to listOf("V", "VI")); pairs.add(d to listOf("VII", "VIII"))
        }
        val shuffledPairs = seededShuffle(pairs, rng)
        val usedPair = mutableSetOf<String>()
        fun place(block: GridUnit, avoidSameDay: Boolean): Boolean {
            for ((day, keys) in shuffledPairs) {
                val pid = "$day:${keys[0]}"
                if (usedPair.contains(pid)) continue
                if (avoidSameDay && dayHas[day]!!.contains(block.code)) continue
                grid[day]!![keys[0]] = block; grid[day]!![keys[1]] = block
                dayHas[day]!!.add(block.code); usedPair.add(pid)
                return true
            }
            return false
        }
        blocks.forEach { if (!place(it, true)) place(it, false) }
        val free = GridUnit("—", "Self Study / Library", "Free")
        AcademicData.DAYS.forEach { d ->
            AcademicData.TEACH_PERIODS.forEach { p -> if (grid[d]!![p.key] == null) grid[d]!![p.key] = free }
        }
        @Suppress("UNCHECKED_CAST")
        return grid as Map<String, Map<String, GridUnit>>
    }

    fun dayCells(branch: Branch, section: String, day: String): List<TimetableCell> {
        val grid = buildWeekGrid(branch, section)
        val dayMap = grid[day] ?: return emptyList()
        return AcademicData.TEACH_PERIODS.map { p ->
            val u = dayMap[p.key]!!
            TimetableCell(p.key, u.code, u.name, u.type, p.start, p.end)
        }
    }

    fun tokenDocId(token: String): String {
        var hash = 0
        for (c in token) hash = ((hash shl 5) - hash + c.code)
        return "android_" + abs(hash).toString(36)
    }
}
