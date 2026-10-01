package com.mmmut.ero.data.local

data class Period(val key: String, val start: String, val end: String, val label: String)
data class Branch(val id: String, val name: String, val sections: List<String>, val room: String, val subjects: List<SubjectDef>)
data class SubjectDef(val code: String, val name: String, val l: Int, val t: Int, val p: Int)
data class BuiltinEvent(val start: String, val end: String, val title: String)

object AcademicData {
    val PERIODS = listOf(
        Period("I", "09:10", "10:00", "I"), Period("II", "10:00", "10:50", "II"),
        Period("III", "10:50", "11:40", "III"), Period("IV", "11:40", "12:30", "IV"),
        Period("LUNCH", "12:30", "14:10", "Lunch"),
        Period("V", "14:10", "15:00", "V"), Period("VI", "15:00", "15:50", "VI"),
        Period("VII", "15:50", "16:40", "VII"), Period("VIII", "16:40", "17:30", "VIII")
    )
    val TEACH_PERIODS = PERIODS.filter { it.key != "LUNCH" }
    val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val ROSTER_BRANCH_TO_ID = mapOf(
        "CED" to "civil", "CSD" to "cse", "EED" to "ee", "ECD" to "ece",
        "IOT" to "eceiot", "MED" to "me", "CHD" to "chemical", "ITC" to "it"
    )
    val ROLL_NUMBER_PATTERN = Regex("^\\d{10}$")
    fun rosterBranchToId(b: String): String = ROSTER_BRANCH_TO_ID[b.trim().uppercase()] ?: "civil"
    fun getBranch(id: String): Branch = BRANCHES.firstOrNull { it.id == id } ?: BRANCHES[1]
    val BRANCHES = listOf(
        Branch("civil", "B.Tech — Civil Engineering", listOf("A", "B"), "TL-206", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-131", "Engineering Physics", 3, 0, 2),
            SubjectDef("BIT-103", "Programming in C", 3, 0, 2),
            SubjectDef("BCE-121", "Engineering Graphics", 2, 0, 4),
            SubjectDef("BHS-101", "Universal Human Values", 3, 1, 0))),
        Branch("cse", "B.Tech — Computer Sc. & Engineering", listOf("A", "B", "C", "D"), "TL-109 / TL-201", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-131", "Engineering Physics", 3, 0, 2),
            SubjectDef("BCS-110", "Introduction to C Programming", 3, 0, 2),
            SubjectDef("BCS-111", "Web Designing-1", 2, 0, 4),
            SubjectDef("BHS-101", "Universal Human Values", 3, 1, 0))),
        Branch("it", "B.Tech — Information Technology", listOf("A", "B"), "TL-203", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-131", "Engineering Physics", 3, 0, 2),
            SubjectDef("BIT-103", "Programming in C", 3, 0, 2),
            SubjectDef("BIT-104", "Internet and Web Designing", 2, 0, 4),
            SubjectDef("BHS-101", "Universal Human Values", 3, 1, 0)))
    )
}
