package com.mmmut.ero.data.local

object AcademicDataExtra {
    val MORE_BRANCHES = listOf(
        Branch("chemical", "B.Tech — Chemical Engineering", listOf("A"), "TL-110", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-131", "Engineering Physics", 3, 0, 2),
            SubjectDef("BIT-103", "Programming in C", 3, 0, 2),
            SubjectDef("BME-104", "Manufacturing Techniques Workshop", 2, 0, 4),
            SubjectDef("BHS-101", "Universal Human Values", 3, 1, 0))),
        Branch("ee", "B.Tech — Electrical Engineering", listOf("A", "B"), "TL-202", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-140", "Environmental Science & Green Chemistry", 3, 0, 2),
            SubjectDef("BEE-110", "Basic Electrical Engineering", 3, 0, 2),
            SubjectDef("BEE-108A", "Electrical Wiring & Estimation", 3, 0, 2),
            SubjectDef("BHS-102", "Technical Writing & Professional Communication", 2, 1, 2))),
        Branch("me", "B.Tech — Mechanical Engineering", listOf("A", "B"), "TL-205", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-140", "Environmental Science & Green Chemistry", 3, 0, 2),
            SubjectDef("BEE-110", "Basic Electrical Engineering", 3, 0, 2),
            SubjectDef("BME-104", "Manufacturing Practice Workshop", 2, 0, 4),
            SubjectDef("BHS-102", "Technical Writing & Professional Communication", 2, 1, 2))),
        Branch("ece", "B.Tech — Electronics & Comm. Engineering", listOf("A", "B", "C"), "TL-207 / TL-204", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-140", "Environmental Science & Green Chemistry", 3, 0, 2),
            SubjectDef("BEE-110", "Basic Electrical Engineering", 3, 0, 2),
            SubjectDef("BEC-106", "Electronic Components Testing & Measurement", 2, 0, 4),
            SubjectDef("BHS-102", "Technical Writing & Professional Communication", 2, 1, 2))),
        Branch("eceiot", "B.Tech - ECE (IOT)", listOf("A"), "TL-204", listOf(
            SubjectDef("BSM-110", "Engineering Mathematics I", 3, 1, 0),
            SubjectDef("BSM-140", "Environmental Science & Green Chemistry", 3, 0, 2),
            SubjectDef("BEE-110", "Basic Electrical Engineering", 3, 0, 2),
            SubjectDef("BEC-106", "Electronic Components Testing & Measurement", 2, 0, 4),
            SubjectDef("BHS-102", "Technical Writing & Professional Communication", 2, 1, 2))),
        Branch("bba", "Management Studies — BBA", listOf("A", "B"), "TL-113 / TL-114", listOf(
            SubjectDef("BBA-114", "Financial Accounting", 3, 1, 0),
            SubjectDef("BBA-115", "Principles & Practices of Management", 3, 1, 0),
            SubjectDef("BBA-116", "Quantitative Techniques for Business Research", 3, 1, 0),
            SubjectDef("BBA-A01", "Industrial Psychology", 2, 1, 0),
            SubjectDef("BHM-121", "Intellectual Property Rights", 2, 1, 0),
            SubjectDef("AUC-108", "Business Communication for Managers", 2, 0, 0))),
        Branch("bpharm", "Department of Pharmacy — B.Pharm", listOf("A"), "L-115 / CH-206", listOf(
            SubjectDef("BPT101T", "Human Anatomy & Physiology I", 3, 1, 0),
            SubjectDef("BPT102T", "Basics of Python Programming", 3, 0, 2),
            SubjectDef("BPT103T", "General Pharmacy", 3, 0, 2),
            SubjectDef("BPT104T", "Healthcare Psychology & Comm Skills", 2, 1, 0),
            SubjectDef("BPT105T", "Introduction to Pharmacognosy", 3, 0, 2),
            SubjectDef("BPT106T", "Pharmaceutical Inorganic Chemistry", 3, 0, 2)))
    )
    val ALL_BRANCHES: List<Branch> get() = AcademicData.BRANCHES + MORE_BRANCHES
    fun getBranch(id: String): Branch = ALL_BRANCHES.firstOrNull { it.id == id } ?: AcademicData.BRANCHES[1]
    val BUILTIN_EVENTS = listOf(
        BuiltinEvent("2026-07-29", "2026-07-30", "Physical Reporting at MMMUT Gorakhpur"),
        BuiltinEvent("2026-07-31", "2026-07-31", "Orientation Program"),
        BuiltinEvent("2026-08-01", "2026-08-22", "Induction Program (IPNS-2026)"),
        BuiltinEvent("2026-08-03", "2026-08-03", "Commencement of classes (partially)"),
        BuiltinEvent("2026-09-18", "2026-09-18", "Display of Mid Semester Attendance by HoD"),
        BuiltinEvent("2026-09-21", "2026-09-26", "Mid Semester Examinations"),
        BuiltinEvent("2026-11-23", "2026-11-28", "End Semester Examinations (Practical)"),
        BuiltinEvent("2026-12-01", "2026-12-12", "End Semester Examinations (Theory)")
    )
}
