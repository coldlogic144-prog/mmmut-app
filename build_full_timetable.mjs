import fs from 'fs';

// Read original civil baseline
const civilBaseline = JSON.parse(fs.readFileSync('civil_baseline_entries.json', 'utf8'));

// We will construct all entries for the entire university (pages 1 to 20).
// Civil Sec A and B will be preserved exactly as in baseline.

function e(id, session, branch, section, sem, day, startP, endP, startT, endT, code, name, type, tutGroup, pracGroup, room, teacher) {
  const tutStr = tutGroup ? `"${tutGroup}"` : 'null';
  const pracStr = pracGroup ? `"${pracGroup}"` : 'null';
  const teacherStr = teacher ? `"${teacher}"` : '""';
  return `        TimetableEntry("${id}", "${session}", "${branch}", "${section}", ${sem}, "${day}", "${startP}", "${endP}", "${startT}", "${endT}", "${code}", "${name}", "${type}", ${tutStr}, ${pracStr}, "${room}", ${teacherStr}),`;
}

const entriesCode = [];

// 1. CIVIL ENGINEERING (Pages 1 & 2) - Exactly as baseline
entriesCode.push(`        // =========================================================================\n        // PAGE 1: B.Tech (Civil Engineering), Semester I, SEC-A, Room: TL-206\n        // =========================================================================`);
civilBaseline.filter(x => x.section === 'A').forEach(x => {
  entriesCode.push(e(x.id, x.session, x.branch, x.section, x.semester, x.day, x.startPeriod, x.endPeriod, x.startTime, x.endTime, x.subjectCode, x.subjectName, x.type, x.tutorialGroup, x.practicalGroup, x.room, x.teacher));
});

entriesCode.push(`\n        // =========================================================================\n        // PAGE 2: B.Tech (Civil Engineering), Semester I, SEC-B, Room: TL-206\n        // =========================================================================`);
civilBaseline.filter(x => x.section === 'B').forEach(x => {
  entriesCode.push(e(x.id, x.session, x.branch, x.section, x.semester, x.day, x.startPeriod, x.endPeriod, x.startTime, x.endTime, x.subjectCode, x.subjectName, x.type, x.tutorialGroup, x.practicalGroup, x.room, x.teacher));
});

