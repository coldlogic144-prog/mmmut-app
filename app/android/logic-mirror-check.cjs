// Node mirror of the pure-Kotlin logic shipped in the Android app.
// Validates: AuthEmail mapping/validation, roll pattern + branch map,
// seeded schedule engine determinism, leave calculator, token id, deep links.
const assert = require('assert');

const SUFFIX = '@mmmut.local';
const toEmail = (u) => { u = String(u||'').trim().toLowerCase(); return u.endsWith(SUFFIX) ? u : u + SUFFIX; };
assert.strictEqual(toEmail('rahul'), 'rahul@mmmut.local');
assert.strictEqual(toEmail('Rahul'), 'rahul@mmmut.local');
assert.strictEqual(toEmail('a@mmmut.local'), 'a@mmmut.local');

const ROLL_RE = /^\d{10}$/;
assert.ok(ROLL_RE.test('2026011001')); assert.ok(!ROLL_RE.test('abc'));

const MAP = { CED:'civil', CSD:'cse', EED:'ee', ECD:'ece', IOT:'eceiot', MED:'me', CHD:'chemical', ITC:'it' };
const rosterBranchToId = (b) => MAP[String(b||'').trim().toUpperCase()] || 'civil';
assert.strictEqual(rosterBranchToId('CSD'), 'cse');
assert.strictEqual(rosterBranchToId('iot'), 'eceiot');

// Seeded engine determinism (mirrors ScheduleEngine.hashSeed + shuffle)
function hashSeed(s){ let h = 1779033703 ^ s.length;
  for (let i=0;i<s.length;i++){ h = Math.imul(h ^ s.charCodeAt(i), 3432918353); h = (h<<13)|(h>>>19); }
  return () => { h = Math.imul(h ^ (h>>>16), 2246822519); h = Math.imul(h ^ (h>>>13), 3266489917); h ^= h>>>16; return (h>>>0)/4294967296; }; }
const rngA = hashSeed('ee::B'), rngB = hashSeed('ee::B');
assert.strictEqual(rngA(), rngB());

// Leave math (mirrors AttendanceUtils.computeLeaveInfo)
function leave(p,a,t){ const total=p+a; if(!total) return 'none'; const tg=t/100, c=p/total;
  if(c>=tg) return 'skip:'+Math.max(0,Math.floor(p/tg-total+1e-9));
  return 'attend:'+Math.max(1,Math.ceil((tg*total-p)/(1-tg)-1e-9)); }
assert.strictEqual(leave(0,0,75),'none');
assert.ok(leave(15,1,75).startsWith('skip:'));
assert.ok(leave(3,5,75).startsWith('attend:'));

// Token id stability + android_ prefix (mirrors TokenId.docId)
function tokenDocId(t){ let h=0; for(let i=0;i<t.length;i++) h=((h<<5)-h+t.charCodeAt(i))|0; return 'android_'+Math.abs(h).toString(36); }
assert.ok(tokenDocId('fake-token-123').startsWith('android_'));
assert.strictEqual(tokenDocId('x'), tokenDocId('x'));

// Deep links never land on plain Home
function routeFor(type, refId){ const t=String(type).toLowerCase();
  if(t==='examination'||t==='exam') return 'academics';
  if(t==='hostel'&&!refId) return 'hostel';
  if(refId) return 'notice/'+refId;
  return 'notices'; }
assert.strictEqual(routeFor('academic','abc'),'notice/abc');
assert.strictEqual(routeFor('examination','x'),'academics');
assert.strictEqual(routeFor('emergency','z'),'notice/z');

// Timetable filtering engine (mirrors ScheduleEngine.filterEntriesForStudent)
function filterTimetable(entries, b, sem, sec, tg, pg) {
  const bN = b.toLowerCase(), sN = sec.toUpperCase(), tN = tg.toUpperCase(), pN = pg.toUpperCase();
  return entries.filter(e => {
    if (e.branch.toLowerCase() !== bN || e.semester !== sem || e.section.toUpperCase() !== sN) return false;
    if (e.classType === 'LECTURE') {
      return (e.tutorialGroup == null || tN === 'N/A' || e.tutorialGroup.toUpperCase() === tN) &&
             (e.practicalGroup == null || pN === 'N/A' || e.practicalGroup.toUpperCase() === pN);
    }
    if (e.classType === 'TUTORIAL') {
      if (e.tutorialGroup == null) return true;
      if (tN === 'N/A' || !tN) return false;
      return e.tutorialGroup.toUpperCase() === tN;
    }
    if (e.classType === 'PRACTICAL') {
      if (e.practicalGroup == null) return true;
      if (pN === 'N/A' || !pN) return false;
      return e.practicalGroup.toUpperCase() === pN;
    }
    return true;
  });
}

const SAMPLE_ENTRIES = [
  { id: '1', branch: 'civil', section: 'A', semester: 1, classType: 'LECTURE', tutorialGroup: null, practicalGroup: null, subjectCode: 'BSM-131' },
  { id: '2', branch: 'civil', section: 'A', semester: 1, classType: 'TUTORIAL', tutorialGroup: 'T1', practicalGroup: null, subjectCode: 'BSM-110' },
  { id: '3', branch: 'civil', section: 'A', semester: 1, classType: 'TUTORIAL', tutorialGroup: 'T2', practicalGroup: null, subjectCode: 'BSM-110' },
  { id: '4', branch: 'civil', section: 'A', semester: 1, classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P1', subjectCode: 'BCE-121' },
  { id: '5', branch: 'civil', section: 'A', semester: 1, classType: 'PRACTICAL', tutorialGroup: null, practicalGroup: 'P2', subjectCode: 'BSM-131' }
];

const t1p1 = filterTimetable(SAMPLE_ENTRIES, 'civil', 1, 'A', 'T1', 'P1');
assert.ok(t1p1.some(e => e.tutorialGroup === 'T1'));
assert.ok(!t1p1.some(e => e.tutorialGroup === 'T2'));
assert.ok(t1p1.some(e => e.practicalGroup === 'P1'));
assert.ok(!t1p1.some(e => e.practicalGroup === 'P2'));

const t2p2 = filterTimetable(SAMPLE_ENTRIES, 'civil', 1, 'A', 'T2', 'P2');
assert.ok(t2p2.some(e => e.tutorialGroup === 'T2'));
assert.ok(!t2p2.some(e => e.tutorialGroup === 'T1'));
assert.ok(t2p2.some(e => e.practicalGroup === 'P2'));
assert.ok(!t2p2.some(e => e.practicalGroup === 'P1'));

const t1p2 = filterTimetable(SAMPLE_ENTRIES, 'civil', 1, 'A', 'T1', 'P2');
assert.ok(t1p2.some(e => e.tutorialGroup === 'T1') && t1p2.some(e => e.practicalGroup === 'P2'));

console.log('android-logic-mirror: ALL CHECKS PASSED');
