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
console.log('android-logic-mirror: ALL CHECKS PASSED');
