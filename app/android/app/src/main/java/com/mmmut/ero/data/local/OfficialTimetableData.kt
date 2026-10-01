package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.TimetableEntry

object OfficialTimetableData {

    // Official Timetable Entries extracted directly from MMMUT Official Timetable PDF (Session 2025-26)
    val ALL_ENTRIES: List<TimetableEntry> = listOf(
        // =========================================================================
        // PAGE 1: B.Tech (Civil Engineering), Semester I, SEC-A, Room: TL-206
        // =========================================================================
        // Monday
        TimetableEntry("civil_1_a_mon_1", "2025-26", "civil", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_a_mon_2", "2025-26", "civil", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_a_mon_3", "2025-26", "civil", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        TimetableEntry("civil_1_a_mon_7", "2025-26", "civil", "A", 1, "Monday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_a_mon_8_t1", "2025-26", "civil", "A", 1, "Monday", "VIII", "VIII", "16:15", "17:00", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-206", "UF"),

        // Tuesday
        TimetableEntry("civil_1_a_tue_1", "2025-26", "civil", "A", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_a_tue_2", "2025-26", "civil", "A", 1, "Tuesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_a_tue_3_t1", "2025-26", "civil", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-206"),
        TimetableEntry("civil_1_a_tue_7_t2", "2025-26", "civil", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-206", "UF"),

        // Wednesday
        TimetableEntry("civil_1_a_wed_1", "2025-26", "civil", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_a_wed_2", "2025-26", "civil", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_a_wed_3_t2", "2025-26", "civil", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-206"),
        // Wednesday afternoon simultaneous practicals: P1 in L-104, P2 in Physics Lab
        TimetableEntry("civil_1_a_wed_5_6_p1", "2025-26", "civil", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BCE-121", "Engineering Graphics", "PRACTICAL", null, "P1", "L-104", "RPT/SU/AnS/CC"),
        TimetableEntry("civil_1_a_wed_5_6_p2", "2025-26", "civil", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab"),

        // Thursday
        TimetableEntry("civil_1_a_thu_1_2_p2", "2025-26", "civil", "A", 1, "Thursday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "ITRC-02", "AK"),
        TimetableEntry("civil_1_a_thu_3", "2025-26", "civil", "A", 1, "Thursday", "III", "III", "10:50", "11:40", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        // Thursday afternoon simultaneous practicals: P1 in Physics Lab, P2 in L-104
        TimetableEntry("civil_1_a_thu_5_6_p1", "2025-26", "civil", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab"),
        TimetableEntry("civil_1_a_thu_5_6_p2", "2025-26", "civil", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BCE-121", "Engineering Graphics", "PRACTICAL", null, "P2", "L-104", "MM/AD/AkS/An"),

        // Friday
        TimetableEntry("civil_1_a_fri_3", "2025-26", "civil", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        TimetableEntry("civil_1_a_fri_4", "2025-26", "civil", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_a_fri_5", "2025-26", "civil", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BCE-121", "Engineering Graphics", "LECTURE", null, null, "TL-206", "NC"),

        // Saturday
        TimetableEntry("civil_1_a_sat_1_2_p1", "2025-26", "civil", "A", 1, "Saturday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "ITRC-02", "AK"),
        TimetableEntry("civil_1_a_sat_3", "2025-26", "civil", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BCE-121", "Engineering Graphics", "LECTURE", null, null, "TL-206", "NC"),
        TimetableEntry("civil_1_a_sat_4", "2025-26", "civil", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),

        // =========================================================================
        // PAGE 2: B.Tech (Civil Engineering), Semester I, SEC-B, Room: TL-206
        // =========================================================================
        // Monday
        TimetableEntry("civil_1_b_mon_1_2_p1", "2025-26", "civil", "B", 1, "Monday", "I", "II", "09:10", "10:50", "BCE-121", "Engineering Graphics", "PRACTICAL", null, "P1", "L-104", "RP/AKS/PP"),
        TimetableEntry("civil_1_b_mon_3_4_p2", "2025-26", "civil", "B", 1, "Monday", "III", "IV", "10:50", "12:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab"),
        TimetableEntry("civil_1_b_mon_5", "2025-26", "civil", "B", 1, "Monday", "V", "V", "14:00", "14:45", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        TimetableEntry("civil_1_b_mon_6", "2025-26", "civil", "B", 1, "Monday", "VI", "VI", "14:45", "15:30", "BCE-121", "Engineering Graphics", "LECTURE", null, null, "TL-206", "NC"),

        // Tuesday
        TimetableEntry("civil_1_b_tue_1_2_p2", "2025-26", "civil", "B", 1, "Tuesday", "I", "II", "09:10", "10:50", "BCE-121", "Engineering Graphics", "PRACTICAL", null, "P2", "L-104", "DP/NC/An"),
        TimetableEntry("civil_1_b_tue_3_4_p1", "2025-26", "civil", "B", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab"),
        TimetableEntry("civil_1_b_tue_5", "2025-26", "civil", "B", 1, "Tuesday", "V", "V", "14:00", "14:45", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        TimetableEntry("civil_1_b_tue_6", "2025-26", "civil", "B", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),

        // Wednesday
        TimetableEntry("civil_1_b_wed_2_3_p2", "2025-26", "civil", "B", 1, "Wednesday", "II", "III", "10:00", "11:40", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "ITRC-01", "NiH"),
        TimetableEntry("civil_1_b_wed_4", "2025-26", "civil", "B", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-206", "NiH"),
        TimetableEntry("civil_1_b_wed_6", "2025-26", "civil", "B", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_b_wed_7", "2025-26", "civil", "B", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),

        // Thursday
        TimetableEntry("civil_1_b_thu_1", "2025-26", "civil", "B", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_b_thu_2", "2025-26", "civil", "B", 1, "Thursday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_b_thu_5", "2025-26", "civil", "B", 1, "Thursday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_b_thu_6_t1", "2025-26", "civil", "B", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-206"),
        TimetableEntry("civil_1_b_thu_7_t1", "2025-26", "civil", "B", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-206", "AKS"),

        // Friday
        TimetableEntry("civil_1_b_fri_1", "2025-26", "civil", "B", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206", "MH"),
        TimetableEntry("civil_1_b_fri_2", "2025-26", "civil", "B", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_b_fri_6", "2025-26", "civil", "B", 1, "Friday", "VI", "VI", "14:45", "15:30", "BCE-121", "Engineering Graphics", "LECTURE", null, null, "TL-206", "NC"),
        TimetableEntry("civil_1_b_fri_7_t2", "2025-26", "civil", "B", 1, "Friday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-206"),

        // Saturday
        TimetableEntry("civil_1_b_sat_1", "2025-26", "civil", "B", 1, "Saturday", "I", "I", "09:10", "10:00", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-206"),
        TimetableEntry("civil_1_b_sat_2_t2", "2025-26", "civil", "B", 1, "Saturday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-206", "AKS"),
        TimetableEntry("civil_1_b_sat_3_4_p1", "2025-26", "civil", "B", 1, "Saturday", "III", "IV", "10:50", "12:30", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "ITRC-01", "NiH"),

        // =========================================================================
        // PAGE 3: B.Tech (Computer Sc. & Engineering), Semester I, SEC-A, Room: TL-109
        // =========================================================================
        // Monday
        TimetableEntry("cse_1_a_mon_1_2_p2", "2025-26", "cse", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab"),
        TimetableEntry("cse_1_a_mon_3", "2025-26", "cse", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_mon_4", "2025-26", "cse", "A", 1, "Monday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_mon_5_6_p1", "2025-26", "cse", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-03", "Dr. Meenu, Anushka (RS)"),
        TimetableEntry("cse_1_a_mon_5_6_p2", "2025-26", "cse", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-04", "SKS, Vaibhavi(RS)"),

        // Tuesday
        TimetableEntry("cse_1_a_tue_1_2_p1", "2025-26", "cse", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab"),
        TimetableEntry("cse_1_a_tue_3", "2025-26", "cse", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_tue_4", "2025-26", "cse", "A", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"),
        TimetableEntry("cse_1_a_tue_5_6_p2", "2025-26", "cse", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "Dr. Meenu, Shreyansh (RS)"),

        // Wednesday
        TimetableEntry("cse_1_a_wed_3", "2025-26", "cse", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_wed_4", "2025-26", "cse", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"),

        // Thursday
        TimetableEntry("cse_1_a_thu_1", "2025-26", "cse", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_a_thu_2", "2025-26", "cse", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_a_thu_5_t1", "2025-26", "cse", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-109", "DM"),
        TimetableEntry("cse_1_a_thu_6", "2025-26", "cse", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_thu_7_t1", "2025-26", "cse", "A", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-109"),

        // Friday
        TimetableEntry("cse_1_a_fri_1", "2025-26", "cse", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_a_fri_2", "2025-26", "cse", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_a_fri_5", "2025-26", "cse", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_a_fri_6_t2", "2025-26", "cse", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-109", "DM"),
        TimetableEntry("cse_1_a_fri_7_t2", "2025-26", "cse", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-109"),

        // Saturday
        TimetableEntry("cse_1_a_sat_1", "2025-26", "cse", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_a_sat_2", "2025-26", "cse", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_a_sat_3_4_p1", "2025-26", "cse", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-04", "SKS, HARSH(RS)"),

        // =========================================================================
        // PAGE 4: B.Tech (Computer Sc. & Engineering), Semester I, SEC-B, Room: TL-109
        // =========================================================================
        // Monday
        TimetableEntry("cse_1_b_mon_1", "2025-26", "cse", "B", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_b_mon_2", "2025-26", "cse", "B", 1, "Monday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_b_mon_3_4_p1", "2025-26", "cse", "B", 1, "Monday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-04", "SKS, MKS"),
        TimetableEntry("cse_1_b_mon_5_t1", "2025-26", "cse", "B", 1, "Monday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-109", "SS"),
        TimetableEntry("cse_1_b_mon_6", "2025-26", "cse", "B", 1, "Monday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-109"),

        // Tuesday
        TimetableEntry("cse_1_b_tue_1", "2025-26", "cse", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_b_tue_2", "2025-26", "cse", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_b_tue_3_4_p2", "2025-26", "cse", "B", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-04", "SKS, MKS"),
        TimetableEntry("cse_1_b_tue_5", "2025-26", "cse", "B", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_b_tue_6_t2", "2025-26", "cse", "B", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-109", "SS"),
        TimetableEntry("cse_1_b_tue_7_t2", "2025-26", "cse", "B", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-109"),

        // Wednesday
        TimetableEntry("cse_1_b_wed_1", "2025-26", "cse", "B", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-109", "KK"),
        TimetableEntry("cse_1_b_wed_2", "2025-26", "cse", "B", 1, "Wednesday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"),
        TimetableEntry("cse_1_b_wed_6", "2025-26", "cse", "B", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109"),

        // Thursday
        TimetableEntry("cse_1_b_thu_3", "2025-26", "cse", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_b_thu_4", "2025-26", "cse", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"),
        TimetableEntry("cse_1_b_thu_5_6_p1", "2025-26", "cse", "B", 1, "Thursday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-03", "G6, VDB (RS)"),

        // Friday
        TimetableEntry("cse_1_b_fri_3", "2025-26", "cse", "B", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_b_fri_4", "2025-26", "cse", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"),
        TimetableEntry("cse_1_b_fri_5_6_p2", "2025-26", "cse", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "G1, VDB (RS)"),
        TimetableEntry("cse_1_b_fri_5_6_p1", "2025-26", "cse", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab"),

        // Saturday
        TimetableEntry("cse_1_b_sat_1_2_p2", "2025-26", "cse", "B", 1, "Saturday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab"),
        TimetableEntry("cse_1_b_sat_3", "2025-26", "cse", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109"),
        TimetableEntry("cse_1_b_sat_4_t1", "2025-26", "cse", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-109"),

        // =========================================================================
        // PAGE 7: B.Tech (Information Technology), Semester I, SEC-A, Room: TL-203
        // =========================================================================
        // Monday
        TimetableEntry("it_1_a_mon_1_2_p1", "2025-26", "it", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P1", "ITRC-03", "NS"),
        TimetableEntry("it_1_a_mon_1_2_p2", "2025-26", "it", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "CL-02", "DS"),
        TimetableEntry("it_1_a_mon_5", "2025-26", "it", "A", 1, "Monday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203"),
        TimetableEntry("it_1_a_mon_6", "2025-26", "it", "A", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203"),

        // Tuesday
        TimetableEntry("it_1_a_tue_1_2_p2", "2025-26", "it", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P2", "ITRC-03", "NS"),
        TimetableEntry("it_1_a_tue_1_2_p1", "2025-26", "it", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "CL-02", "DS"),
        TimetableEntry("it_1_a_tue_5", "2025-26", "it", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203"),

        // Wednesday
        TimetableEntry("it_1_a_wed_1", "2025-26", "it", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"),
        TimetableEntry("it_1_a_wed_2", "2025-26", "it", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"),
        TimetableEntry("it_1_a_wed_5", "2025-26", "it", "A", 1, "Wednesday", "V", "V", "14:00", "14:45", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "NS"),
        TimetableEntry("it_1_a_wed_6_t2", "2025-26", "it", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-203", "KM"),
        TimetableEntry("it_1_a_wed_7_8_p2", "2025-26", "it", "A", 1, "Wednesday", "VII", "VIII", "15:30", "17:00", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab"),

        // Thursday
        TimetableEntry("it_1_a_thu_1", "2025-26", "it", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"),
        TimetableEntry("it_1_a_thu_2", "2025-26", "it", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"),
        TimetableEntry("it_1_a_thu_5_t1", "2025-26", "it", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-203", "KM"),
        TimetableEntry("it_1_a_thu_6", "2025-26", "it", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203"),
        TimetableEntry("it_1_a_thu_7_t1", "2025-26", "it", "A", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-203"),

        // Friday
        TimetableEntry("it_1_a_fri_3", "2025-26", "it", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203"),
        TimetableEntry("it_1_a_fri_4", "2025-26", "it", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"),
        TimetableEntry("it_1_a_fri_5_t2", "2025-26", "it", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-203"),
        TimetableEntry("it_1_a_fri_6", "2025-26", "it", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"),
        TimetableEntry("it_1_a_fri_7_8_p1", "2025-26", "it", "A", 1, "Friday", "VII", "VIII", "15:30", "17:00", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab"),

        // Saturday
        TimetableEntry("it_1_a_sat_3", "2025-26", "it", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203"),
        TimetableEntry("it_1_a_sat_4", "2025-26", "it", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "NS"),

        // =========================================================================
        // PAGE 10: B.Tech (Mechanical Engineering), Semester I, SEC-A, Room: TL-205
        // =========================================================================
        // Monday
        TimetableEntry("me_1_a_mon_1", "2025-26", "me", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"),
        TimetableEntry("me_1_a_mon_2", "2025-26", "me", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"),
        TimetableEntry("me_1_a_mon_3", "2025-26", "me", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"),
        TimetableEntry("me_1_a_mon_5_6_p2", "2025-26", "me", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "RKY, NK & RS"),
        TimetableEntry("me_1_a_mon_5_6_p1", "2025-26", "me", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-205"),

        // Tuesday
        TimetableEntry("me_1_a_tue_1_2_p2", "2025-26", "me", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P2", "Workshop", "PSY/Nistha"),
        TimetableEntry("me_1_a_tue_5_6_p1", "2025-26", "me", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "CL, Preeti & RS"),
        TimetableEntry("me_1_a_tue_7", "2025-26", "me", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"),
        TimetableEntry("me_1_a_tue_8_t2", "2025-26", "me", "A", 1, "Tuesday", "VIII", "VIII", "16:15", "17:00", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-205"),

        // Wednesday
        TimetableEntry("me_1_a_wed_1", "2025-26", "me", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"),
        TimetableEntry("me_1_a_wed_2", "2025-26", "me", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"),
        TimetableEntry("me_1_a_wed_5_6_p1", "2025-26", "me", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "SY/M"),
        TimetableEntry("me_1_a_wed_7", "2025-26", "me", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205"),
        TimetableEntry("me_1_a_wed_8_t2", "2025-26", "me", "A", 1, "Wednesday", "VIII", "VIII", "16:15", "17:00", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T2", null, "TL-205", "VP"),

        // Thursday
        TimetableEntry("me_1_a_thu_1", "2025-26", "me", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"),
        TimetableEntry("me_1_a_thu_2", "2025-26", "me", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"),
        TimetableEntry("me_1_a_thu_5", "2025-26", "me", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"),
        TimetableEntry("me_1_a_thu_6_t1", "2025-26", "me", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T1", null, "TL-205", "VP"),

        // Friday
        TimetableEntry("me_1_a_fri_1", "2025-26", "me", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"),
        TimetableEntry("me_1_a_fri_2", "2025-26", "me", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"),
        TimetableEntry("me_1_a_fri_5_6_p2", "2025-26", "me", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-205"),
        TimetableEntry("me_1_a_fri_5_6_p1", "2025-26", "me", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P1", "Workshop", "Anjani Kumar Singh/Dr. Kuldeep Kumar"),

        // Saturday
        TimetableEntry("me_1_a_sat_1", "2025-26", "me", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205"),
        TimetableEntry("me_1_a_sat_2_t1", "2025-26", "me", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-205"),
        TimetableEntry("me_1_a_sat_3_4_p2", "2025-26", "me", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "SY/M"),

        // =========================================================================
        // PAGE 12: B.Tech (Electrical Engineering), Semester I, SEC-A, Room: TL-202
        // =========================================================================
        // Monday
        TimetableEntry("ee_1_a_mon_1", "2025-26", "ee", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"),
        TimetableEntry("ee_1_a_mon_2", "2025-26", "ee", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"),
        TimetableEntry("ee_1_a_mon_5_6_p1", "2025-26", "ee", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P1", "Wiring Lab", "KBS/RSG"),
        TimetableEntry("ee_1_a_mon_7_t2", "2025-26", "ee", "A", 1, "Monday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-202"),

        // Tuesday
        TimetableEntry("ee_1_a_tue_3", "2025-26", "ee", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"),
        TimetableEntry("ee_1_a_tue_4", "2025-26", "ee", "A", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202"),
        TimetableEntry("ee_1_a_tue_7", "2025-26", "ee", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202"),

        // Wednesday
        TimetableEntry("ee_1_a_wed_1_2_p1", "2025-26", "ee", "A", 1, "Wednesday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-202"),
        TimetableEntry("ee_1_a_wed_3", "2025-26", "ee", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"),
        TimetableEntry("ee_1_a_wed_4", "2025-26", "ee", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"),
        TimetableEntry("ee_1_a_wed_6_t2", "2025-26", "ee", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-202", "RK"),
        TimetableEntry("ee_1_a_wed_7_p2", "2025-26", "ee", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-202"),
        TimetableEntry("ee_1_a_wed_7_t1", "2025-26", "ee", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-202"),

        // Thursday
        TimetableEntry("ee_1_a_thu_1", "2025-26", "ee", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"),
        TimetableEntry("ee_1_a_thu_2", "2025-26", "ee", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"),
        TimetableEntry("ee_1_a_thu_3_4_p1", "2025-26", "ee", "A", 1, "Thursday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "AK/US"),
        TimetableEntry("ee_1_a_thu_3_4_p2", "2025-26", "ee", "A", 1, "Thursday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "SJ, Preeti, & RS"),
        TimetableEntry("ee_1_a_thu_6", "2025-26", "ee", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"),

        // Friday
        TimetableEntry("ee_1_a_fri_1", "2025-26", "ee", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"),
        TimetableEntry("ee_1_a_fri_2", "2025-26", "ee", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"),
        TimetableEntry("ee_1_a_fri_3_4_p2", "2025-26", "ee", "A", 1, "Friday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "RB/US"),
        TimetableEntry("ee_1_a_fri_3_4_p1", "2025-26", "ee", "A", 1, "Friday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "PS, CL & RS"),
        TimetableEntry("ee_1_a_fri_7", "2025-26", "ee", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"),

        // Saturday
        TimetableEntry("ee_1_a_sat_1", "2025-26", "ee", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"),
        TimetableEntry("ee_1_a_sat_2_t1", "2025-26", "ee", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-202", "RK"),
        TimetableEntry("ee_1_a_sat_3_4_p2", "2025-26", "ee", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P2", "Wiring Lab", "RSG/BS"),

        // =========================================================================
        // PAGE 14: B.Tech (Electronics & Comm. Engineering), Semester I, SEC-A, Room: TL-207
        // =========================================================================
        // Monday
        TimetableEntry("ece_1_a_mon_1", "2025-26", "ece", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"),
        TimetableEntry("ece_1_a_mon_2", "2025-26", "ece", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"),
        TimetableEntry("ece_1_a_mon_5", "2025-26", "ece", "A", 1, "Monday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207"),
        TimetableEntry("ece_1_a_mon_6", "2025-26", "ece", "A", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"),
        TimetableEntry("ece_1_a_mon_7_8_p1", "2025-26", "ece", "A", 1, "Monday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "RB/AG"),

        // Tuesday
        TimetableEntry("ece_1_a_tue_3_4_p1", "2025-26", "ece", "A", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "PS, NK & RS"),
        TimetableEntry("ece_1_a_tue_3_4_p2", "2025-26", "ece", "A", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P2", "Electronics Lab", "PL/NK"),
        TimetableEntry("ece_1_a_tue_5", "2025-26", "ece", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207"),
        TimetableEntry("ece_1_a_tue_6", "2025-26", "ece", "A", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"),
        TimetableEntry("ece_1_a_tue_7_8_p2", "2025-26", "ece", "A", 1, "Tuesday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "RB/AG"),

        // Wednesday
        TimetableEntry("ece_1_a_wed_1_2_p1", "2025-26", "ece", "A", 1, "Wednesday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-207"),
        TimetableEntry("ece_1_a_wed_3", "2025-26", "ece", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"),
        TimetableEntry("ece_1_a_wed_4", "2025-26", "ece", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "PL"),
        TimetableEntry("ece_1_a_wed_6_7_p2", "2025-26", "ece", "A", 1, "Wednesday", "VI", "VII", "14:45", "16:15", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-207"),
        TimetableEntry("ece_1_a_wed_6_7_p1", "2025-26", "ece", "A", 1, "Wednesday", "VI", "VII", "14:45", "16:15", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P1", "Electronics Lab", "PL/CH"),

        // Thursday
        TimetableEntry("ece_1_a_thu_1", "2025-26", "ece", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"),
        TimetableEntry("ece_1_a_thu_2", "2025-26", "ece", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"),
        TimetableEntry("ece_1_a_thu_5_t1", "2025-26", "ece", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T1", null, "TL-111", "PM"),

        // Friday
        TimetableEntry("ece_1_a_fri_1", "2025-26", "ece", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"),
        TimetableEntry("ece_1_a_fri_2", "2025-26", "ece", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"),
        TimetableEntry("ece_1_a_fri_5_t2", "2025-26", "ece", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T2", null, "TL-111", "PM"),
        TimetableEntry("ece_1_a_fri_6_t2", "2025-26", "ece", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-207"),

        // Saturday
        TimetableEntry("ece_1_a_sat_1", "2025-26", "ece", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "PL"),
        TimetableEntry("ece_1_a_sat_2_t1", "2025-26", "ece", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-207"),
        TimetableEntry("ece_1_a_sat_3_4_p2", "2025-26", "ece", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "CL & RS")
    )
}
