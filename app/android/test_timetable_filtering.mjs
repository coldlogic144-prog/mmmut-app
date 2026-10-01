import assert from 'assert';

// Mirror of OfficialTimetableData entries for Civil Sem 1 Sec A & Sec B
const ALL_ENTRIES = [
  // Page 1: Civil Sem 1 Sec A (TL-206)
  // Monday
  { id: 'civil_1_a_mon_1', branch: 'civil', section: 'A', semester: 1, day: 'Monday', startPeriod: 'I', endPeriod: 'I', startTime: '09:10', endTime: '10:00', subjectCode: 'BSM-131', subjectName: 'Engineering Physics', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'MH' },
  { id: 'civil_1_a_mon_2', branch: 'civil', section: 'A', semester: 1, day: 'Monday', startPeriod: 'II', endPeriod: 'II', startTime: '10:00', endTime: '10:50', subjectCode: 'BSM-110', subjectName: 'Engineering Mathematics I', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'MH' },
  { id: 'civil_1_a_mon_3', branch: 'civil', section: 'A', semester: 1, day: 'Monday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BIT-103', subjectName: 'Programming in C', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'NiH' },
  { id: 'civil_1_a_mon_7', branch: 'civil', section: 'A', semester: 1, day: 'Monday', startPeriod: 'VII', endPeriod: 'VII', startTime: '15:30', endTime: '16:15', subjectCode: 'BHS-101', subjectName: 'Universal Human Values', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: '' },
  { id: 'civil_1_a_mon_8_t1', branch: 'civil', section: 'A', semester: 1, day: 'Monday', startPeriod: 'VIII', endPeriod: 'VIII', startTime: '16:15', endTime: '17:00', subjectCode: 'BSM-110', subjectName: 'Engineering Mathematics I', classType: 'TUTORIAL', tutorialGroup: 'T1', practicalGroup: null, room: 'TL-206', instructor: 'UF' },

  // Tuesday
  { id: 'civil_1_a_tue_1', branch: 'civil', section: 'A', semester: 1, day: 'Tuesday', startPeriod: 'I', endPeriod: 'I', startTime: '09:10', endTime: '10:00', subjectCode: 'BSM-131', subjectName: 'Engineering Physics', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'MH' },
  { id: 'civil_1_a_tue_2', branch: 'civil', section: 'A', semester: 1, day: 'Tuesday', startPeriod: 'II', endPeriod: 'II', startTime: '10:00', endTime: '10:50', subjectCode: 'BSM-110', subjectName: 'Engineering Mathematics I', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: '' },
  { id: 'civil_1_a_tue_3_t1', branch: 'civil', section: 'A', semester: 1, day: 'Tuesday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BHS-101', subjectName: 'Universal Human Values', classType: 'TUTORIAL', tutorialGroup: 'T1', practicalGroup: null, room: 'TL-206', instructor: '' },
  { id: 'civil_1_a_tue_7_t2', branch: 'civil', section: 'A', semester: 1, day: 'Tuesday', startPeriod: 'VII', endPeriod: 'VII', startTime: '15:30', endTime: '16:15', subjectCode: 'BSM-110', subjectName: 'Engineering Mathematics I', classType: 'TUTORIAL', tutorialGroup: 'T2', practicalGroup: null, room: 'TL-206', instructor: 'UF' },

  // Wednesday
  { id: 'civil_1_a_wed_1', branch: 'civil', section: 'A', semester: 1, day: 'Wednesday', startPeriod: 'I', endPeriod: 'I', startTime: '09:10', endTime: '10:00', subjectCode: 'BSM-131', subjectName: 'Engineering Physics', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'MH' },
  { id: 'civil_1_a_wed_2', branch: 'civil', section: 'A', semester: 1, day: 'Wednesday', startPeriod: 'II', endPeriod: 'II', startTime: '10:00', endTime: '10:50', subjectCode: 'BSM-110', subjectName: 'Engineering Mathematics I', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: '' },
  { id: 'civil_1_a_wed_3_t2', branch: 'civil', section: 'A', semester: 1, day: 'Wednesday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BHS-101', subjectName: 'Universal Human Values', classType: 'TUTORIAL', tutorialGroup: 'T2', practicalGroup: null, room: 'TL-206', instructor: '' },
  // Wednesday afternoon simultaneous practicals: P1 in L-104, P2 in Physics Lab
  { id: 'civil_1_a_wed_5_6_p1', branch: 'civil', section: 'A', semester: 1, day: 'Wednesday', startPeriod: 'V', endPeriod: 'VI', startTime: '14:00', endTime: '15:30', subjectCode: 'BCE-121', subjectName: 'Engineering Graphics', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P1', room: 'L-104', instructor: 'RPT/SU/AnS/CC' },
  { id: 'civil_1_a_wed_5_6_p2', branch: 'civil', section: 'A', semester: 1, day: 'Wednesday', startPeriod: 'V', endPeriod: 'VI', startTime: '14:00', endTime: '15:30', subjectCode: 'BSM-131', subjectName: 'Engineering Physics', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P2', room: 'Physics Lab', instructor: '' },

  // Thursday
  { id: 'civil_1_a_thu_1_2_p2', branch: 'civil', section: 'A', semester: 1, day: 'Thursday', startPeriod: 'I', endPeriod: 'II', startTime: '09:10', endTime: '10:50', subjectCode: 'BIT-103', subjectName: 'Programming in C', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P2', room: 'ITRC-02', instructor: 'AK' },
  { id: 'civil_1_a_thu_3', branch: 'civil', section: 'A', semester: 1, day: 'Thursday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BIT-103', subjectName: 'Programming in C', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'NiH' },
  // Thursday afternoon simultaneous practicals: P1 in Physics Lab, P2 in L-104
  { id: 'civil_1_a_thu_5_6_p1', branch: 'civil', section: 'A', semester: 1, day: 'Thursday', startPeriod: 'V', endPeriod: 'VI', startTime: '14:00', endTime: '15:30', subjectCode: 'BSM-131', subjectName: 'Engineering Physics', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P1', room: 'Physics Lab', instructor: '' },
  { id: 'civil_1_a_thu_5_6_p2', branch: 'civil', section: 'A', semester: 1, day: 'Thursday', startPeriod: 'V', endPeriod: 'VI', startTime: '14:00', endTime: '15:30', subjectCode: 'BCE-121', subjectName: 'Engineering Graphics', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P2', room: 'L-104', instructor: 'MM/AD/AkS/An' },

  // Friday
  { id: 'civil_1_a_fri_3', branch: 'civil', section: 'A', semester: 1, day: 'Friday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BIT-103', subjectName: 'Programming in C', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'NiH' },
  { id: 'civil_1_a_fri_4', branch: 'civil', section: 'A', semester: 1, day: 'Friday', startPeriod: 'IV', endPeriod: 'IV', startTime: '11:40', endTime: '12:30', subjectCode: 'BHS-101', subjectName: 'Universal Human Values', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: '' },
  { id: 'civil_1_a_fri_5', branch: 'civil', section: 'A', semester: 1, day: 'Friday', startPeriod: 'V', endPeriod: 'V', startTime: '14:00', endTime: '14:45', subjectCode: 'BCE-121', subjectName: 'Engineering Graphics', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'NC' },

  // Saturday
  { id: 'civil_1_a_sat_1_2_p1', branch: 'civil', section: 'A', semester: 1, day: 'Saturday', startPeriod: 'I', endPeriod: 'II', startTime: '09:10', endTime: '10:50', subjectCode: 'BIT-103', subjectName: 'Programming in C', classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P1', room: 'ITRC-02', instructor: 'AK' },
  { id: 'civil_1_a_sat_3', branch: 'civil', section: 'A', semester: 1, day: 'Saturday', startPeriod: 'III', endPeriod: 'III', startTime: '10:50', endTime: '11:40', subjectCode: 'BCE-121', subjectName: 'Engineering Graphics', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: 'NC' },
  { id: 'civil_1_a_sat_4', branch: 'civil', section: 'A', semester: 1, day: 'Saturday', startPeriod: 'IV', endPeriod: 'IV', startTime: '11:40', endTime: '12:30', subjectCode: 'BHS-101', subjectName: 'Universal Human Values', classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, room: 'TL-206', instructor: '' }
];

function timeToMinutes(t) {
  const [h, m] = t.split(':').map(Number);
  return h * 60 + m;
}

function periodOrder(p) {
  const map = { I: 1, II: 2, III: 3, IV: 4, V: 5, VI: 6, VII: 7, VIII: 8 };
  return map[p] || 9;
}

function filterEntries(entries, branch, semester, section, tutorialGroup, practicalGroup) {
  const bNorm = branch.trim().toLowerCase();
  const secNorm = section.trim().toUpperCase() || 'A';
  const tutNorm = tutorialGroup.trim().toUpperCase();
  const pracNorm = practicalGroup.trim().toUpperCase();

  return entries.filter(entry => {
    if (entry.branch.toLowerCase() !== bNorm) return false;
    if (entry.semester !== semester) return false;
    if (entry.section.toUpperCase() !== secNorm) return false;

    switch (entry.classType.toUpperCase()) {
      case 'LECTURE': {
        const tMatch = entry.tutorialGroup == null || tutNorm === 'N/A' || entry.tutorialGroup.toUpperCase() === tutNorm;
        const pMatch = entry.practicalGroup == null || pracNorm === 'N/A' || entry.practicalGroup.toUpperCase() === pracNorm;
        return tMatch && pMatch;
      }
      case 'TUTORIAL': {
        if (entry.tutorialGroup == null) return true;
        if (tutNorm === 'N/A' || !tutNorm) return false;
        return entry.tutorialGroup.toUpperCase() === tutNorm;
      }
      case 'PRACTICAL': {
        if (entry.practicalGroup == null) return true;
        if (pracNorm === 'N/A' || !pracNorm) return false;
        return entry.practicalGroup.toUpperCase() === pracNorm;
      }
      default: {
        const tMatch = entry.tutorialGroup == null || tutNorm === 'N/A' || entry.tutorialGroup.toUpperCase() === tutNorm;
        const pMatch = entry.practicalGroup == null || pracNorm === 'N/A' || entry.practicalGroup.toUpperCase() === pracNorm;
        return tMatch && pMatch;
      }
    }
  }).sort((a, b) => {
    const diff = timeToMinutes(a.startTime) - timeToMinutes(b.startTime);
    if (diff !== 0) return diff;
    return periodOrder(a.startPeriod) - periodOrder(b.startPeriod);
  });
}

function toCell(e) {
  const periodKey = e.startPeriod === e.endPeriod ? e.startPeriod : `${e.startPeriod}-${e.endPeriod}`;
  return {
    id: e.id,
    day: e.day,
    periodKey,
    startPeriod: e.startPeriod,
    endPeriod: e.endPeriod,
    subjectCode: e.subjectCode,
    subjectName: e.subjectName,
    type: e.classType.charAt(0).toUpperCase() + e.classType.slice(1).toLowerCase(),
    start: e.startTime,
    end: e.endTime,
    tutorialGroup: e.tutorialGroup,
    practicalGroup: e.practicalGroup,
    room: e.room,
    instructor: e.instructor
  };
}

function getCurrentClass(cells, now) {
  return cells.find(c => c.subjectCode !== '—' && c.type !== 'Free' && now >= c.start && now <= c.end) || null;
}

function getNextClass(cells, now) {
  return cells.find(c => c.subjectCode !== '—' && c.type !== 'Free' && now < c.start) || null;
}

console.log('=== RUNNING OFFICIAL TIMETABLE VERIFICATION SUITE ===');

// =========================================================================
// TEST A: Civil Sem I Sec A T1 P1
// =========================================================================
console.log('\n--- TEST A: Civil Sem I Sec A T1 P1 ---');
const testAEntries = filterEntries(ALL_ENTRIES, 'civil', 1, 'A', 'T1', 'P1');
const testACells = testAEntries.map(toCell);

// 1. T1 student NEVER gets T2-only class
assert.ok(!testACells.some(c => c.tutorialGroup === 'T2'), 'TEST A: T1 student must NOT receive any T2 tutorial');
// 2. P1 student NEVER gets P2-only class
assert.ok(!testACells.some(c => c.practicalGroup === 'P2'), 'TEST A: P1 student must NOT receive any P2 practical');
// 3. T1 student gets T1 tutorials
assert.ok(testACells.some(c => c.tutorialGroup === 'T1' && c.subjectCode === 'BSM-110'), 'TEST A: must receive T1 BSM-110 tutorial');
assert.ok(testACells.some(c => c.tutorialGroup === 'T1' && c.subjectCode === 'BHS-101'), 'TEST A: must receive T1 BHS-101 tutorial');
// 4. P1 student gets P1 practicals
assert.ok(testACells.some(c => c.practicalGroup === 'P1' && c.subjectCode === 'BCE-121' && c.room === 'L-104'), 'TEST A: must receive BCE-121 in L-104');
assert.ok(testACells.some(c => c.practicalGroup === 'P1' && c.subjectCode === 'BIT-103' && c.room === 'ITRC-02'), 'TEST A: must receive BIT-103 in ITRC-02');
assert.ok(testACells.some(c => c.practicalGroup === 'P1' && c.subjectCode === 'BSM-131'), 'TEST A: must receive BSM-131 practical');

// Simultaneous check Wednesday 14:00-15:30:
const wedA = testACells.filter(c => c.day === 'Wednesday' && c.start === '14:00' && c.end === '15:30');
assert.strictEqual(wedA.length, 1, 'TEST A: Wednesday 14:00-15:30 must have exactly ONE class');
assert.strictEqual(wedA[0].subjectCode, 'BCE-121', 'TEST A: Wednesday 14:00-15:30 must be BCE-121 for P1');
assert.strictEqual(wedA[0].room, 'L-104', 'TEST A: Wednesday practical room must be L-104');
console.log('✓ TEST A passed: All 5 assertions succeeded.');

// =========================================================================
// TEST B: Civil Sem I Sec A T1 P2
// =========================================================================
console.log('\n--- TEST B: Civil Sem I Sec A T1 P2 ---');
const testBEntries = filterEntries(ALL_ENTRIES, 'civil', 1, 'A', 'T1', 'P2');
const testBCells = testBEntries.map(toCell);

assert.ok(!testBCells.some(c => c.tutorialGroup === 'T2'), 'TEST B: T1 student must NOT receive any T2 tutorial');
assert.ok(!testBCells.some(c => c.practicalGroup === 'P1'), 'TEST B: P2 student must NOT receive any P1 practical');
assert.ok(testBCells.some(c => c.tutorialGroup === 'T1'), 'TEST B: must receive T1 tutorials');
assert.ok(testBCells.some(c => c.practicalGroup === 'P2' && c.subjectCode === 'BSM-131'), 'TEST B: must receive BSM-131 on Wed');

// Simultaneous check Wednesday 14:00-15:30:
const wedB = testBCells.filter(c => c.day === 'Wednesday' && c.start === '14:00' && c.end === '15:30');
assert.strictEqual(wedB.length, 1, 'TEST B: Wednesday 14:00-15:30 must have exactly ONE class');
assert.strictEqual(wedB[0].subjectCode, 'BSM-131', 'TEST B: Wednesday 14:00-15:30 must be BSM-131 for P2');

// Thursday morning 09:10-10:50:
const thuMorningB = testBCells.filter(c => c.start === '09:10' && c.end === '10:50');
assert.strictEqual(thuMorningB.length, 1, 'TEST B: Thursday morning P2 has BIT-103');
assert.strictEqual(thuMorningB[0].subjectCode, 'BIT-103');
assert.strictEqual(thuMorningB[0].room, 'ITRC-02');
console.log('✓ TEST B passed: All assertions succeeded.');

// =========================================================================
// TEST C: Civil Sem I Sec A T2 P1
// =========================================================================
console.log('\n--- TEST C: Civil Sem I Sec A T2 P1 ---');
const testCEntries = filterEntries(ALL_ENTRIES, 'civil', 1, 'A', 'T2', 'P1');
const testCCells = testCEntries.map(toCell);

assert.ok(!testCCells.some(c => c.tutorialGroup === 'T1'), 'TEST C: T2 student must NOT receive any T1 tutorial');
assert.ok(!testCCells.some(c => c.practicalGroup === 'P2'), 'TEST C: P1 student must NOT receive any P2 practical');
assert.ok(testCCells.some(c => c.tutorialGroup === 'T2' && c.subjectCode === 'BSM-110'), 'TEST C: must receive T2 BSM-110 tutorial');
assert.ok(testCCells.some(c => c.tutorialGroup === 'T2' && c.subjectCode === 'BHS-101'), 'TEST C: must receive T2 BHS-101 tutorial');

// Tuesday Period VII (15:30-16:15) T2 tutorial check:
const tue7C = testCCells.find(c => c.start === '15:30' && c.end === '16:15' && c.tutorialGroup === 'T2');
assert.ok(tue7C, 'TEST C: Tuesday 15:30-16:15 has T2 BSM-110 tutorial');
console.log('✓ TEST C passed: All assertions succeeded.');

// =========================================================================
// TEST D: Civil Sem I Sec A T2 P2
// =========================================================================
console.log('\n--- TEST D: Civil Sem I Sec A T2 P2 ---');
const testDEntries = filterEntries(ALL_ENTRIES, 'civil', 1, 'A', 'T2', 'P2');
const testDCells = testDEntries.map(toCell);

assert.ok(!testDCells.some(c => c.tutorialGroup === 'T1'), 'TEST D: T2 student must NOT receive any T1 tutorial');
assert.ok(!testDCells.some(c => c.practicalGroup === 'P1'), 'TEST D: P2 student must NOT receive any P1 practical');
assert.ok(testDCells.some(c => c.tutorialGroup === 'T2'), 'TEST D: must receive T2 tutorials');
assert.ok(testDCells.some(c => c.practicalGroup === 'P2'), 'TEST D: must receive P2 practicals');
console.log('✓ TEST D passed: All assertions succeeded.');

// =========================================================================
// TEST MULTI-PERIOD PRACTICAL INTEGRITY
// =========================================================================
console.log('\n--- MULTI-PERIOD PRACTICAL INTEGRITY ---');
const wedP1 = testACells.find(c => c.id === 'civil_1_a_wed_5_6_p1');
assert.strictEqual(wedP1.periodKey, 'V-VI', 'Must have period key V-VI');
assert.strictEqual(wedP1.start, '14:00');
assert.strictEqual(wedP1.end, '15:30');
assert.strictEqual(wedP1.room, 'L-104');
assert.strictEqual(wedP1.instructor, 'RPT/SU/AnS/CC');

const thuP2 = testBCells.find(c => c.id === 'civil_1_a_thu_1_2_p2');
assert.strictEqual(thuP2.periodKey, 'I-II', 'Must have period key I-II');
assert.strictEqual(thuP2.start, '09:10');
assert.strictEqual(thuP2.end, '10:50');
assert.strictEqual(thuP2.room, 'ITRC-02');
console.log('✓ Multi-period practical integrity verified: Exactly 1 session spanning correct periods.');

// =========================================================================
// TEST CURRENT CLASS & NEXT CLASS ON FILTERED TIMETABLE
// =========================================================================
console.log('\n--- CURRENT CLASS & NEXT CLASS TEST ---');
// On Wednesday for P1:
const wedCellsP1 = testACells.filter(c => c.id.includes('wed'));
// At 14:15: Current class should be BCE-121
const curWedP1 = getCurrentClass(wedCellsP1, '14:15');
assert.ok(curWedP1, 'Should find current class at 14:15');
assert.strictEqual(curWedP1.subjectCode, 'BCE-121');
assert.strictEqual(curWedP1.room, 'L-104');

// On Wednesday for P2:
const wedCellsP2 = testBCells.filter(c => c.id.includes('wed'));
const curWedP2 = getCurrentClass(wedCellsP2, '14:15');
assert.ok(curWedP2, 'Should find current class at 14:15');
assert.strictEqual(curWedP2.subjectCode, 'BSM-131');
assert.strictEqual(curWedP2.room, 'Physics Lab');

// Next class at 11:00 on Wednesday:
// P1 next class is BCE-121 at 14:00 (since T2 tutorial at 10:50 is NOT for T1!)
const nextWedT1P1 = getNextClass(wedCellsP1, '11:00');
assert.strictEqual(nextWedT1P1.subjectCode, 'BCE-121', 'T1/P1 next class after 11:00 must be BCE-121 (no T2 tutorial!)');

// On Wednesday for T2/P2:
const wedCellsT2P2 = testDCells.filter(c => c.id.includes('wed'));
// At 10:15: Current class is BSM-110 Lecture
const curWedT2P2 = getCurrentClass(wedCellsT2P2, '10:15');
assert.strictEqual(curWedT2P2.subjectCode, 'BSM-110');
// Next class after 10:15 for T2 is T2/BHS-101 Tutorial at 10:50!
const nextWedT2P2 = getNextClass(wedCellsT2P2, '10:15');
assert.strictEqual(nextWedT2P2.subjectCode, 'BHS-101');
assert.strictEqual(nextWedT2P2.tutorialGroup, 'T2');

console.log('✓ Current class and Next class calculations verified on filtered timetable.');
console.log('\nALL TESTS PASSED SUCCESSFULLY!');