// 2. CSE SEC-A (Page 3)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 3: B.Tech (Computer Sc. & Engineering), Semester I, SEC-A, Room: TL-109\n        // =========================================================================`);
// Monday
entriesCode.push(e("cse_1_a_mon_1_2_p2", "2025-26", "cse", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
entriesCode.push(e("cse_1_a_mon_3", "2025-26", "cse", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_mon_4", "2025-26", "cse", "A", 1, "Monday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_mon_5_6_p1", "2025-26", "cse", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-03", "Dr. Meenu, Anushka (RS)"));
entriesCode.push(e("cse_1_a_mon_5_6_p2", "2025-26", "cse", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-04", "SKS, Vaibhavi(RS)"));
// Tuesday
entriesCode.push(e("cse_1_a_tue_1_2_p1", "2025-26", "cse", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
entriesCode.push(e("cse_1_a_tue_3", "2025-26", "cse", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_tue_4", "2025-26", "cse", "A", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"));
entriesCode.push(e("cse_1_a_tue_5_6_p2", "2025-26", "cse", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "Dr. Meenu, Shreyansh (RS)"));
// Wednesday
entriesCode.push(e("cse_1_a_wed_3", "2025-26", "cse", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_wed_4", "2025-26", "cse", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"));
// Thursday
entriesCode.push(e("cse_1_a_thu_1", "2025-26", "cse", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_a_thu_2", "2025-26", "cse", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_a_thu_5_t1", "2025-26", "cse", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T1", null, "TL-109", "DM"));
entriesCode.push(e("cse_1_a_thu_6", "2025-26", "cse", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_thu_7_t1", "2025-26", "cse", "A", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T1", null, "TL-109", ""));
// Friday
entriesCode.push(e("cse_1_a_fri_1", "2025-26", "cse", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_a_fri_2", "2025-26", "cse", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_a_fri_5", "2025-26", "cse", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_a_fri_6_t2", "2025-26", "cse", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T2", null, "TL-109", "DM"));
entriesCode.push(e("cse_1_a_fri_7_t2", "2025-26", "cse", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T2", null, "TL-109", ""));
// Saturday
entriesCode.push(e("cse_1_a_sat_1", "2025-26", "cse", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_a_sat_2", "2025-26", "cse", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_a_sat_3_4_p1", "2025-26", "cse", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-04", "SKS, HARSH(RS)"));

// 3. CSE SEC-B (Page 4)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 4: B.Tech (Computer Sc. & Engineering), Semester I, SEC-B, Room: TL-109\n        // =========================================================================`);
// Monday
entriesCode.push(e("cse_1_b_mon_1", "2025-26", "cse", "B", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_b_mon_2", "2025-26", "cse", "B", 1, "Monday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_b_mon_3_4_p1", "2025-26", "cse", "B", 1, "Monday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-04", "SKS, MKS"));
entriesCode.push(e("cse_1_b_mon_5_t1", "2025-26", "cse", "B", 1, "Monday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T1", null, "TL-109", "SS"));
entriesCode.push(e("cse_1_b_mon_6", "2025-26", "cse", "B", 1, "Monday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-109", ""));
// Tuesday
entriesCode.push(e("cse_1_b_tue_1", "2025-26", "cse", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_b_tue_2", "2025-26", "cse", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_b_tue_3_4_p2", "2025-26", "cse", "B", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-04", "SKS, MKS"));
entriesCode.push(e("cse_1_b_tue_5", "2025-26", "cse", "B", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_b_tue_6_t2", "2025-26", "cse", "B", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T2", null, "TL-109", "SS"));
entriesCode.push(e("cse_1_b_tue_7_t2", "2025-26", "cse", "B", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T2", null, "TL-109", ""));
// Wednesday
entriesCode.push(e("cse_1_b_wed_1", "2025-26", "cse", "B", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-109", "KK"));
entriesCode.push(e("cse_1_b_wed_2", "2025-26", "cse", "B", 1, "Wednesday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-109", "SKS"));
entriesCode.push(e("cse_1_b_wed_6", "2025-26", "cse", "B", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", ""));
// Thursday
entriesCode.push(e("cse_1_b_thu_3", "2025-26", "cse", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_b_thu_4", "2025-26", "cse", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"));
entriesCode.push(e("cse_1_b_thu_5_6_p1", "2025-26", "cse", "B", 1, "Thursday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-03", "G6, VDB (RS)"));
// Friday
entriesCode.push(e("cse_1_b_fri_3", "2025-26", "cse", "B", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_b_fri_4", "2025-26", "cse", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-109", "Dr. Meenu"));
entriesCode.push(e("cse_1_b_fri_5_6_p2", "2025-26", "cse", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "G1, VDB (RS)"));
entriesCode.push(e("cse_1_b_fri_5_6_p1", "2025-26", "cse", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
// Saturday
entriesCode.push(e("cse_1_b_sat_1_2_p2", "2025-26", "cse", "B", 1, "Saturday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
entriesCode.push(e("cse_1_b_sat_3", "2025-26", "cse", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-109", ""));
entriesCode.push(e("cse_1_b_sat_4_t1", "2025-26", "cse", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T1", null, "TL-109", ""));

// 4. CSE SEC-C (Page 5)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 5: B.Tech (Computer Sc. & Engineering), Semester I, SEC-C, Room: TL-201\n        // =========================================================================`);
// Monday
entriesCode.push(e("cse_1_c_mon_3", "2025-26", "cse", "C", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "KK"));
entriesCode.push(e("cse_1_c_mon_4", "2025-26", "cse", "C", 1, "Monday", "IV", "IV", "11:40", "12:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_c_mon_6_t1", "2025-26", "cse", "C", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T1", null, "TL-201", "NK"));
entriesCode.push(e("cse_1_c_mon_7_t2", "2025-26", "cse", "C", 1, "Monday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T2", null, "TL-201", ""));
// Tuesday
entriesCode.push(e("cse_1_c_tue_1_2_p2", "2025-26", "cse", "C", 1, "Tuesday", "I", "II", "09:10", "10:50", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "G7, PS(RS)"));
entriesCode.push(e("cse_1_c_tue_3_p1", "2025-26", "cse", "C", 1, "Tuesday", "III", "III", "10:50", "11:40", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-03", "Ram, VAIBHAVI(RS)"));
entriesCode.push(e("cse_1_c_tue_5", "2025-26", "cse", "C", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_c_tue_6_t2", "2025-26", "cse", "C", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T2", null, "TL-201", "NK"));
entriesCode.push(e("cse_1_c_tue_7_t1", "2025-26", "cse", "C", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T1", null, "TL-201", ""));
// Wednesday
entriesCode.push(e("cse_1_c_wed_3", "2025-26", "cse", "C", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "KK"));
entriesCode.push(e("cse_1_c_wed_4", "2025-26", "cse", "C", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_c_wed_6_7_p1", "2025-26", "cse", "C", 1, "Wednesday", "VI", "VII", "14:45", "16:15", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-02", "G6, Divyanshi (RS)"));
// Thursday
entriesCode.push(e("cse_1_c_thu_1", "2025-26", "cse", "C", 1, "Thursday", "I", "I", "09:10", "10:00", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-201", "GF-6"));
entriesCode.push(e("cse_1_c_thu_2", "2025-26", "cse", "C", 1, "Thursday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_c_thu_3_4_p1", "2025-26", "cse", "C", 1, "Thursday", "III", "IV", "10:50", "12:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
entriesCode.push(e("cse_1_c_thu_6", "2025-26", "cse", "C", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_c_thu_7", "2025-26", "cse", "C", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
// Friday
entriesCode.push(e("cse_1_c_fri_1", "2025-26", "cse", "C", 1, "Friday", "I", "I", "09:10", "10:00", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-201", "GF-6"));
entriesCode.push(e("cse_1_c_fri_2", "2025-26", "cse", "C", 1, "Friday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_c_fri_6", "2025-26", "cse", "C", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "KK"));
entriesCode.push(e("cse_1_c_fri_7_p2", "2025-26", "cse", "C", 1, "Friday", "VII", "VII", "15:30", "16:15", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-01", "RAM, HARSH(RS)"));
// Saturday
entriesCode.push(e("cse_1_c_sat_1", "2025-26", "cse", "C", 1, "Saturday", "I", "I", "09:10", "10:00", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_c_sat_2", "2025-26", "cse", "C", 1, "Saturday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_c_sat_3_4_p2", "2025-26", "cse", "C", 1, "Saturday", "III", "IV", "10:50", "12:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));

// 5. CSE SEC-D (Page 6)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 6: B.Tech (Computer Sc. & Engineering), Semester I, SEC-D, Room: TL-201\n        // =========================================================================`);
// Monday
entriesCode.push(e("cse_1_d_mon_1", "2025-26", "cse", "D", 1, "Monday", "I", "I", "09:10", "10:00", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_mon_2", "2025-26", "cse", "D", 1, "Monday", "II", "II", "10:00", "10:50", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_d_mon_6_7_p1", "2025-26", "cse", "D", 1, "Monday", "VI", "VII", "14:45", "16:15", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P1", "ITRC-01", "G6, AM(RS)"));
// Tuesday
entriesCode.push(e("cse_1_d_tue_1", "2025-26", "cse", "D", 1, "Tuesday", "I", "I", "09:10", "10:00", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_d_tue_2", "2025-26", "cse", "D", 1, "Tuesday", "II", "II", "10:00", "10:50", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-201", "Dr. Meenu"));
entriesCode.push(e("cse_1_d_tue_3", "2025-26", "cse", "D", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "HP"));
entriesCode.push(e("cse_1_d_tue_4", "2025-26", "cse", "D", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_tue_5_6_p2", "2025-26", "cse", "D", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
entriesCode.push(e("cse_1_d_tue_6_7_p2", "2025-26", "cse", "D", 1, "Tuesday", "VI", "VII", "14:45", "16:15", "BCS-111", "Web Designing-1", "PRACTICAL", null, "P2", "ITRC-03", "G7, SG(RS)"));
// Wednesday
entriesCode.push(e("cse_1_d_wed_1", "2025-26", "cse", "D", 1, "Wednesday", "I", "I", "09:10", "10:00", "BCS-110", "Introduction to C Programming", "LECTURE", null, null, "TL-201", "RAM"));
entriesCode.push(e("cse_1_d_wed_2", "2025-26", "cse", "D", 1, "Wednesday", "II", "II", "10:00", "10:50", "BCS-111", "Web Designing-1", "LECTURE", null, null, "TL-201", "Dr. Meenu"));
entriesCode.push(e("cse_1_d_wed_3_4_p1", "2025-26", "cse", "D", 1, "Wednesday", "III", "IV", "10:50", "12:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
entriesCode.push(e("cse_1_d_wed_5_6_p1", "2025-26", "cse", "D", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P1", "ITRC-04", "RAM, MKS"));
// Thursday
entriesCode.push(e("cse_1_d_thu_3", "2025-26", "cse", "D", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "HP"));
entriesCode.push(e("cse_1_d_thu_4", "2025-26", "cse", "D", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_thu_7", "2025-26", "cse", "D", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_thu_8_t1", "2025-26", "cse", "D", 1, "Thursday", "VIII", "VIII", "16:15", "17:00", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T1", null, "TL-201", ""));
// Friday
entriesCode.push(e("cse_1_d_fri_3", "2025-26", "cse", "D", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-I", "LECTURE", null, null, "TL-201", "HP"));
entriesCode.push(e("cse_1_d_fri_4", "2025-26", "cse", "D", 1, "Friday", "IV", "IV", "11:40", "12:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_fri_7_t1", "2025-26", "cse", "D", 1, "Friday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T1", null, "TL-201", "SS"));
entriesCode.push(e("cse_1_d_fri_8_t2", "2025-26", "cse", "D", 1, "Friday", "VIII", "VIII", "16:15", "17:00", "BHS-101", "Universal Human Values: Understanding Harmony", "TUTORIAL", "T2", null, "TL-201", ""));
// Saturday
entriesCode.push(e("cse_1_d_sat_1_2_p2", "2025-26", "cse", "D", 1, "Saturday", "I", "II", "09:10", "10:50", "BCS-110", "Introduction to C Programming", "PRACTICAL", null, "P2", "ITRC-03", "RAM, SKS"));
entriesCode.push(e("cse_1_d_sat_3", "2025-26", "cse", "D", 1, "Saturday", "III", "III", "10:50", "11:40", "BHS-101", "Universal Human Values: Understanding Harmony", "LECTURE", null, null, "TL-201", ""));
entriesCode.push(e("cse_1_d_sat_4_t2", "2025-26", "cse", "D", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BSM-110", "Engineering Mathematics-I", "TUTORIAL", "T2", null, "TL-201", "SS"));

// 6. IT SEC-A (Page 7)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 7: B.Tech (Information Technology), Semester I, SEC-A, Room: TL-203\n        // =========================================================================`);
// Monday
entriesCode.push(e("it_1_a_mon_1_2_p1", "2025-26", "it", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P1", "ITRC-03", "NS"));
entriesCode.push(e("it_1_a_mon_1_2_p2", "2025-26", "it", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "CL-02", "DS"));
entriesCode.push(e("it_1_a_mon_5", "2025-26", "it", "A", 1, "Monday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_a_mon_6", "2025-26", "it", "A", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203", ""));
// Tuesday
entriesCode.push(e("it_1_a_tue_1_2_p2", "2025-26", "it", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P2", "ITRC-03", "NS"));
entriesCode.push(e("it_1_a_tue_1_2_p1", "2025-26", "it", "A", 1, "Tuesday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "CL-02", "DS"));
entriesCode.push(e("it_1_a_tue_5", "2025-26", "it", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
// Wednesday
entriesCode.push(e("it_1_a_wed_1", "2025-26", "it", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_a_wed_2", "2025-26", "it", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_a_wed_5", "2025-26", "it", "A", 1, "Wednesday", "V", "V", "14:00", "14:45", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "NS"));
entriesCode.push(e("it_1_a_wed_6_t2", "2025-26", "it", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-203", "KM"));
entriesCode.push(e("it_1_a_wed_7_8_p2", "2025-26", "it", "A", 1, "Wednesday", "VII", "VIII", "15:30", "17:00", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
// Thursday
entriesCode.push(e("it_1_a_thu_1", "2025-26", "it", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_a_thu_2", "2025-26", "it", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_a_thu_5_t1", "2025-26", "it", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-203", "KM"));
entriesCode.push(e("it_1_a_thu_6", "2025-26", "it", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_a_thu_7_t1", "2025-26", "it", "A", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-203", ""));
// Friday
entriesCode.push(e("it_1_a_fri_3", "2025-26", "it", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_a_fri_4", "2025-26", "it", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_a_fri_5_t2", "2025-26", "it", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-203", ""));
entriesCode.push(e("it_1_a_fri_6", "2025-26", "it", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_a_fri_7_8_p1", "2025-26", "it", "A", 1, "Friday", "VII", "VIII", "15:30", "17:00", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
// Saturday
entriesCode.push(e("it_1_a_sat_3", "2025-26", "it", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_a_sat_4", "2025-26", "it", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "NS"));

// 7. IT SEC-B (Page 8)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 8: B.Tech (Information Technology), Semester I, SEC-B, Room: TL-203\n        // =========================================================================`);
// Monday
entriesCode.push(e("it_1_b_mon_1", "2025-26", "it", "B", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_b_mon_2", "2025-26", "it", "B", 1, "Monday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_b_mon_3", "2025-26", "it", "B", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_b_mon_4", "2025-26", "it", "B", 1, "Monday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
// Tuesday
entriesCode.push(e("it_1_b_tue_1", "2025-26", "it", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_b_tue_2", "2025-26", "it", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_b_tue_3", "2025-26", "it", "B", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("it_1_b_tue_7_t2", "2025-26", "it", "B", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-203", ""));
entriesCode.push(e("it_1_b_tue_8_t1", "2025-26", "it", "B", 1, "Tuesday", "VIII", "VIII", "16:15", "17:00", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-203", "DG"));
// Wednesday
entriesCode.push(e("it_1_b_wed_1_2_p1", "2025-26", "it", "B", 1, "Wednesday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
entriesCode.push(e("it_1_b_wed_3", "2025-26", "it", "B", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_b_wed_4", "2025-26", "it", "B", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_b_wed_5_6_p1", "2025-26", "it", "B", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P1", "ITRC-03", "TH"));
entriesCode.push(e("it_1_b_wed_5_6_p2", "2025-26", "it", "B", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "CL-02", "SPS/DS"));
// Thursday
entriesCode.push(e("it_1_b_thu_1_2_p2", "2025-26", "it", "B", 1, "Thursday", "I", "II", "09:10", "10:50", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
entriesCode.push(e("it_1_b_thu_3", "2025-26", "it", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-203", "VB"));
entriesCode.push(e("it_1_b_thu_4_t2", "2025-26", "it", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-203", ""));
entriesCode.push(e("it_1_b_thu_5_6_p2", "2025-26", "it", "B", 1, "Thursday", "V", "VI", "14:00", "15:30", "BIT-104", "Internet and Web Designing", "PRACTICAL", null, "P2", "ITRC-02", "TH"));
// Friday
entriesCode.push(e("it_1_b_fri_1", "2025-26", "it", "B", 1, "Friday", "I", "I", "09:10", "10:00", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-203", "SPS"));
entriesCode.push(e("it_1_b_fri_2_t2", "2025-26", "it", "B", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-203", "DG"));
entriesCode.push(e("it_1_b_fri_6", "2025-26", "it", "B", 1, "Friday", "VI", "VI", "14:45", "15:30", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "TH"));
// Saturday
entriesCode.push(e("it_1_b_sat_1", "2025-26", "it", "B", 1, "Saturday", "I", "I", "09:10", "10:00", "BIT-104", "Internet and Web Designing", "LECTURE", null, null, "TL-203", "TH"));
entriesCode.push(e("it_1_b_sat_2", "2025-26", "it", "B", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-203", ""));
entriesCode.push(e("it_1_b_sat_3_4_p1", "2025-26", "it", "B", 1, "Saturday", "III", "IV", "10:50", "12:30", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "CL-02", "SPS/DS"));

// 8. CHEMICAL ENGINEERING (Page 9)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 9: B.Tech (Chemical Engineering), Semester I, SEC-A, Room: TL-110\n        // =========================================================================`);
// Monday
entriesCode.push(e("chem_1_a_mon_2", "2025-26", "chemical", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_mon_3", "2025-26", "chemical", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics -I", "LECTURE", null, null, "TL-110", "VB"));
entriesCode.push(e("chem_1_a_mon_7_8_p2", "2025-26", "chemical", "A", 1, "Monday", "VII", "VIII", "15:30", "17:00", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P2", "Physics Lab", ""));
// Tuesday
entriesCode.push(e("chem_1_a_tue_2", "2025-26", "chemical", "A", 1, "Tuesday", "II", "II", "10:00", "10:50", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_tue_3", "2025-26", "chemical", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics -I", "LECTURE", null, null, "TL-110", "VB"));
entriesCode.push(e("chem_1_a_tue_5_6_p1", "2025-26", "chemical", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BSM-131", "Engineering Physics", "PRACTICAL", null, "P1", "Physics Lab", ""));
entriesCode.push(e("chem_1_a_tue_7_t1", "2025-26", "chemical", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics -I", "TUTORIAL", "T1", null, "TL-110", "AG"));
// Wednesday
entriesCode.push(e("chem_1_a_wed_1_2_p2", "2025-26", "chemical", "A", 1, "Wednesday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P2", "ITRC-02", "AKG"));
entriesCode.push(e("chem_1_a_wed_1_4_p1", "2025-26", "chemical", "A", 1, "Wednesday", "I", "IV", "09:10", "12:30", "BME-104", "Manufacturing Techniques Workshop", "PRACTICAL", null, "P1", "Workshop", ""));
entriesCode.push(e("chem_1_a_wed_6", "2025-26", "chemical", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-110", "AKG"));
entriesCode.push(e("chem_1_a_wed_7_t1", "2025-26", "chemical", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T1", null, "TL-110", ""));
// Thursday
entriesCode.push(e("chem_1_a_thu_2", "2025-26", "chemical", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BSM-131", "Engineering Physics", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_thu_3", "2025-26", "chemical", "A", 1, "Thursday", "III", "III", "10:50", "11:40", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_thu_4", "2025-26", "chemical", "A", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-110", "AKG"));
entriesCode.push(e("chem_1_a_thu_5_8_p2", "2025-26", "chemical", "A", 1, "Thursday", "V", "VIII", "14:00", "17:00", "BME-104", "Manufacturing Techniques Workshop", "PRACTICAL", null, "P2", "Workshop", ""));
// Friday
entriesCode.push(e("chem_1_a_fri_2", "2025-26", "chemical", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics -I", "LECTURE", null, null, "TL-110", "VB"));
entriesCode.push(e("chem_1_a_fri_3", "2025-26", "chemical", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_fri_4", "2025-26", "chemical", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BIT-103", "Programming in C", "LECTURE", null, null, "TL-110", "AKG"));
entriesCode.push(e("chem_1_a_fri_5", "2025-26", "chemical", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BME-101", "Manufacturing Techniques Workshop", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_fri_6_t2", "2025-26", "chemical", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics -I", "TUTORIAL", "T2", null, "TL-110", "AG"));
entriesCode.push(e("chem_1_a_fri_7_t2", "2025-26", "chemical", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BHS-101", "Universal Human Values", "TUTORIAL", "T2", null, "TL-110", ""));
// Saturday
entriesCode.push(e("chem_1_a_sat_1_2_p1", "2025-26", "chemical", "A", 1, "Saturday", "I", "II", "09:10", "10:50", "BIT-103", "Programming in C", "PRACTICAL", null, "P1", "ITRC-01", "AKG"));
entriesCode.push(e("chem_1_a_sat_3", "2025-26", "chemical", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BME-101", "Manufacturing Techniques Workshop", "LECTURE", null, null, "TL-110", ""));
entriesCode.push(e("chem_1_a_sat_4", "2025-26", "chemical", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-101", "Universal Human Values", "LECTURE", null, null, "TL-110", ""));

// 9. MECHANICAL SEC-A (Page 10)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 10: B.Tech (Mechanical Engineering), Semester I, SEC-A, Room: TL-205\n        // =========================================================================`);
// Monday
entriesCode.push(e("me_1_a_mon_1", "2025-26", "me", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_a_mon_2", "2025-26", "me", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_a_mon_3", "2025-26", "me", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_a_mon_5_6_p2", "2025-26", "me", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "RKY, NK & RS"));
entriesCode.push(e("me_1_a_mon_5_6_p1", "2025-26", "me", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-205", ""));
// Tuesday
entriesCode.push(e("me_1_a_tue_1_4_p2", "2025-26", "me", "A", 1, "Tuesday", "I", "IV", "09:10", "12:30", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P2", "Workshop", "PSY/Nistha"));
entriesCode.push(e("me_1_a_tue_5_6_p1", "2025-26", "me", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "CL, Preeti & RS"));
entriesCode.push(e("me_1_a_tue_7", "2025-26", "me", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_a_tue_8_t2", "2025-26", "me", "A", 1, "Tuesday", "VIII", "VIII", "16:15", "17:00", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-205", ""));
// Wednesday
entriesCode.push(e("me_1_a_wed_1", "2025-26", "me", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_a_wed_2", "2025-26", "me", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"));
entriesCode.push(e("me_1_a_wed_5_6_p1", "2025-26", "me", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "SY/M"));
entriesCode.push(e("me_1_a_wed_7", "2025-26", "me", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205", ""));
entriesCode.push(e("me_1_a_wed_8_t2", "2025-26", "me", "A", 1, "Wednesday", "VIII", "VIII", "16:15", "17:00", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T2", null, "TL-205", "VP"));
// Thursday
entriesCode.push(e("me_1_a_thu_1", "2025-26", "me", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_a_thu_2", "2025-26", "me", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_a_thu_5", "2025-26", "me", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"));
entriesCode.push(e("me_1_a_thu_6_t1", "2025-26", "me", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T1", null, "TL-205", "VP"));
// Friday
entriesCode.push(e("me_1_a_fri_1", "2025-26", "me", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_a_fri_2", "2025-26", "me", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_a_fri_5_6_p2", "2025-26", "me", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-205", ""));
entriesCode.push(e("me_1_a_fri_5_8_p1", "2025-26", "me", "A", 1, "Friday", "V", "VIII", "14:00", "17:00", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P1", "Workshop", "Anjani Kumar Singh/Dr. Kuldeep Kumar"));
// Saturday
entriesCode.push(e("me_1_a_sat_1", "2025-26", "me", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205", ""));
entriesCode.push(e("me_1_a_sat_2_t1", "2025-26", "me", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-205", ""));
entriesCode.push(e("me_1_a_sat_3_4_p2", "2025-26", "me", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "SY/M"));

// 10. MECHANICAL SEC-B (Page 11)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 11: B.Tech (Mechanical Engineering), Semester I, SEC-B, Room: TL-205\n        // =========================================================================`);
// Monday
entriesCode.push(e("me_1_b_mon_1_2_p2", "2025-26", "me", "B", 1, "Monday", "I", "II", "09:10", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "Preeti & RS"));
entriesCode.push(e("me_1_b_mon_1_4_p1", "2025-26", "me", "B", 1, "Monday", "I", "IV", "09:10", "12:30", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P1", "Workshop", "Anjani Kumar Singh/Anuj"));
entriesCode.push(e("me_1_b_mon_6", "2025-26", "me", "B", 1, "Monday", "VI", "VI", "14:45", "15:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_b_mon_7_t1", "2025-26", "me", "B", 1, "Monday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T1", null, "TL-205", "NV"));
// Tuesday
entriesCode.push(e("me_1_b_tue_1", "2025-26", "me", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_b_tue_2", "2025-26", "me", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_b_tue_3", "2025-26", "me", "B", 1, "Tuesday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_b_tue_4", "2025-26", "me", "B", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"));
entriesCode.push(e("me_1_b_tue_5", "2025-26", "me", "B", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205", ""));
entriesCode.push(e("me_1_b_tue_6_8_p2", "2025-26", "me", "B", 1, "Tuesday", "VI", "VIII", "14:45", "17:00", "BME-104", "Manufacturing Practice Workshop", "PRACTICAL", null, "P2", "Workshop", "PSY/Nistha"));
// Wednesday
entriesCode.push(e("me_1_b_wed_3", "2025-26", "me", "B", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_b_wed_4", "2025-26", "me", "B", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-205", ""));
// Thursday
entriesCode.push(e("me_1_b_thu_1_2_p1", "2025-26", "me", "B", 1, "Thursday", "I", "II", "09:10", "10:50", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "M/PT"));
entriesCode.push(e("me_1_b_thu_3", "2025-26", "me", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-205", "RKY"));
entriesCode.push(e("me_1_b_thu_4", "2025-26", "me", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BME-104", "Manufacturing Practice Workshop", "LECTURE", null, null, "TL-205", "MG"));
entriesCode.push(e("me_1_b_thu_5_6_p2", "2025-26", "me", "B", 1, "Thursday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-205", ""));
entriesCode.push(e("me_1_b_thu_7_t2", "2025-26", "me", "B", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics-1", "TUTORIAL", "T2", null, "TL-205", "NV"));
// Friday
entriesCode.push(e("me_1_b_fri_1_2_p1", "2025-26", "me", "B", 1, "Friday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-205", ""));
entriesCode.push(e("me_1_b_fri_3", "2025-26", "me", "B", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_b_fri_4", "2025-26", "me", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-205", "SY"));
entriesCode.push(e("me_1_b_fri_5_6_p1", "2025-26", "me", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "Preeti & RS"));
entriesCode.push(e("me_1_b_fri_7_t1", "2025-26", "me", "B", 1, "Friday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-205", ""));
// Saturday
entriesCode.push(e("me_1_b_sat_1_2_p2", "2025-26", "me", "B", 1, "Saturday", "I", "II", "09:10", "10:50", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "SY/M"));
entriesCode.push(e("me_1_b_sat_3", "2025-26", "me", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics-1", "LECTURE", null, null, "TL-205", "SL"));
entriesCode.push(e("me_1_b_sat_4_t2", "2025-26", "me", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-205", ""));

// 11. ELECTRICAL SEC-A (Page 12)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 12: B.Tech (Electrical Engineering), Semester I, SEC-A, Room: TL-202\n        // =========================================================================`);
// Monday
entriesCode.push(e("ee_1_a_mon_1", "2025-26", "ee", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"));
entriesCode.push(e("ee_1_a_mon_2", "2025-26", "ee", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_a_mon_5_6_p1", "2025-26", "ee", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P1", "Wiring Lab", "KBS/RSG"));
entriesCode.push(e("ee_1_a_mon_7_t2", "2025-26", "ee", "A", 1, "Monday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-202", ""));
// Tuesday
entriesCode.push(e("ee_1_a_tue_3", "2025-26", "ee", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"));
entriesCode.push(e("ee_1_a_tue_4", "2025-26", "ee", "A", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202", ""));
entriesCode.push(e("ee_1_a_tue_7", "2025-26", "ee", "A", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202", ""));
// Wednesday
entriesCode.push(e("ee_1_a_wed_1_2_p1", "2025-26", "ee", "A", 1, "Wednesday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-202", ""));
entriesCode.push(e("ee_1_a_wed_3", "2025-26", "ee", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"));
entriesCode.push(e("ee_1_a_wed_4", "2025-26", "ee", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
entriesCode.push(e("ee_1_a_wed_6_t2", "2025-26", "ee", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-202", "RK"));
entriesCode.push(e("ee_1_a_wed_7_p2", "2025-26", "ee", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-202", ""));
entriesCode.push(e("ee_1_a_wed_7_t1", "2025-26", "ee", "A", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-202", ""));
// Thursday
entriesCode.push(e("ee_1_a_thu_1", "2025-26", "ee", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"));
entriesCode.push(e("ee_1_a_thu_2", "2025-26", "ee", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_a_thu_3_4_p1", "2025-26", "ee", "A", 1, "Thursday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "AK/US"));
entriesCode.push(e("ee_1_a_thu_3_4_p2", "2025-26", "ee", "A", 1, "Thursday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "SJ, Preeti, & RS"));
entriesCode.push(e("ee_1_a_thu_6", "2025-26", "ee", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "AK"));
// Friday
entriesCode.push(e("ee_1_a_fri_1", "2025-26", "ee", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "PS"));
entriesCode.push(e("ee_1_a_fri_2", "2025-26", "ee", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_a_fri_3_4_p2", "2025-26", "ee", "A", 1, "Friday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "RB/US"));
entriesCode.push(e("ee_1_a_fri_3_4_p1", "2025-26", "ee", "A", 1, "Friday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "PS, CL & RS"));
entriesCode.push(e("ee_1_a_fri_7", "2025-26", "ee", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
// Saturday
entriesCode.push(e("ee_1_a_sat_1", "2025-26", "ee", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
entriesCode.push(e("ee_1_a_sat_2_t1", "2025-26", "ee", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-202", "RK"));
entriesCode.push(e("ee_1_a_sat_3_4_p2", "2025-26", "ee", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P2", "Wiring Lab", "RSG/BS"));

// 12. ELECTRICAL SEC-B (Page 13)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 13: B.Tech (Electrical Engineering), Semester I, SEC-B, Room: TL-202\n        // =========================================================================`);
// Monday
entriesCode.push(e("ee_1_b_mon_3", "2025-26", "ee", "B", 1, "Monday", "III", "III", "10:50", "11:40", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
entriesCode.push(e("ee_1_b_mon_4", "2025-26", "ee", "B", 1, "Monday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "RB"));
entriesCode.push(e("ee_1_b_mon_5_t2", "2025-26", "ee", "B", 1, "Monday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T2", null, "TL-202", "VK"));
entriesCode.push(e("ee_1_b_mon_6_t2", "2025-26", "ee", "B", 1, "Monday", "VI", "VI", "14:45", "15:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-202", ""));
// Tuesday
entriesCode.push(e("ee_1_b_tue_1", "2025-26", "ee", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "SJ"));
entriesCode.push(e("ee_1_b_tue_2", "2025-26", "ee", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_b_tue_3_4_p1", "2025-26", "ee", "B", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "PT/US"));
entriesCode.push(e("ee_1_b_tue_6", "2025-26", "ee", "B", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
entriesCode.push(e("ee_1_b_tue_7_8_p1", "2025-26", "ee", "B", 1, "Tuesday", "VII", "VIII", "15:30", "17:00", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-202", ""));
// Wednesday
entriesCode.push(e("ee_1_b_wed_1", "2025-26", "ee", "B", 1, "Wednesday", "I", "I", "09:10", "10:00", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "SJ"));
entriesCode.push(e("ee_1_b_wed_2", "2025-26", "ee", "B", 1, "Wednesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_b_wed_7_8_p2", "2025-26", "ee", "B", 1, "Wednesday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "AK/US"));
// Thursday
entriesCode.push(e("ee_1_b_thu_1_2_p1", "2025-26", "ee", "B", 1, "Thursday", "I", "II", "09:10", "10:50", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P1", "Wiring Lab", "KBS/AV"));
entriesCode.push(e("ee_1_b_thu_3", "2025-26", "ee", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "RB"));
entriesCode.push(e("ee_1_b_thu_4", "2025-26", "ee", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202", ""));
entriesCode.push(e("ee_1_b_thu_7_p1", "2025-26", "ee", "B", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "PS, Preeti & RS"));
entriesCode.push(e("ee_1_b_thu_7_8_p2", "2025-26", "ee", "B", 1, "Thursday", "VII", "VIII", "15:30", "17:00", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "PS, NK,& RS"));
entriesCode.push(e("ee_1_b_thu_8_t1", "2025-26", "ee", "B", 1, "Thursday", "VIII", "VIII", "16:15", "17:00", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-202", ""));
// Friday
entriesCode.push(e("ee_1_b_fri_3", "2025-26", "ee", "B", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-202", "SJ"));
entriesCode.push(e("ee_1_b_fri_4", "2025-26", "ee", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BSM-110", "Engineering Mathematics I", "LECTURE", null, null, "TL-202", "AKB"));
entriesCode.push(e("ee_1_b_fri_5", "2025-26", "ee", "B", 1, "Friday", "V", "V", "14:00", "14:45", "BEE-108A", "Electrical Wiring & Estimation", "LECTURE", null, null, "TL-202", "KBS"));
entriesCode.push(e("ee_1_b_fri_6", "2025-26", "ee", "B", 1, "Friday", "VI", "VI", "14:45", "15:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-202", "RB"));
entriesCode.push(e("ee_1_b_fri_7_8_p2", "2025-26", "ee", "B", 1, "Friday", "VII", "VIII", "15:30", "17:00", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-202", ""));
// Saturday
entriesCode.push(e("ee_1_b_sat_1_2_p2", "2025-26", "ee", "B", 1, "Saturday", "I", "II", "09:10", "10:50", "BEE-108A", "Electrical Wiring & Estimation", "PRACTICAL", null, "P2", "Wiring Lab", "KBS/AV"));
entriesCode.push(e("ee_1_b_sat_3", "2025-26", "ee", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-202", ""));
entriesCode.push(e("ee_1_b_sat_4_t1", "2025-26", "ee", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BSM-110", "Engineering Mathematics I", "TUTORIAL", "T1", null, "TL-202", "VK"));

// 13. ECE SEC-A (Page 14)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 14: B.Tech (Electronics & Comm. Engineering), Semester I, SEC-A, Room: TL-207\n        // =========================================================================`);
// Monday
entriesCode.push(e("ece_1_a_mon_1", "2025-26", "ece", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_a_mon_2", "2025-26", "ece", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"));
entriesCode.push(e("ece_1_a_mon_5", "2025-26", "ece", "A", 1, "Monday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207", ""));
entriesCode.push(e("ece_1_a_mon_6", "2025-26", "ece", "A", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_a_mon_7_8_p1", "2025-26", "ece", "A", 1, "Monday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "RB/AG"));
// Tuesday
entriesCode.push(e("ece_1_a_tue_3_4_p1", "2025-26", "ece", "A", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "PS, NK & RS"));
entriesCode.push(e("ece_1_a_tue_3_4_p2", "2025-26", "ece", "A", 1, "Tuesday", "III", "IV", "10:50", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P2", "Electronics Lab", "PL/NK"));
entriesCode.push(e("ece_1_a_tue_5", "2025-26", "ece", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207", ""));
entriesCode.push(e("ece_1_a_tue_6", "2025-26", "ece", "A", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_a_tue_7_8_p2", "2025-26", "ece", "A", 1, "Tuesday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "RB/AG"));
// Wednesday
entriesCode.push(e("ece_1_a_wed_1_2_p1", "2025-26", "ece", "A", 1, "Wednesday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-207", ""));
entriesCode.push(e("ece_1_a_wed_3", "2025-26", "ece", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_a_wed_4", "2025-26", "ece", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "PL"));
entriesCode.push(e("ece_1_a_wed_6_7_p2", "2025-26", "ece", "A", 1, "Wednesday", "VI", "VII", "14:45", "16:15", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-207", ""));
entriesCode.push(e("ece_1_a_wed_6_7_p1", "2025-26", "ece", "A", 1, "Wednesday", "VI", "VII", "14:45", "16:15", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P1", "Electronics Lab", "PL/CH"));
// Thursday
entriesCode.push(e("ece_1_a_thu_1", "2025-26", "ece", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_a_thu_2", "2025-26", "ece", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"));
entriesCode.push(e("ece_1_a_thu_5_t1", "2025-26", "ece", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T1", null, "TL-111", "PM"));
// Friday
entriesCode.push(e("ece_1_a_fri_1", "2025-26", "ece", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_a_fri_2", "2025-26", "ece", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "DDS"));
entriesCode.push(e("ece_1_a_fri_5_t2", "2025-26", "ece", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T2", null, "TL-111", "PM"));
entriesCode.push(e("ece_1_a_fri_6_t2", "2025-26", "ece", "A", 1, "Friday", "VI", "VI", "14:45", "15:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-207", ""));
// Saturday
entriesCode.push(e("ece_1_a_sat_1", "2025-26", "ece", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "PL"));
entriesCode.push(e("ece_1_a_sat_2_t1", "2025-26", "ece", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-207", ""));
entriesCode.push(e("ece_1_a_sat_3_4_p2", "2025-26", "ece", "A", 1, "Saturday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "CL & RS"));

// 14. ECE SEC-B (Page 15)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 15: B.Tech (Electronics & Comm. Engineering), Semester I, SEC-B, Room: TL-207\n        // =========================================================================`);
// Monday
entriesCode.push(e("ece_1_b_mon_3", "2025-26", "ece", "B", 1, "Monday", "III", "III", "10:50", "11:40", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "SKM"));
entriesCode.push(e("ece_1_b_mon_4", "2025-26", "ece", "B", 1, "Monday", "IV", "IV", "11:40", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_b_mon_7_t1", "2025-26", "ece", "B", 1, "Monday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T1", null, "TL-207", "KY"));
// Tuesday
entriesCode.push(e("ece_1_b_tue_1", "2025-26", "ece", "B", 1, "Tuesday", "I", "I", "09:10", "10:00", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "AS"));
entriesCode.push(e("ece_1_b_tue_2", "2025-26", "ece", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_b_tue_3", "2025-26", "ece", "B", 1, "Tuesday", "III", "III", "10:50", "11:40", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207", ""));
entriesCode.push(e("ece_1_b_tue_4", "2025-26", "ece", "B", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_b_tue_5_6_p1", "2025-26", "ece", "B", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-207", ""));
entriesCode.push(e("ece_1_b_tue_7", "2025-26", "ece", "B", 1, "Tuesday", "VII", "VII", "15:30", "16:15", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-207", "SKM"));
// Wednesday
entriesCode.push(e("ece_1_b_wed_1", "2025-26", "ece", "B", 1, "Wednesday", "I", "I", "09:10", "10:00", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "AS"));
entriesCode.push(e("ece_1_b_wed_2", "2025-26", "ece", "B", 1, "Wednesday", "II", "II", "10:00", "10:50", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_b_wed_5_6_p1", "2025-26", "ece", "B", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-207", ""));
entriesCode.push(e("ece_1_b_wed_7", "2025-26", "ece", "B", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-207", ""));
// Thursday
entriesCode.push(e("ece_1_b_thu_1_2_p1", "2025-26", "ece", "B", 1, "Thursday", "I", "II", "09:10", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "CL, BR & RS"));
entriesCode.push(e("ece_1_b_thu_1_4_p2", "2025-26", "ece", "B", 1, "Thursday", "I", "IV", "09:10", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P2", "Electronics Lab", "SKM/GDB/P"));
entriesCode.push(e("ece_1_b_thu_3_t2", "2025-26", "ece", "B", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T2", null, "TL-207", "KY"));
entriesCode.push(e("ece_1_b_thu_4_t2", "2025-26", "ece", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-207", ""));
entriesCode.push(e("ece_1_b_thu_5_6_p1", "2025-26", "ece", "B", 1, "Thursday", "V", "VI", "14:00", "15:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "AS/SY"));
// Friday
entriesCode.push(e("ece_1_b_fri_3", "2025-26", "ece", "B", 1, "Friday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-207", "HC"));
entriesCode.push(e("ece_1_b_fri_4", "2025-26", "ece", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-207", "AS"));
entriesCode.push(e("ece_1_b_fri_5_6_p2", "2025-26", "ece", "B", 1, "Friday", "V", "VI", "14:00", "15:30", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "AS/SY"));
entriesCode.push(e("ece_1_b_fri_5_8_p1", "2025-26", "ece", "B", 1, "Friday", "V", "VIII", "14:00", "17:00", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P1", "Electronics Lab", "SKM/GDB"));
// Saturday
entriesCode.push(e("ece_1_b_sat_1_2_p2", "2025-26", "ece", "B", 1, "Saturday", "I", "II", "09:10", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "PPP, CL & RS"));
entriesCode.push(e("ece_1_b_sat_3", "2025-26", "ece", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-207", "PPP"));
entriesCode.push(e("ece_1_b_sat_4_t1", "2025-26", "ece", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-207", ""));

// 15. ECE SEC-C (Page 16)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 16: B.Tech (Electronics & Comm. Engineering), Semester I, SEC-C, Room: TL-204\n        // =========================================================================`);
// Monday
entriesCode.push(e("ece_1_c_mon_1_4_p1", "2025-26", "ece", "C", 1, "Monday", "I", "IV", "09:10", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P1", "Electronics Lab", "R/VSC"));
entriesCode.push(e("ece_1_c_mon_1_2_p2", "2025-26", "ece", "C", 1, "Monday", "I", "II", "09:10", "10:50", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "SS/AT"));
entriesCode.push(e("ece_1_c_mon_3_p2", "2025-26", "ece", "C", 1, "Monday", "III", "III", "10:50", "11:40", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-204", ""));
entriesCode.push(e("ece_1_c_mon_6", "2025-26", "ece", "C", 1, "Monday", "VI", "VI", "14:45", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "SJ"));
entriesCode.push(e("ece_1_c_mon_7", "2025-26", "ece", "C", 1, "Monday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-204", ""));
// Tuesday
entriesCode.push(e("ece_1_c_tue_3", "2025-26", "ece", "C", 1, "Tuesday", "III", "III", "10:50", "11:40", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NK"));
entriesCode.push(e("ece_1_c_tue_4", "2025-26", "ece", "C", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-204", "PKK"));
entriesCode.push(e("ece_1_c_tue_5_8_p2", "2025-26", "ece", "C", 1, "Tuesday", "V", "VIII", "14:00", "17:00", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P2", "Electronics Lab", "PKK/VSC"));
entriesCode.push(e("ece_1_c_tue_6_8_p1", "2025-26", "ece", "C", 1, "Tuesday", "VI", "VIII", "14:45", "17:00", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "SJ & RS"));
// Wednesday
entriesCode.push(e("ece_1_c_wed_1", "2025-26", "ece", "C", 1, "Wednesday", "I", "I", "09:10", "10:00", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-204", "PKK"));
entriesCode.push(e("ece_1_c_wed_2", "2025-26", "ece", "C", 1, "Wednesday", "II", "II", "10:00", "10:50", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NK"));
entriesCode.push(e("ece_1_c_wed_3_4_p2", "2025-26", "ece", "C", 1, "Wednesday", "III", "IV", "10:50", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "PS, NK & RS"));
entriesCode.push(e("ece_1_c_wed_6_t2", "2025-26", "ece", "C", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T2", null, "TL-204", "ST"));
entriesCode.push(e("ece_1_c_wed_7_t2", "2025-26", "ece", "C", 1, "Wednesday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-204", ""));
// Thursday
entriesCode.push(e("ece_1_c_thu_1", "2025-26", "ece", "C", 1, "Thursday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "MP"));
entriesCode.push(e("ece_1_c_thu_2", "2025-26", "ece", "C", 1, "Thursday", "II", "II", "10:00", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "SJ"));
entriesCode.push(e("ece_1_c_thu_6_t1", "2025-26", "ece", "C", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T1", null, "TL-204", "ST"));
entriesCode.push(e("ece_1_c_thu_7_t1", "2025-26", "ece", "C", 1, "Thursday", "VII", "VII", "15:30", "16:15", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-204", ""));
// Friday
entriesCode.push(e("ece_1_c_fri_1", "2025-26", "ece", "C", 1, "Friday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "MP"));
entriesCode.push(e("ece_1_c_fri_2", "2025-26", "ece", "C", 1, "Friday", "II", "II", "10:00", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "SJ"));
entriesCode.push(e("ece_1_c_fri_6", "2025-26", "ece", "C", 1, "Friday", "VI", "VI", "14:45", "15:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NK"));
entriesCode.push(e("ece_1_c_fri_7_8_p1", "2025-26", "ece", "C", 1, "Friday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "SS/AT"));
// Saturday
entriesCode.push(e("ece_1_c_sat_1", "2025-26", "ece", "C", 1, "Saturday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "MP"));
entriesCode.push(e("ece_1_c_sat_2", "2025-26", "ece", "C", 1, "Saturday", "II", "II", "10:00", "10:50", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-204", ""));
entriesCode.push(e("ece_1_c_sat_3_4_p1", "2025-26", "ece", "C", 1, "Saturday", "III", "IV", "10:50", "12:30", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-204", ""));

// 16. ECE(IOT) SEC-A (Page 17)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 17: B.Tech,ECE(IOT), Semester I, SEC-A, Room: TL-204\n        // =========================================================================`);
// Monday
entriesCode.push(e("eceiot_1_a_mon_1", "2025-26", "eceiot", "A", 1, "Monday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "KV"));
entriesCode.push(e("eceiot_1_a_mon_2", "2025-26", "eceiot", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "PS"));
entriesCode.push(e("eceiot_1_a_mon_3_4", "2025-26", "eceiot", "A", 1, "Monday", "III", "IV", "10:50", "12:30", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-204", "RP"));
entriesCode.push(e("eceiot_1_a_mon_5_8_p2", "2025-26", "eceiot", "A", 1, "Monday", "V", "VIII", "14:00", "17:00", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P2", "Electronics Lab", "RP/RK"));
entriesCode.push(e("eceiot_1_a_mon_6_7_p1", "2025-26", "eceiot", "A", 1, "Monday", "VI", "VII", "14:45", "16:15", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P1", "Electrical Lab", "VP/RB"));
// Tuesday
entriesCode.push(e("eceiot_1_a_tue_1", "2025-26", "eceiot", "A", 1, "Tuesday", "I", "I", "09:10", "10:00", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "KV"));
entriesCode.push(e("eceiot_1_a_tue_2", "2025-26", "eceiot", "A", 1, "Tuesday", "II", "II", "10:00", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "PS"));
entriesCode.push(e("eceiot_1_a_tue_5", "2025-26", "eceiot", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-204", ""));
entriesCode.push(e("eceiot_1_a_tue_6_t1", "2025-26", "eceiot", "A", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T1", null, "TL-204", "NT"));
// Wednesday
entriesCode.push(e("eceiot_1_a_wed_3", "2025-26", "eceiot", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BHS-102", "Technical Writing and Professional Communication", "LECTURE", null, null, "TL-204", ""));
entriesCode.push(e("eceiot_1_a_wed_4", "2025-26", "eceiot", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NP"));
entriesCode.push(e("eceiot_1_a_wed_5_t1", "2025-26", "eceiot", "A", 1, "Wednesday", "V", "V", "14:00", "14:45", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T1", null, "TL-204", ""));
// Thursday
entriesCode.push(e("eceiot_1_a_thu_1_2_p2", "2025-26", "eceiot", "A", 1, "Thursday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P2", "TL-204", ""));
entriesCode.push(e("eceiot_1_a_thu_3", "2025-26", "eceiot", "A", 1, "Thursday", "III", "III", "10:50", "11:40", "BSM-110", "Engineering Mathematics - I", "LECTURE", null, null, "TL-204", "KV"));
entriesCode.push(e("eceiot_1_a_thu_4", "2025-26", "eceiot", "A", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BSM-140", "Environmental Science and Green Chemistry", "LECTURE", null, null, "TL-204", "PS"));
entriesCode.push(e("eceiot_1_a_thu_5_8_p1", "2025-26", "eceiot", "A", 1, "Thursday", "V", "VIII", "14:00", "17:00", "BEC-106", "Electronic Components Testing and Measurement", "PRACTICAL", null, "P1", "Electronics Lab", "RP/SK"));
entriesCode.push(e("eceiot_1_a_thu_5_6_p2", "2025-26", "eceiot", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P2", "Chemistry Lab", "PPP, NK & RS"));
entriesCode.push(e("eceiot_1_a_thu_7_8_p2", "2025-26", "eceiot", "A", 1, "Thursday", "VII", "VIII", "15:30", "17:00", "BEE-110", "Basic Electrical Engineering", "PRACTICAL", null, "P2", "Electrical Lab", "VP/SS"));
// Friday
entriesCode.push(e("eceiot_1_a_fri_1_2_p1", "2025-26", "eceiot", "A", 1, "Friday", "I", "II", "09:10", "10:50", "BSM-140", "Environmental Science and Green Chemistry", "PRACTICAL", null, "P1", "Chemistry Lab", "NK & RS"));
entriesCode.push(e("eceiot_1_a_fri_3", "2025-26", "eceiot", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BEC-106", "Electronic Components Testing and Measurement", "LECTURE", null, null, "TL-204", "RP"));
entriesCode.push(e("eceiot_1_a_fri_4", "2025-26", "eceiot", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NP"));
entriesCode.push(e("eceiot_1_a_fri_7_t2", "2025-26", "eceiot", "A", 1, "Friday", "VII", "VII", "15:30", "16:15", "BSM-110", "Engineering Mathematics - I", "TUTORIAL", "T2", null, "TL-204", "NT"));
// Saturday
entriesCode.push(e("eceiot_1_a_sat_1_2_p1", "2025-26", "eceiot", "A", 1, "Saturday", "I", "II", "09:10", "10:50", "BHS-102", "Technical Writing and Professional Communication", "PRACTICAL", null, "P1", "TL-204", ""));
entriesCode.push(e("eceiot_1_a_sat_3_t2", "2025-26", "eceiot", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BHS-102", "Technical Writing and Professional Communication", "TUTORIAL", "T2", null, "TL-204", ""));
entriesCode.push(e("eceiot_1_a_sat_4", "2025-26", "eceiot", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BEE-110", "Basic Electrical Engineering", "LECTURE", null, null, "TL-204", "NP"));

// 17. BBA SEC-A (Page 18)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 18: BBA, Semester I, SEC-A, Room: TL-113\n        // =========================================================================`);
// Monday
entriesCode.push(e("bba_1_a_mon_2", "2025-26", "bba", "A", 1, "Monday", "II", "II", "10:00", "10:50", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-113", "UG"));
entriesCode.push(e("bba_1_a_mon_3_t1", "2025-26", "bba", "A", 1, "Monday", "III", "III", "10:50", "11:40", "BBA-116", "Quantitative Techniques for Business Research", "TUTORIAL", "T1", null, "TL-113", "ST"));
entriesCode.push(e("bba_1_a_mon_4_t1", "2025-26", "bba", "A", 1, "Monday", "IV", "IV", "11:40", "12:30", "BBA-114", "Financial Accounting", "TUTORIAL", "T1", null, "TL-113", "SG"));
entriesCode.push(e("bba_1_a_mon_5_t1", "2025-26", "bba", "A", 1, "Monday", "V", "V", "14:00", "14:45", "BHM-121", "Industrial Psychology", "TUTORIAL", "T1", null, "TL-113", ""));
// Tuesday
entriesCode.push(e("bba_1_a_tue_1", "2025-26", "bba", "A", 1, "Tuesday", "I", "I", "09:10", "10:00", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-113", ""));
entriesCode.push(e("bba_1_a_tue_3_t2", "2025-26", "bba", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BBA-114", "Financial Accounting", "TUTORIAL", "T2", null, "TL-113", "SG"));
entriesCode.push(e("bba_1_a_tue_4", "2025-26", "bba", "A", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-113", "JA"));
entriesCode.push(e("bba_1_a_tue_5", "2025-26", "bba", "A", 1, "Tuesday", "V", "V", "14:00", "14:45", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-113", "AJ"));
// Wednesday
entriesCode.push(e("bba_1_a_wed_1", "2025-26", "bba", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-113", ""));
entriesCode.push(e("bba_1_a_wed_2", "2025-26", "bba", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-113", "UG"));
entriesCode.push(e("bba_1_a_wed_3_t2", "2025-26", "bba", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BBA-116", "Quantitative Techniques for Business Research", "TUTORIAL", "T2", null, "TL-113", "ST"));
entriesCode.push(e("bba_1_a_wed_4_t1", "2025-26", "bba", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "TUTORIAL", "T1", null, "TL-113", "AKS"));
entriesCode.push(e("bba_1_a_wed_5", "2025-26", "bba", "A", 1, "Wednesday", "V", "V", "14:00", "14:45", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-113", "PO"));
entriesCode.push(e("bba_1_a_wed_6_t2", "2025-26", "bba", "A", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BHM-121", "Industrial Psychology", "TUTORIAL", "T2", null, "TL-113", ""));
// Thursday
entriesCode.push(e("bba_1_a_thu_1", "2025-26", "bba", "A", 1, "Thursday", "I", "I", "09:10", "10:00", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-113", ""));
entriesCode.push(e("bba_1_a_thu_2", "2025-26", "bba", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "AUC-108", "Intellectual Property Rights", "LECTURE", null, null, "TL-113", "AH"));
entriesCode.push(e("bba_1_a_thu_3_t2", "2025-26", "bba", "A", 1, "Thursday", "III", "III", "10:50", "11:40", "BBA-A01", "Business Communication for Managers", "TUTORIAL", "T2", null, "TL-113", "BAS"));
entriesCode.push(e("bba_1_a_thu_4", "2025-26", "bba", "A", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-113", "JA"));
entriesCode.push(e("bba_1_a_thu_5", "2025-26", "bba", "A", 1, "Thursday", "V", "V", "14:00", "14:45", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-113", "AJ"));
entriesCode.push(e("bba_1_a_thu_6", "2025-26", "bba", "A", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-113", "PO"));
// Friday
entriesCode.push(e("bba_1_a_fri_2", "2025-26", "bba", "A", 1, "Friday", "II", "II", "10:00", "10:50", "AUC-108", "Intellectual Property Rights", "LECTURE", null, null, "TL-113", "AH"));
entriesCode.push(e("bba_1_a_fri_3_t1", "2025-26", "bba", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BBA-A01", "Business Communication for Managers", "TUTORIAL", "T1", null, "TL-113", "BAS"));
entriesCode.push(e("bba_1_a_fri_4", "2025-26", "bba", "A", 1, "Friday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-113", "JA"));
entriesCode.push(e("bba_1_a_fri_5", "2025-26", "bba", "A", 1, "Friday", "V", "V", "14:00", "14:45", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-113", "PO"));
// Saturday
entriesCode.push(e("bba_1_a_sat_1_t2", "2025-26", "bba", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BBA-115", "Principles and Practices of Management", "TUTORIAL", "T2", null, "TL-113", "AKS"));
entriesCode.push(e("bba_1_a_sat_2", "2025-26", "bba", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-113", "AJ"));
entriesCode.push(e("bba_1_a_sat_4", "2025-26", "bba", "A", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-113", "UG"));

// 18. BBA SEC-B (Page 19)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 19: BBA, Semester I, SEC-B, Room: TL-114\n        // =========================================================================`);
// Monday
entriesCode.push(e("bba_1_b_mon_2", "2025-26", "bba", "B", 1, "Monday", "II", "II", "10:00", "10:50", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-114", "VS"));
entriesCode.push(e("bba_1_b_mon_3", "2025-26", "bba", "B", 1, "Monday", "III", "III", "10:50", "11:40", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-114", ""));
entriesCode.push(e("bba_1_b_mon_4_t1", "2025-26", "bba", "B", 1, "Monday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "TUTORIAL", "T1", null, "TL-114", "AKJ"));
entriesCode.push(e("bba_1_b_mon_5", "2025-26", "bba", "B", 1, "Monday", "V", "V", "14:00", "14:45", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-114", "SS"));
// Tuesday
entriesCode.push(e("bba_1_b_tue_2_t1", "2025-26", "bba", "B", 1, "Tuesday", "II", "II", "10:00", "10:50", "BBA-116", "Quantitative Techniques for Business Research", "TUTORIAL", "T1", null, "TL-114", "SY"));
entriesCode.push(e("bba_1_b_tue_3_t1", "2025-26", "bba", "B", 1, "Tuesday", "III", "III", "10:50", "11:40", "BHM-121", "Industrial Psychology", "TUTORIAL", "T1", null, "TL-114", ""));
entriesCode.push(e("bba_1_b_tue_4", "2025-26", "bba", "B", 1, "Tuesday", "IV", "IV", "11:40", "12:30", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-114", "AS"));
entriesCode.push(e("bba_1_b_tue_5", "2025-26", "bba", "B", 1, "Tuesday", "V", "V", "14:00", "14:45", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-114", "IK"));
entriesCode.push(e("bba_1_b_tue_6", "2025-26", "bba", "B", 1, "Tuesday", "VI", "VI", "14:45", "15:30", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-114", "SS"));
// Wednesday
entriesCode.push(e("bba_1_b_wed_1", "2025-26", "bba", "B", 1, "Wednesday", "I", "I", "09:10", "10:00", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-114", ""));
entriesCode.push(e("bba_1_b_wed_2", "2025-26", "bba", "B", 1, "Wednesday", "II", "II", "10:00", "10:50", "AUC-108", "Intellectual Property Rights", "LECTURE", null, null, "TL-114", "MO"));
entriesCode.push(e("bba_1_b_wed_3_t1", "2025-26", "bba", "B", 1, "Wednesday", "III", "III", "10:50", "11:40", "BBA-114", "Financial Accounting", "TUTORIAL", "T1", null, "TL-114", "SG"));
entriesCode.push(e("bba_1_b_wed_4", "2025-26", "bba", "B", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-114", "AS"));
entriesCode.push(e("bba_1_b_wed_5_t2", "2025-26", "bba", "B", 1, "Wednesday", "V", "V", "14:00", "14:45", "BHM-121", "Industrial Psychology", "TUTORIAL", "T2", null, "TL-114", ""));
entriesCode.push(e("bba_1_b_wed_6_t1", "2025-26", "bba", "B", 1, "Wednesday", "VI", "VI", "14:45", "15:30", "BBA-A01", "Business Communication for Managers", "TUTORIAL", "T1", null, "TL-114", "KUS"));
// Thursday
entriesCode.push(e("bba_1_b_thu_1", "2025-26", "bba", "B", 1, "Thursday", "I", "I", "09:10", "10:00", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-114", "IK"));
entriesCode.push(e("bba_1_b_thu_2_t2", "2025-26", "bba", "B", 1, "Thursday", "II", "II", "10:00", "10:50", "BBA-116", "Quantitative Techniques for Business Research", "TUTORIAL", "T2", null, "TL-114", "SY"));
entriesCode.push(e("bba_1_b_thu_4", "2025-26", "bba", "B", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-114", "VS"));
entriesCode.push(e("bba_1_b_thu_5_t2", "2025-26", "bba", "B", 1, "Thursday", "V", "V", "14:00", "14:45", "BBA-A01", "Business Communication for Managers", "TUTORIAL", "T2", null, "TL-114", "KUS"));
entriesCode.push(e("bba_1_b_thu_6_t2", "2025-26", "bba", "B", 1, "Thursday", "VI", "VI", "14:45", "15:30", "BBA-115", "Principles and Practices of Management", "TUTORIAL", "T2", null, "TL-114", "AKJ"));
// Friday
entriesCode.push(e("bba_1_b_fri_2", "2025-26", "bba", "B", 1, "Friday", "II", "II", "10:00", "10:50", "AUC-108", "Intellectual Property Rights", "LECTURE", null, null, "TL-114", "MO"));
entriesCode.push(e("bba_1_b_fri_4", "2025-26", "bba", "B", 1, "Friday", "IV", "IV", "11:40", "12:30", "BBA-115", "Principles and Practices of Management", "LECTURE", null, null, "TL-114", "IK"));
entriesCode.push(e("bba_1_b_fri_5", "2025-26", "bba", "B", 1, "Friday", "V", "V", "14:00", "14:45", "BHM-121", "Industrial Psychology", "LECTURE", null, null, "TL-114", ""));
entriesCode.push(e("bba_1_b_fri_6", "2025-26", "bba", "B", 1, "Friday", "VI", "VI", "14:45", "15:30", "BBA-A01", "Business Communication for Managers", "LECTURE", null, null, "TL-114", "SS"));
// Saturday
entriesCode.push(e("bba_1_b_sat_1_t2", "2025-26", "bba", "B", 1, "Saturday", "I", "I", "09:10", "10:00", "BBA-114", "Financial Accounting", "TUTORIAL", "T2", null, "TL-114", "SG"));
entriesCode.push(e("bba_1_b_sat_3", "2025-26", "bba", "B", 1, "Saturday", "III", "III", "10:50", "11:40", "BBA-114", "Financial Accounting", "LECTURE", null, null, "TL-114", "AS"));
entriesCode.push(e("bba_1_b_sat_4", "2025-26", "bba", "B", 1, "Saturday", "IV", "IV", "11:40", "12:30", "BBA-116", "Quantitative Techniques for Business Research", "LECTURE", null, null, "TL-114", "VS"));

// 19. B.PHARM (Page 20)
entriesCode.push(`\n        // =========================================================================\n        // PAGE 20: B.Pharm, Semester I, SEC-A, Room: L-115 / CH-206\n        // =========================================================================`);
// Monday
entriesCode.push(e("bpharm_1_a_mon_1_2_p1", "2025-26", "bpharm", "A", 1, "Monday", "I", "II", "09:10", "10:50", "BPT108P", "Healthcare Psychology and Communication Skills", "PRACTICAL", null, "P1", "L-115", "SKS/SP"));
entriesCode.push(e("bpharm_1_a_mon_3_4_p2", "2025-26", "bpharm", "A", 1, "Monday", "III", "IV", "10:50", "12:30", "BPT108P", "Healthcare Psychology and Communication Skills", "PRACTICAL", null, "P2", "L-115", "SC/SKS"));
entriesCode.push(e("bpharm_1_a_mon_5_6_p1", "2025-26", "bpharm", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BPT109P", "Human Anatomy and Physiology I", "PRACTICAL", null, "P1", "HAP Lab", "RK/SY"));
entriesCode.push(e("bpharm_1_a_mon_5_6_p2", "2025-26", "bpharm", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BPT110P", "Introduction to Pharmacognosy", "PRACTICAL", null, "P2", "Micro Lab", "AV/AD"));
entriesCode.push(e("bpharm_1_a_mon_5_6_p3", "2025-26", "bpharm", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BPT111P", "Pharmaceutical Inorganic and Analytical Chemistry", "PRACTICAL", null, "P3", "Chemistry Lab", "GS/NK"));
entriesCode.push(e("bpharm_1_a_mon_5_6_p4", "2025-26", "bpharm", "A", 1, "Monday", "V", "VI", "14:00", "15:30", "BPT107P", "General Pharmacy", "PRACTICAL", null, "P4", "Pharmaceutics Lab GF", "AJ/RD"));
// Tuesday
entriesCode.push(e("bpharm_1_a_tue_1", "2025-26", "bpharm", "A", 1, "Tuesday", "I", "I", "09:10", "10:00", "BPT105T", "Introduction to Pharmacognosy", "LECTURE", null, null, "CH-206", "AV"));
entriesCode.push(e("bpharm_1_a_tue_2", "2025-26", "bpharm", "A", 1, "Tuesday", "II", "II", "10:00", "10:50", "BPT104T", "Healthcare Psychology and Communication Skills", "LECTURE", null, null, "CH-206", "DSP"));
entriesCode.push(e("bpharm_1_a_tue_3", "2025-26", "bpharm", "A", 1, "Tuesday", "III", "III", "10:50", "11:40", "BPT106T", "Pharmaceutical Inorganic and Analytical Chemistry", "LECTURE", null, null, "CH-206", "GS"));
entriesCode.push(e("bpharm_1_a_tue_5_6_p3", "2025-26", "bpharm", "A", 1, "Tuesday", "V", "VI", "14:00", "15:30", "BPT108P", "Healthcare Psychology and Communication Skills", "PRACTICAL", null, "P3", "Micro Lab", "SC/SKS"));
entriesCode.push(e("bpharm_1_a_tue_7_8_p4", "2025-26", "bpharm", "A", 1, "Tuesday", "VII", "VIII", "15:30", "17:00", "BPT108P", "Healthcare Psychology and Communication Skills", "PRACTICAL", null, "P4", "Micro Lab", "SC/SKS"));
// Wednesday
entriesCode.push(e("bpharm_1_a_wed_1", "2025-26", "bpharm", "A", 1, "Wednesday", "I", "I", "09:10", "10:00", "BPT104T", "Healthcare Psychology and Communication Skills", "LECTURE", null, null, "CH-206", "DSP"));
entriesCode.push(e("bpharm_1_a_wed_2", "2025-26", "bpharm", "A", 1, "Wednesday", "II", "II", "10:00", "10:50", "BPT102T", "General Pharmacy", "LECTURE", null, null, "CH-206", "AJ"));
entriesCode.push(e("bpharm_1_a_wed_3", "2025-26", "bpharm", "A", 1, "Wednesday", "III", "III", "10:50", "11:40", "BPT106T", "Pharmaceutical Inorganic and Analytical Chemistry", "LECTURE", null, null, "CH-206", "GS"));
entriesCode.push(e("bpharm_1_a_wed_4", "2025-26", "bpharm", "A", 1, "Wednesday", "IV", "IV", "11:40", "12:30", "BPT103T", "Human Anatomy, Physiology and Pathophysiology I", "LECTURE", null, null, "CH-206", "SC"));
entriesCode.push(e("bpharm_1_a_wed_5_6_p1", "2025-26", "bpharm", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BPT110P", "Introduction to Pharmacognosy", "PRACTICAL", null, "P1", "L-115", "AV/AD"));
entriesCode.push(e("bpharm_1_a_wed_5_6_p2", "2025-26", "bpharm", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BPT111P", "Pharmaceutical Inorganic and Analytical Chemistry", "PRACTICAL", null, "P2", "L-115", "GS/TA"));
entriesCode.push(e("bpharm_1_a_wed_5_6_p3", "2025-26", "bpharm", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BPT107P", "General Pharmacy", "PRACTICAL", null, "P3", "L-115", "AJ/RD"));
entriesCode.push(e("bpharm_1_a_wed_5_6_p4", "2025-26", "bpharm", "A", 1, "Wednesday", "V", "VI", "14:00", "15:30", "BPT109P", "Human Anatomy and Physiology I", "PRACTICAL", null, "P4", "L-115", "SK/PKV"));
// Thursday
entriesCode.push(e("bpharm_1_a_thu_2", "2025-26", "bpharm", "A", 1, "Thursday", "II", "II", "10:00", "10:50", "BPT101T", "Human Anatomy, Physiology and Pathophysiology I", "LECTURE", null, null, "CH-206", "SO"));
entriesCode.push(e("bpharm_1_a_thu_3", "2025-26", "bpharm", "A", 1, "Thursday", "III", "III", "10:50", "11:40", "BPT104T", "Healthcare Psychology and Communication Skills", "LECTURE", null, null, "CH-206", "DSP"));
entriesCode.push(e("bpharm_1_a_thu_4", "2025-26", "bpharm", "A", 1, "Thursday", "IV", "IV", "11:40", "12:30", "BPT105T", "Introduction to Pharmacognosy", "LECTURE", null, null, "CH-206", "AV"));
entriesCode.push(e("bpharm_1_a_thu_5_6_p1", "2025-26", "bpharm", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BPT111P", "Pharmaceutical Inorganic and Analytical Chemistry", "PRACTICAL", null, "P1", "L-115", "GS/TA"));
entriesCode.push(e("bpharm_1_a_thu_5_6_p2", "2025-26", "bpharm", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BPT107P", "General Pharmacy", "PRACTICAL", null, "P2", "L-115", "AJ/SH"));
entriesCode.push(e("bpharm_1_a_thu_5_6_p3", "2025-26", "bpharm", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BPT109P", "Human Anatomy and Physiology I", "PRACTICAL", null, "P3", "L-115", "SK/PKV"));
entriesCode.push(e("bpharm_1_a_thu_5_6_p4", "2025-26", "bpharm", "A", 1, "Thursday", "V", "VI", "14:00", "15:30", "BPT110P", "Introduction to Pharmacognosy", "PRACTICAL", null, "P4", "L-115", "AV/AM"));
// Friday
entriesCode.push(e("bpharm_1_a_fri_1", "2025-26", "bpharm", "A", 1, "Friday", "I", "I", "09:10", "10:00", "BPT102T", "General Pharmacy", "LECTURE", null, null, "CH-206", "AJ"));
entriesCode.push(e("bpharm_1_a_fri_2", "2025-26", "bpharm", "A", 1, "Friday", "II", "II", "10:00", "10:50", "BPT106T", "Pharmaceutical Inorganic and Analytical Chemistry", "LECTURE", null, null, "CH-206", "GS"));
entriesCode.push(e("bpharm_1_a_fri_3", "2025-26", "bpharm", "A", 1, "Friday", "III", "III", "10:50", "11:40", "BPT104T", "Healthcare Psychology and Communication Skills", "LECTURE", null, null, "CH-206", "DSP"));
entriesCode.push(e("bpharm_1_a_fri_5_6_p1", "2025-26", "bpharm", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BPT107P", "General Pharmacy", "PRACTICAL", null, "P1", "L-115", "AJ/SH"));
entriesCode.push(e("bpharm_1_a_fri_5_6_p2", "2025-26", "bpharm", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BPT109P", "Human Anatomy and Physiology I", "PRACTICAL", null, "P2", "L-115", "RK/SY"));
entriesCode.push(e("bpharm_1_a_fri_5_6_p3", "2025-26", "bpharm", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BPT110P", "Introduction to Pharmacognosy", "PRACTICAL", null, "P3", "L-115", "AV/AM"));
entriesCode.push(e("bpharm_1_a_fri_5_6_p4", "2025-26", "bpharm", "A", 1, "Friday", "V", "VI", "14:00", "15:30", "BPT111P", "Pharmaceutical Inorganic and Analytical Chemistry", "PRACTICAL", null, "P4", "L-115", "GS/NK"));
// Saturday
entriesCode.push(e("bpharm_1_a_sat_1", "2025-26", "bpharm", "A", 1, "Saturday", "I", "I", "09:10", "10:00", "BPT101T", "Human Anatomy, Physiology and Pathophysiology I", "LECTURE", null, null, "CH-206", "SO"));
entriesCode.push(e("bpharm_1_a_sat_2", "2025-26", "bpharm", "A", 1, "Saturday", "II", "II", "10:00", "10:50", "BPT105T", "Introduction to Pharmacognosy", "LECTURE", null, null, "CH-206", "AV"));
entriesCode.push(e("bpharm_1_a_sat_3", "2025-26", "bpharm", "A", 1, "Saturday", "III", "III", "10:50", "11:40", "BPT102T", "General Pharmacy", "LECTURE", null, null, "CH-206", "AJ"));

const finalFileContent = `package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.TimetableEntry

object OfficialTimetableData {

    // Official Timetable Entries extracted directly from MMMUT Official Timetable PDF (Session 2025-26)
    // Covers all branches, semesters, sections, tutorial and practical groups.
    val ALL_ENTRIES: List<TimetableEntry> = listOf(
${entriesCode.join('\n')}
    )
}
`;

fs.writeFileSync('app/android/app/src/main/java/com/mmmut/ero/data/local/OfficialTimetableData.kt', finalFileContent);
console.log('Successfully generated complete OfficialTimetableData.kt!');
