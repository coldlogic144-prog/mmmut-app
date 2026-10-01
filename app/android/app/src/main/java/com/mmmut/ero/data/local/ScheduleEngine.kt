package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.TimetableCell
import kotlin.math.abs

data class GridUnit(
    val code: String,
    val name: String,
    val type: String,
    val tutorialGroup: String? = null,
    val practicalGroup: String? = null
)

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

    fun getCivilSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-206"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BIT-103", "Programming in C", "Lecture", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "—", "L", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Lecture", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "BSM-110", "Engineering Mathematics I", "Tutorial", "16:15", "17:00", "T1", null, "TL-206")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BHS-101", "Universal Human Values", "Tutorial", "10:50", "11:40", "T1", null, "TL-206"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T2", null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BHS-101", "Universal Human Values", "Tutorial", "10:50", "11:40", "T2", null, "TL-206"),
            TimetableCell("IV", "—", "H C N U L", "Lecture", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Practical", "14:00", "14:45", null, "P2", "TL-206"),
            TimetableCell("VI", "BCE-121", "Engineering Graphics", "Practical", "14:45", "15:30", null, "P1", "TL-206"),
            TimetableCell("VII", "—", "AnS/AS", "Lecture", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BIT-103", "Programming in C", "Practical", "10:00", "10:50", null, "P2", "TL-206"),
            TimetableCell("III", "BIT-103", "Programming in C", "Lecture", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Practical", "14:00", "14:45", null, "P1", "TL-206"),
            TimetableCell("VI", "BCE-121", "Engineering Graphics", "Practical", "14:45", "15:30", null, "P2", "TL-206"),
            TimetableCell("VII", "—", "AD/An", "Lecture", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BIT-103", "Programming in C", "Lecture", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "BHS-101", "Universal Human Values", "Lecture", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "BCE-121", "Engineering Graphics", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "BSM-110", "Engineering Mathematics I", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BIT-103", "Programming in C", "Practical", "10:00", "10:50", null, "P1", "TL-206"),
            TimetableCell("III", "BCE-121", "Engineering Graphics", "Lecture", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "BHS-101", "Universal Human Values", "Lecture", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        return schedule
    }

    fun getCivilSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-206"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "—", "AkS/PP", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BCE-121", "Engineering Graphics", "Practical", "10:50", "11:40", null, "P1", "TL-206"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Practical", "11:40", "12:30", null, "P2", "TL-206"),
            TimetableCell("V", "BIT-103", "Programming in C", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "BCE-121", "Engineering Graphics", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "—", "An", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BCE-121", "Engineering Graphics", "Practical", "10:50", "11:40", null, "P2", "TL-206"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Practical", "11:40", "12:30", null, "P1", "TL-206"),
            TimetableCell("V", "BIT-103", "Programming in C", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "BHS-101", "Universal Human Values", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "BIT-103", "Programming in C", "Practical", "10:50", "11:40", null, "P2", "TL-206"),
            TimetableCell("IV", "BIT-103", "Programming in C", "Lecture", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "—", "L MH", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Lecture", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "BHS-101", "Universal Human Values", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "BHS-101", "Universal Human Values", "Tutorial", "14:45", "15:30", "T1", null, "TL-206"),
            TimetableCell("VII", "—", "T1/BSM- 110//AKS/TL-206", "Tutorial", "15:30", "16:15", "T1", null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-206"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-206"),
            TimetableCell("V", "—", "L NC", "Lecture", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "BCE-121", "Engineering Graphics", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T2", null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-206"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Tutorial", "10:00", "10:50", "T2", null, "TL-206"),
            TimetableCell("III", "—", "3", "Lecture", "10:50", "11:40", null, null, "TL-206"),
            TimetableCell("IV", "BIT-103", "Programming in C", "Practical", "11:40", "12:30", null, "P1", "TL-206"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-206"),
            TimetableCell("VI", "—", "Dr. Mohammad Hasan(MH) &  Akash Kumar Singh (AKS)", "Lecture", "14:45", "15:30", null, null, "TL-206"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-206"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-206")
        )

        return schedule
    }

    fun getCseSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-109"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Practical", "10:00", "10:50", null, "P2", "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BHS-101", "Universal Human Values", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "BCS-110", "Introduction to C Programming", "Practical", "14:45", "15:30", null, "P2", "TL-109"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P1", "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Practical", "10:00", "10:50", null, "P1", "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BCS-111", "Web Designing-1", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P2", "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BCS-111", "Web Designing-1", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:00", "14:45", "T1", null, "TL-109"),
            TimetableCell("VI", "—", "L", "Lecture", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T1", null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T2", null, "TL-109"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T2", null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BCS-110", "Introduction to C Programming", "Practical", "11:40", "12:30", null, "P1", "TL-109"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        return schedule
    }

    fun getCseSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-109"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "—", "TL-109 SKS", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "—", "TL-109", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BCS-110", "Introduction to C Programming", "Practical", "11:40", "12:30", null, "P1", "TL-109"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:00", "14:45", "T1", null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "—", "TL-109", "Lecture", "15:30", "16:15", null, null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BCS-110", "Introduction to C Programming", "Practical", "11:40", "12:30", null, "P2", "TL-109"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T2", null, "TL-109"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T2", null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L KK", "Lecture", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "L", "Lecture", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Lecture", "15:30", "16:15", null, null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BCS-111", "Web Designing-1", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P1", "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-109"),
            TimetableCell("V", "BCS-111", "Web Designing-1", "Lecture", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "BSM-131", "Engineering Physics", "Practical", "14:45", "15:30", null, "P1", "TL-109"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P2", "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-109"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Practical", "10:00", "10:50", null, "P2", "TL-109"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-109"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Tutorial", "11:40", "12:30", "T1", null, "TL-109"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-109"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-109"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-109"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-109")
        )

        return schedule
    }

    fun getCseSecCTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-201"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "—", "L KK", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T1", null, "TL-201"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T2", null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-111", "Web Designing-1", "Practical", "10:50", "11:40", null, "P2", "TL-201"),
            TimetableCell("IV", "BCS-110", "Introduction to C Programming", "Practical", "11:40", "12:30", null, "P1", "TL-201"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T2", null, "TL-201"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T1", null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "—", "L KK", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P1", "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L GF-6", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BCS-111", "Web Designing-1", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Practical", "11:40", "12:30", null, "P1", "TL-201"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "BHS-101", "Universal Human Values", "Lecture", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "BSM-131", "Engineering Physics", "Lecture", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L GF-6", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BCS-111", "Web Designing-1", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "L KK", "Lecture", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Lecture", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "BCS-110", "Introduction to C Programming", "Practical", "16:15", "17:00", null, "P2", "TL-201")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BHS-101", "Universal Human Values", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Practical", "11:40", "12:30", null, "P2", "TL-201"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        return schedule
    }

    fun getCseSecDTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-201"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "]'", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BHS-101", "Universal Human Values", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-110", "Introduction to C Programming", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "BSM-131", "Engineering Physics", "Practical", "14:45", "15:30", null, "P2", "TL-201"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P1", "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L RAM", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BCS-110", "Introduction to C Programming", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-111", "Web Designing-1", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "BCS-111", "Web Designing-1", "Practical", "15:30", "16:15", null, "P2", "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L RAM", "Lecture", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BCS-110", "Introduction to C Programming", "Lecture", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "BCS-111", "Web Designing-1", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Practical", "11:40", "12:30", null, "P1", "TL-201"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "BCS-110", "Introduction to C Programming", "Practical", "14:45", "15:30", null, "P1", "TL-201"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "—", "L HP", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "—", "L", "Lecture", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "BHS-101", "Universal Human Values", "Tutorial", "16:15", "17:00", "T1", null, "TL-201")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-201"),
            TimetableCell("III", "—", "L HP", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-201"),
            TimetableCell("V", "BSM-131", "Engineering Physics", "Lecture", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "—", "T1//BSM- 110/SS/TL-201", "Tutorial", "15:30", "16:15", "T1", null, "TL-201"),
            TimetableCell("VIII", "BHS-101", "Universal Human Values", "Tutorial", "16:15", "17:00", "T2", null, "TL-201")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-201"),
            TimetableCell("II", "BCS-110", "Introduction to C Programming", "Practical", "10:00", "10:50", null, "P2", "TL-201"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-201"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Tutorial", "11:40", "12:30", "T2", null, "TL-201"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-201"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-201"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-201"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-201")
        )

        return schedule
    }

    fun getItSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-203"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BIT-103", "Programming in C", "Practical", "10:00", "10:50", null, "P2", "TL-203"),
            TimetableCell("III", "BIT-104", "Internet and Web Designing", "Practical", "10:50", "11:40", null, "P1", "TL-203"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "BHS-101", "Universal Human Values", "Lecture", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "TL-203", "Lecture", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Practical", "10:00", "10:50", null, "P1", "TL-203"),
            TimetableCell("III", "BIT-104", "Internet and Web Designing", "Practical", "10:50", "11:40", null, "P2", "TL-203"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "TL-203", "Lecture", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "VB L", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "L H C N U L NS", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "BIT-104", "Internet and Web Designing", "Lecture", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T2", null, "TL-203"),
            TimetableCell("VIII", "BSM-131", "Engineering Physics", "Practical", "16:15", "17:00", null, "P2", "TL-203")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "VB", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BIT-103", "Programming in C", "Lecture", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:00", "14:45", "T1", null, "TL-203"),
            TimetableCell("VI", "—", "L", "Lecture", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T1", null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "BIT-103", "Programming in C", "Tutorial", "14:00", "14:45", "T2", null, "TL-203"),
            TimetableCell("VI", "—", "VB", "Lecture", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "TL-203", "Lecture", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "BSM-131", "Engineering Physics", "Practical", "16:15", "17:00", null, "P1", "TL-203")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "—", "L NS TL-203", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "BIT-104", "Internet and Web Designing", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        return schedule
    }

    fun getItSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-203"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "VB L", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "TL-203", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "VB", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BIT-103", "Programming in C", "Lecture", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BSM-131", "Engineering Physics", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "BHS-101", "Universal Human Values", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "BHS-101", "Universal Human Values", "Tutorial", "15:30", "16:15", "T2", null, "TL-203"),
            TimetableCell("VIII", "BSM-110", "Engineering Mathematics I", "Tutorial", "16:15", "17:00", "T1", null, "TL-203")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Practical", "10:00", "10:50", null, "P1", "TL-203"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "H C N U L TL-203", "Lecture", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "BIT-103", "Programming in C", "Practical", "14:45", "15:30", null, "P2", "TL-203"),
            TimetableCell("VII", "BIT-104", "Internet and Web Designing", "Practical", "15:30", "16:15", null, "P1", "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BSM-131", "Engineering Physics", "Practical", "10:00", "10:50", null, "P2", "TL-203"),
            TimetableCell("III", "—", "VB", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BHS-101", "Universal Human Values", "Tutorial", "11:40", "12:30", "T2", null, "TL-203"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "BIT-104", "Internet and Web Designing", "Practical", "15:30", "16:15", null, "P2", "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L SPS", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BIT-103", "Programming in C", "Tutorial", "10:00", "10:50", "T2", null, "TL-203"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-203"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "L TH", "Lecture", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "BIT-104", "Internet and Web Designing", "Lecture", "16:15", "17:00", null, null, "TL-203")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L TH", "Lecture", "09:10", "10:00", null, null, "TL-203"),
            TimetableCell("II", "BIT-104", "Internet and Web Designing", "Lecture", "10:00", "10:50", null, null, "TL-203"),
            TimetableCell("III", "—", "TL-203", "Lecture", "10:50", "11:40", null, null, "TL-203"),
            TimetableCell("IV", "BIT-103", "Programming in C", "Practical", "11:40", "12:30", null, "P1", "TL-203"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-203"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-203"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-203"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-203")
        )

        return schedule
    }

    fun getChemicalSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-110"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "—", "L", "Lecture", "10:00", "10:50", null, null, "TL-110"),
            TimetableCell("III", "BSM-131", "Engineering Physics", "Lecture", "10:50", "11:40", null, null, "TL-110"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-110"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-110"),
            TimetableCell("VIII", "BSM-131", "Engineering Physics", "Practical", "16:15", "17:00", null, "P2", "TL-110")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "—", "L", "Lecture", "10:00", "10:50", null, null, "TL-110"),
            TimetableCell("III", "BSM-131", "Engineering Physics", "Lecture", "10:50", "11:40", null, null, "TL-110"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "BSM-131", "Engineering Physics", "Practical", "14:45", "15:30", null, "P1", "TL-110"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-110"),
            TimetableCell("VIII", "—", "T/1/BSM- 110/AG/TL-110", "Tutorial", "16:15", "17:00", "T1", null, "TL-110")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "BIT-103", "Programming in C", "Practical", "10:00", "10:50", null, "P2", "TL-110"),
            TimetableCell("III", "BME-104", "Manufacturing Practice Workshop", "Practical", "10:50", "11:40", null, "P1", "TL-110"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "—", "L AKG", "Lecture", "14:45", "15:30", null, null, "TL-110"),
            TimetableCell("VII", "BIT-103", "Programming in C", "Lecture", "15:30", "16:15", null, null, "TL-110"),
            TimetableCell("VIII", "BHS-101", "Universal Human Values", "Tutorial", "16:15", "17:00", "T1", null, "TL-110")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "—", "L", "Lecture", "10:00", "10:50", null, null, "TL-110"),
            TimetableCell("III", "BSM-131", "Engineering Physics", "Lecture", "10:50", "11:40", null, null, "TL-110"),
            TimetableCell("IV", "BHS-101", "Universal Human Values", "Lecture", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "BIT-103", "Programming in C", "Lecture", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-110"),
            TimetableCell("VII", "BME-104", "Manufacturing Practice Workshop", "Practical", "15:30", "16:15", null, "P2", "TL-110"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-110")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "—", "L VB", "Lecture", "10:00", "10:50", null, null, "TL-110"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-110"),
            TimetableCell("IV", "BHS-101", "Universal Human Values", "Lecture", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "BIT-103", "Programming in C", "Lecture", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "BME-101", "Manufacturing Techniques Workshop", "Lecture", "14:45", "15:30", null, null, "TL-110"),
            TimetableCell("VII", "—", "T/2//BSM- 110/AG/TL-110", "Tutorial", "15:30", "16:15", "T2", null, "TL-110"),
            TimetableCell("VIII", "BHS-101", "Universal Human Values", "Tutorial", "16:15", "17:00", "T2", null, "TL-110")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-110"),
            TimetableCell("II", "BIT-103", "Programming in C", "Practical", "10:00", "10:50", null, "P1", "TL-110"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-110"),
            TimetableCell("IV", "BME-101", "Manufacturing Techniques Workshop", "Lecture", "11:40", "12:30", null, null, "TL-110"),
            TimetableCell("V", "BHS-101", "Universal Human Values", "Lecture", "14:00", "14:45", null, null, "TL-110"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-110"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-110"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-110")
        )

        return schedule
    }

    fun getMeSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-205"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "L SL", "Lecture", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "14:45", "15:30", null, "P1", "TL-205"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "BME-104", "Manufacturing Practice Workshop", "Practical", "10:50", "11:40", null, "P2", "TL-205"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "14:45", "15:30", null, "P1", "TL-205"),
            TimetableCell("VII", "—", "L RKY", "Lecture", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "BSM-140", "Environmental Science & Green Chemistry", "Tutorial", "16:15", "17:00", "T2", null, "TL-205")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L RKY", "Lecture", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "BME-104", "Manufacturing Practice Workshop", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BEE-110", "Basic Electrical Engineering", "Practical", "14:45", "15:30", null, "P1", "TL-205"),
            TimetableCell("VII", "—", "L", "Lecture", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "BSM-110", "Engineering Mathematics I", "Tutorial", "16:15", "17:00", "T2", null, "TL-205")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L SL L", "Lecture", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "L MG", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T1", null, "TL-205"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "SL L", "Lecture", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "—", "TL-205", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Practical", "14:45", "15:30", null, "P2", "TL-205"),
            TimetableCell("VII", "BME-104", "Manufacturing Practice Workshop", "Practical", "15:30", "16:15", null, "P1", "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "10:00", "10:50", "T1", null, "TL-205"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Practical", "11:40", "12:30", null, "P2", "TL-205"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-205"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        return schedule
    }

    fun getMeSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-205"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Practical", "10:00", "10:50", null, "P2", "TL-205"),
            TimetableCell("III", "BME-104", "Manufacturing Practice Workshop", "Practical", "10:50", "11:40", null, "P1", "TL-205"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "14:45", "15:30", null, null, "TL-205"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T1", null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "SL", "Lecture", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "—", "TL-205 SY", "Lecture", "10:00", "10:50", null, null, "TL-205"),
            TimetableCell("III", "—", "TL-205 RKY L RKY", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "TL-205 H C N U L TL-205", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "—", "TL-205", "Lecture", "14:45", "15:30", null, null, "TL-205"),
            TimetableCell("VII", "BME-104", "Manufacturing Practice Workshop", "Practical", "15:30", "16:15", null, "P2", "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BEE-110", "Basic Electrical Engineering", "Practical", "10:00", "10:50", null, "P1", "TL-205"),
            TimetableCell("III", "—", "L RKY L", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "TL-205", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Practical", "14:45", "15:30", null, "P2", "TL-205"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T2", null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Practical", "10:00", "10:50", null, "P1", "TL-205"),
            TimetableCell("III", "—", "SL L", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-205"),
            TimetableCell("V", "—", "TL-205", "Lecture", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "14:45", "15:30", null, "P1", "TL-205"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "15:30", "16:15", "T1", null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-205"),
            TimetableCell("II", "BEE-110", "Basic Electrical Engineering", "Practical", "10:00", "10:50", null, "P2", "TL-205"),
            TimetableCell("III", "—", "SL", "Lecture", "10:50", "11:40", null, null, "TL-205"),
            TimetableCell("IV", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "11:40", "12:30", "T2", null, "TL-205"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-205"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-205"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-205"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-205")
        )

        return schedule
    }

    fun getEeSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-202"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "L PS", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "BEE-108A", "Electrical Wiring & Estimation", "Practical", "14:45", "15:30", null, "P1", "TL-202"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "16:15", "17:00", "T2", null, "TL-202")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "—", "L AK", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "L", "Lecture", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "16:15", "17:00", null, null, "TL-202")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Practical", "10:00", "10:50", null, "P1", "TL-202"),
            TimetableCell("III", "—", "L AK", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T2", null, "TL-202"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Practical", "16:15", "17:00", "T1", "P2", "TL-202")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L PS", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "11:40", "12:30", null, "P1", "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "L AK", "Lecture", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "BEE-110", "Basic Electrical Engineering", "Lecture", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-202")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L PS", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "11:40", "12:30", null, "P1", "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "L KBS", "Lecture", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "16:15", "17:00", null, null, "TL-202")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L KBS", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Tutorial", "10:50", "11:40", "T1", null, "TL-202"),
            TimetableCell("IV", "BEE-108A", "Electrical Wiring & Estimation", "Practical", "11:40", "12:30", null, "P2", "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-202")
        )

        return schedule
    }

    fun getEeSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-202"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "—", "L KBS", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "BEE-110", "Basic Electrical Engineering", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T2", null, "TL-202"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "15:30", "16:15", "T2", null, "TL-202"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-202")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L SJ", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Practical", "11:40", "12:30", null, "P1", "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "L KBS", "Lecture", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Practical", "16:15", "17:00", null, "P1", "TL-202")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L SJ", "Lecture", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "16:15", "17:00", null, "P1", "TL-202")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BEE-108A", "Electrical Wiring & Estimation", "Practical", "10:00", "10:50", null, "P1", "TL-202"),
            TimetableCell("III", "—", "L RB", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "16:15", "17:00", "T1", "P2", "TL-202")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-202"),
            TimetableCell("III", "—", "L SJ", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-202"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Lecture", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "BEE-108A", "Electrical Wiring & Estimation", "Lecture", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "BEE-110", "Basic Electrical Engineering", "Lecture", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Practical", "16:15", "17:00", null, "P2", "TL-202")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-202"),
            TimetableCell("II", "BEE-108A", "Electrical Wiring & Estimation", "Practical", "10:00", "10:50", null, "P2", "TL-202"),
            TimetableCell("III", "—", "L", "Lecture", "10:50", "11:40", null, null, "TL-202"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Tutorial", "11:40", "12:30", "T1", null, "TL-202"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-202"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-202"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-202"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-202")
        )

        return schedule
    }

    fun getEceSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-207"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "L HC", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "BEE-110", "Basic Electrical Engineering", "Practical", "16:15", "17:00", null, "P1", "TL-207")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "10:50", "11:40", null, "P2", "TL-207"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "11:40", "12:30", null, "P1", "TL-207"),
            TimetableCell("V", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "BEE-110", "Basic Electrical Engineering", "Practical", "16:15", "17:00", null, "P2", "TL-207")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Practical", "10:00", "10:50", null, "P1", "TL-207"),
            TimetableCell("III", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "15:30", "16:15", null, "P1", "TL-207"),
            TimetableCell("VIII", "BHS-102", "Technical Writing & Professional Communication", "Practical", "16:15", "17:00", null, "P2", "TL-207")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L HC", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:00", "14:45", "T1", null, "TL-207"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L HC", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:00", "14:45", "T2", null, "TL-207"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "14:45", "15:30", "T2", null, "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L PL", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BEC-106", "Electronic Components Testing & Measurement", "Tutorial", "10:00", "10:50", "T1", null, "TL-207"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "11:40", "12:30", null, "P2", "TL-207"),
            TimetableCell("V", "—", "0", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "—", "Dr. Harish Chandra (HC) and Prgya Mishra (PM)", "Lecture", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        return schedule
    }

    fun getEceSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-207"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T1", null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L AS", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Practical", "14:45", "15:30", null, "P1", "TL-207"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L AS", "Lecture", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BHS-102", "Technical Writing & Professional Communication", "Practical", "14:45", "15:30", null, "P1", "TL-207"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "10:00", "10:50", null, "P1", "TL-207"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Practical", "10:50", "11:40", "T2", "P2", "TL-207"),
            TimetableCell("IV", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "11:40", "12:30", "T2", null, "TL-207"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BEE-110", "Basic Electrical Engineering", "Practical", "14:45", "15:30", null, "P1", "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-207"),
            TimetableCell("III", "BSM-110", "Engineering Mathematics I", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-207"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "BEE-110", "Basic Electrical Engineering", "Practical", "14:45", "15:30", null, "P2", "TL-207"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "15:30", "16:15", null, "P1", "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-207"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "10:00", "10:50", null, "P2", "TL-207"),
            TimetableCell("III", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:50", "11:40", null, null, "TL-207"),
            TimetableCell("IV", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "11:40", "12:30", "T1", null, "TL-207"),
            TimetableCell("V", "—", "0", "Lecture", "14:00", "14:45", null, null, "TL-207"),
            TimetableCell("VI", "—", "Dr. Harish Chndra (HC) & Km. Kshama Yadav", "Lecture", "14:45", "15:30", null, null, "TL-207"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-207"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-207")
        )

        return schedule
    }

    fun getEceSecCTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-204"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BEE-110", "Basic Electrical Engineering", "Practical", "10:00", "10:50", null, "P2", "TL-204"),
            TimetableCell("III", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "10:50", "11:40", null, "P1", "TL-204"),
            TimetableCell("IV", "BHS-102", "Technical Writing & Professional Communication", "Practical", "11:40", "12:30", null, "P2", "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "15:30", "16:15", null, "P2", "TL-204"),
            TimetableCell("VIII", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "16:15", "17:00", null, "P1", "TL-204")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L PKK", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BEE-110", "Basic Electrical Engineering", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "11:40", "12:30", null, "P2", "TL-204"),
            TimetableCell("V", "—", "H C N U L", "Lecture", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T2", null, "TL-204"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "15:30", "16:15", "T2", null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L MP", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T1", null, "TL-204"),
            TimetableCell("VII", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "15:30", "16:15", "T1", null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L MP", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BEE-110", "Basic Electrical Engineering", "Lecture", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "BEE-110", "Basic Electrical Engineering", "Practical", "16:15", "17:00", null, "P1", "TL-204")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "L MP", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-110", "Engineering Mathematics I", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BHS-102", "Technical Writing & Professional Communication", "Practical", "11:40", "12:30", null, "P1", "TL-204"),
            TimetableCell("V", "—", "0", "Lecture", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "—", "Dr. Manmohan Pandey (MP) & Km. Shrishti Tripathi (ST)", "Lecture", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        return schedule
    }

    fun getEceIotSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-204"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "BSM-110", "Engineering Mathematics I", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BEE-110", "Basic Electrical Engineering", "Practical", "14:45", "15:30", null, "P1", "TL-204"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "15:30", "16:15", null, "P2", "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "BSM-110", "Engineering Mathematics I", "Lecture", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Lecture", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "—", "Self Study / Library", "Free", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "L", "Lecture", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BSM-110", "Engineering Mathematics I", "Tutorial", "14:45", "15:30", "T1", null, "TL-204"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "—", "Self Study / Library", "Free", "10:00", "10:50", null, null, "TL-204"),
            TimetableCell("III", "BHS-102", "Technical Writing & Professional Communication", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "14:00", "14:45", "T1", null, "TL-204"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Practical", "10:00", "10:50", null, "P2", "TL-204"),
            TimetableCell("III", "—", "L KV TL-204", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BSM-110", "Engineering Mathematics I", "Lecture", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "14:45", "15:30", null, "P2", "TL-204"),
            TimetableCell("VII", "BEC-106", "Electronic Components Testing & Measurement", "Practical", "15:30", "16:15", null, "P1", "TL-204"),
            TimetableCell("VIII", "BEE-110", "Basic Electrical Engineering", "Practical", "16:15", "17:00", null, "P2", "TL-204")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BSM-140", "Environmental Science & Green Chemistry", "Practical", "10:00", "10:50", null, "P1", "TL-204"),
            TimetableCell("III", "BEC-106", "Electronic Components Testing & Measurement", "Lecture", "10:50", "11:40", null, null, "TL-204"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "BSM-110", "Engineering Mathematics I", "Tutorial", "15:30", "16:15", "T2", null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-204"),
            TimetableCell("II", "BHS-102", "Technical Writing & Professional Communication", "Practical", "10:00", "10:50", null, "P1", "TL-204"),
            TimetableCell("III", "BHS-102", "Technical Writing & Professional Communication", "Tutorial", "10:50", "11:40", "T2", null, "TL-204"),
            TimetableCell("IV", "BEE-110", "Basic Electrical Engineering", "Lecture", "11:40", "12:30", null, null, "TL-204"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-204"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-204"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-204"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-204")
        )

        return schedule
    }

    fun getBbaSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-113"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-113"),
            TimetableCell("II", "—", "UG TL-113", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "BBA-116", "Quantitative Techniques for Business Research", "Tutorial", "10:50", "11:40", "T1", null, "TL-113"),
            TimetableCell("IV", "BBA-114", "Financial Accounting", "Tutorial", "11:40", "12:30", "T1", null, "TL-113"),
            TimetableCell("V", "BBA-115", "Principles and Practices of Management", "Lecture", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-113"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "BHM-121", "Intellectual Property Rights", "Lecture", "09:10", "10:00", null, null, "TL-113"),
            TimetableCell("II", "—", "TL-113", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "BBA-114", "Financial Accounting", "Tutorial", "10:50", "11:40", "T2", null, "TL-113"),
            TimetableCell("IV", "—", "JA", "Lecture", "11:40", "12:30", null, null, "TL-113"),
            TimetableCell("V", "BBA-A01", "Industrial Psychology", "Lecture", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-113"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "BHM-121", "Intellectual Property Rights", "Lecture", "09:10", "10:00", null, null, "TL-113"),
            TimetableCell("II", "BBA-114", "Financial Accounting", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "BBA-116", "Quantitative Techniques for Business Research", "Tutorial", "10:50", "11:40", "T2", null, "TL-113"),
            TimetableCell("IV", "BBA-115", "Principles and Practices of Management", "Tutorial", "11:40", "12:30", "T1", null, "TL-113"),
            TimetableCell("V", "BBA-115", "Principles and Practices of Management", "Lecture", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "BHM-121", "Intellectual Property Rights", "Tutorial", "14:45", "15:30", "T2", null, "TL-113"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-113"),
            TimetableCell("II", "AUC-108", "Business Communication for Managers", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "BBA-A01", "Industrial Psychology", "Tutorial", "10:50", "11:40", "T2", null, "TL-113"),
            TimetableCell("IV", "—", "JA L", "Lecture", "11:40", "12:30", null, null, "TL-113"),
            TimetableCell("V", "BBA-115", "Principles and Practices of Management", "Lecture", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "—", "PO", "Lecture", "14:45", "15:30", null, null, "TL-113"),
            TimetableCell("VII", "BBA-116", "Quantitative Techniques for Business Research", "Lecture", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-113"),
            TimetableCell("II", "BBA-A01", "Industrial Psychology", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "BBA-A01", "Industrial Psychology", "Tutorial", "10:50", "11:40", "T1", null, "TL-113"),
            TimetableCell("IV", "BBA-114", "Financial Accounting", "Lecture", "11:40", "12:30", null, null, "TL-113"),
            TimetableCell("V", "—", "PO TL-113 TL-113", "Lecture", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-113"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "BBA-114", "Financial Accounting", "Tutorial", "09:10", "10:00", "T2", null, "TL-113"),
            TimetableCell("II", "—", "AJ TL-113", "Lecture", "10:00", "10:50", null, null, "TL-113"),
            TimetableCell("III", "—", "Financial Accounting Principles and Practices of Management", "Lecture", "10:50", "11:40", null, null, "TL-113"),
            TimetableCell("IV", "—", "UG TL-113", "Lecture", "11:40", "12:30", null, null, "TL-113"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "TL-113"),
            TimetableCell("VI", "—", "Dr. Ugrasen (UG) / Ms. Swati Gupta (SG) Dr. Javed Alam (JA) /  Ms. Akriti Singh (AKS)", "Lecture", "14:45", "15:30", null, null, "TL-113"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-113"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-113")
        )

        return schedule
    }

    fun getBbaSecBTimetable(): Map<String, List<TimetableCell>> {
        val room = "TL-114"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-114"),
            TimetableCell("II", "BBA-116", "Quantitative Techniques for Business Research", "Lecture", "10:00", "10:50", "T1", null, "TL-114"),
            TimetableCell("III", "BHM-121", "Intellectual Property Rights", "Lecture", "10:50", "11:40", null, null, "TL-114"),
            TimetableCell("IV", "BBA-115", "Principles and Practices of Management", "Tutorial", "11:40", "12:30", "T1", null, "TL-114"),
            TimetableCell("V", "BBA-114", "Financial Accounting", "Lecture", "14:00", "14:45", null, null, "TL-114"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "TL-114"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L", "Lecture", "09:10", "10:00", null, null, "TL-114"),
            TimetableCell("II", "BBA-116", "Quantitative Techniques for Business Research", "Lecture", "10:00", "10:50", null, null, "TL-114"),
            TimetableCell("III", "BHM-121", "Intellectual Property Rights", "Tutorial", "10:50", "11:40", "T1", null, "TL-114"),
            TimetableCell("IV", "BBA-114", "Financial Accounting", "Lecture", "11:40", "12:30", null, null, "TL-114"),
            TimetableCell("V", "BBA-114", "Financial Accounting", "Lecture", "14:00", "14:45", "T2", null, "TL-114"),
            TimetableCell("VI", "—", "TL-114 SS T1", "Lecture", "14:45", "15:30", null, null, "TL-114"),
            TimetableCell("VII", "BBA-A01", "Industrial Psychology", "Lecture", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "BHM-121", "Intellectual Property Rights", "Lecture", "09:10", "10:00", null, null, "TL-114"),
            TimetableCell("II", "AUC-108", "Business Communication for Managers", "Lecture", "10:00", "10:50", null, null, "TL-114"),
            TimetableCell("III", "—", "SG", "Lecture", "10:50", "11:40", null, null, "TL-114"),
            TimetableCell("IV", "—", "TL-114 AS", "Lecture", "11:40", "12:30", null, null, "TL-114"),
            TimetableCell("V", "BHM-121", "Intellectual Property Rights", "Lecture", "14:00", "14:45", null, null, "TL-114"),
            TimetableCell("VI", "—", "TL-114 KUS", "Lecture", "14:45", "15:30", null, null, "TL-114"),
            TimetableCell("VII", "BBA-A01", "Industrial Psychology", "Lecture", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "L IK", "Lecture", "09:10", "10:00", null, null, "TL-114"),
            TimetableCell("II", "BBA-115", "Principles and Practices of Management", "Lecture", "10:00", "10:50", "T2", null, "TL-114"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-114"),
            TimetableCell("IV", "—", "L VS", "Lecture", "11:40", "12:30", null, null, "TL-114"),
            TimetableCell("V", "BBA-116", "Quantitative Techniques for Business Research", "Lecture", "14:00", "14:45", "T2", null, "TL-114"),
            TimetableCell("VI", "BBA-115", "Principles and Practices of Management", "Tutorial", "14:45", "15:30", "T2", null, "TL-114"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "TL-114"),
            TimetableCell("II", "AUC-108", "Business Communication for Managers", "Lecture", "10:00", "10:50", null, null, "TL-114"),
            TimetableCell("III", "—", "Self Study / Library", "Free", "10:50", "11:40", null, null, "TL-114"),
            TimetableCell("IV", "—", "L IK", "Lecture", "11:40", "12:30", null, null, "TL-114"),
            TimetableCell("V", "BBA-115", "Principles and Practices of Management", "Lecture", "14:00", "14:45", null, null, "TL-114"),
            TimetableCell("VI", "—", "TL-114 SS", "Lecture", "14:45", "15:30", null, null, "TL-114"),
            TimetableCell("VII", "BBA-A01", "Industrial Psychology", "Lecture", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "BBA-114", "Financial Accounting", "Lecture", "09:10", "10:00", "T2", null, "TL-114"),
            TimetableCell("II", "—", "TL-114", "Lecture", "10:00", "10:50", null, null, "TL-114"),
            TimetableCell("III", "—", "L AS Financial Accounting Principles and Practices of Management", "Lecture", "10:50", "11:40", null, null, "TL-114"),
            TimetableCell("IV", "BBA-114", "Financial Accounting", "Lecture", "11:40", "12:30", null, null, "TL-114"),
            TimetableCell("V", "—", "TL-114", "Lecture", "14:00", "14:45", null, null, "TL-114"),
            TimetableCell("VI", "—", "Dr. Anjali Singh (AS) / Ms. Swati Gupta (SG) Dr. Indal Kumar (IK)/ Ms. Akanksha Jaiswal (AKJ)", "Lecture", "14:45", "15:30", null, null, "TL-114"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "TL-114"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "TL-114")
        )

        return schedule
    }

    fun getBpharmSecATimetable(): Map<String, List<TimetableCell>> {
        val room = "L-115"
        val schedule = mutableMapOf<String, List<TimetableCell>>()

        schedule["Monday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "BPT108P", "Healthcare Psychology Lab", "Practical", "10:00", "10:50", null, "P1", "L-115"),
            TimetableCell("III", "LIBRARY", "Library & Digital Resources", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT108P", "Healthcare Psychology Lab", "Practical", "11:40", "12:30", null, "P2", "L-115"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "L-115"),
            TimetableCell("VII", "BPT107P", "General Pharmacy Lab", "Practical", "15:30", "16:15", null, "P1", "L-115"),
            TimetableCell("VIII", "LIBRARY", "Library & Digital Resources", "Lecture", "16:15", "17:00", null, null, "L-115")
        )

        schedule["Tuesday"] = listOf(
            TimetableCell("I", "—", "L AV", "Lecture", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "BPT105T", "Introduction to Pharmacognosy", "Lecture", "10:00", "10:50", null, null, "L-115"),
            TimetableCell("III", "BPT104T", "Healthcare Psychology & Comm", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT106T", "Pharmaceutical Inorganic Chemistry", "Lecture", "11:40", "12:30", null, null, "L-115"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "BPT108P", "Healthcare Psychology Lab", "Practical", "14:45", "15:30", null, "P3", "L-115"),
            TimetableCell("VII", "LIBRARY", "Library & Digital Resources", "Lecture", "15:30", "16:15", null, null, "L-115"),
            TimetableCell("VIII", "BPT108P", "Healthcare Psychology Lab", "Practical", "16:15", "17:00", null, "P4", "L-115")
        )

        schedule["Wednesday"] = listOf(
            TimetableCell("I", "—", "L DSP", "Lecture", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "BPT104T", "Healthcare Psychology & Comm", "Lecture", "10:00", "10:50", null, null, "L-115"),
            TimetableCell("III", "BPT102T", "Basics of Python Programming", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT106T", "Pharmaceutical Inorganic Chemistry", "Lecture", "11:40", "12:30", null, null, "L-115"),
            TimetableCell("V", "BPT103T", "General Pharmacy", "Lecture", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "L-115"),
            TimetableCell("VII", "BPT107P", "General Pharmacy Lab", "Practical", "15:30", "16:15", null, "P1", "L-115"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "L-115")
        )

        schedule["Thursday"] = listOf(
            TimetableCell("I", "—", "Self Study / Library", "Free", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "—", "L SO", "Lecture", "10:00", "10:50", null, null, "L-115"),
            TimetableCell("III", "BPT101T", "Human Anatomy & Physiology I", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT104T", "Healthcare Psychology & Comm", "Lecture", "11:40", "12:30", null, null, "L-115"),
            TimetableCell("V", "BPT105T", "Introduction to Pharmacognosy", "Lecture", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "L-115"),
            TimetableCell("VII", "BPT107P", "General Pharmacy Lab", "Practical", "15:30", "16:15", null, "P1", "L-115"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "L-115")
        )

        schedule["Friday"] = listOf(
            TimetableCell("I", "—", "L AJ", "Lecture", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "BPT102T", "Basics of Python Programming", "Lecture", "10:00", "10:50", null, null, "L-115"),
            TimetableCell("III", "BPT106T", "Pharmaceutical Inorganic Chemistry", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT104T", "Healthcare Psychology & Comm", "Lecture", "11:40", "12:30", null, null, "L-115"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "—", "Self Study / Library", "Free", "14:45", "15:30", null, null, "L-115"),
            TimetableCell("VII", "BPT107P", "General Pharmacy Lab", "Practical", "15:30", "16:15", null, "P1", "L-115"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "L-115")
        )

        schedule["Saturday"] = listOf(
            TimetableCell("I", "BPT101T", "Human Anatomy & Physiology I", "Lecture", "09:10", "10:00", null, null, "L-115"),
            TimetableCell("II", "BPT101T", "Human Anatomy & Physiology I", "Lecture", "10:00", "10:50", null, null, "L-115"),
            TimetableCell("III", "BPT105T", "Introduction to Pharmacognosy", "Lecture", "10:50", "11:40", null, null, "L-115"),
            TimetableCell("IV", "BPT102T", "Basics of Python Programming", "Lecture", "11:40", "12:30", null, null, "L-115"),
            TimetableCell("V", "—", "Self Study / Library", "Free", "14:00", "14:45", null, null, "L-115"),
            TimetableCell("VI", "—", "Dr. Smriti Ojha", "Lecture", "14:45", "15:30", null, null, "L-115"),
            TimetableCell("VII", "—", "Self Study / Library", "Free", "15:30", "16:15", null, null, "L-115"),
            TimetableCell("VIII", "—", "Self Study / Library", "Free", "16:15", "17:00", null, null, "L-115")
        )

        return schedule
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

    fun filterCellsForStudent(
        cells: List<TimetableCell>,
        tutorialGroup: String,
        practicalGroup: String
    ): List<TimetableCell> {
        val tGroup = tutorialGroup.trim().uppercase()
        val pGroup = practicalGroup.trim().uppercase()

        // Sync logic: T1 <-> P1, T2 <-> P2
        val effectiveTut = if (tGroup == "N/A" || tGroup.isBlank()) {
            if (pGroup == "P1") "T1" else if (pGroup == "P2") "T2" else ""
        } else tGroup

        val effectivePrac = if (pGroup == "N/A" || pGroup.isBlank()) {
            if (tGroup == "T1") "P1" else if (tGroup == "T2") "P2" else ""
        } else pGroup

        val result = mutableListOf<TimetableCell>()
        val groupedByPeriod = cells.groupBy { it.periodKey }

        groupedByPeriod.forEach { (_, periodCells) ->
            val matching = periodCells.filter { cell ->
                val tMatch = cell.tutorialGroup == null || effectiveTut.isBlank() || cell.tutorialGroup.equals(effectiveTut, ignoreCase = true)
                val pMatch = cell.practicalGroup == null || effectivePrac.isBlank() || cell.practicalGroup.equals(effectivePrac, ignoreCase = true)
                tMatch && pMatch
            }
            if (matching.isNotEmpty()) {
                result.addAll(matching)
            } else {
                periodCells.firstOrNull()?.let { result.add(it) }
            }
        }
        return result
    }

    fun dayCells(branch: Branch, section: String, day: String): List<TimetableCell> {
        val bId = branch.id.lowercase()
        val sec = section.uppercase().ifBlank { "A" }

        val scheduleMap: Map<String, List<TimetableCell>>? = when {
            bId == "civil" && sec == "A" -> getCivilSecATimetable()
            bId == "civil" && sec == "B" -> getCivilSecBTimetable()
            bId == "cse" && sec == "A" -> getCseSecATimetable()
            bId == "cse" && sec == "B" -> getCseSecBTimetable()
            bId == "cse" && sec == "C" -> getCseSecCTimetable()
            bId == "cse" && sec == "D" -> getCseSecDTimetable()
            bId == "it" && sec == "A" -> getItSecATimetable()
            bId == "it" && sec == "B" -> getItSecBTimetable()
            bId == "chemical" -> getChemicalSecATimetable()
            bId == "me" && sec == "A" -> getMeSecATimetable()
            bId == "me" && sec == "B" -> getMeSecBTimetable()
            bId == "ee" && sec == "A" -> getEeSecATimetable()
            bId == "ee" && sec == "B" -> getEeSecBTimetable()
            bId == "ece" && sec == "A" -> getEceSecATimetable()
            bId == "ece" && sec == "B" -> getEceSecBTimetable()
            bId == "ece" && sec == "C" -> getEceSecCTimetable()
            bId == "eceiot" -> getEceIotSecATimetable()
            bId == "bba" && sec == "A" -> getBbaSecATimetable()
            bId == "bba" && sec == "B" -> getBbaSecBTimetable()
            bId == "bpharm" -> getBpharmSecATimetable()
            else -> null
        }

        if (scheduleMap != null) {
            return scheduleMap[day] ?: emptyList()
        }

        val grid = buildWeekGrid(branch, section)
        val dayMap = grid[day] ?: return emptyList()
        return AcademicData.TEACH_PERIODS.map { p ->
            val u = dayMap[p.key]!!
            TimetableCell(p.key, u.code, u.name, u.type, p.start, p.end, u.tutorialGroup, u.practicalGroup)
        }
    }

    fun tokenDocId(token: String): String {
        var hash = 0
        for (c in token) hash = ((hash shl 5) - hash + c.code)
        return "android_" + abs(hash).toString(36)
    }
}
