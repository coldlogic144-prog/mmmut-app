import fs from 'fs';

const content = fs.readFileSync('app/android/app/src/main/java/com/mmmut/ero/data/local/OfficialTimetableData.kt', 'utf8');
const entryRegex = /TimetableEntry\("([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*"([^"]+)",\s*(\d+)/g;
let match;
const counts = {};
while ((match = entryRegex.exec(content)) !== null) {
  const [_, id, session, branch, section, sem] = match;
  const key = `${branch} | Sem ${sem} | Sec ${section}`;
  counts[key] = (counts[key] || 0) + 1;
}
console.log('Current Entry Counts in OfficialTimetableData.kt:');
console.table(counts);

// Branches required in audit:
const branchesToAudit = [
  { name: 'Civil', id: 'civil', sections: ['A', 'B'] },
  { name: 'ECE', id: 'ece', sections: ['A', 'B', 'C'] },
  { name: 'Electrical', id: 'ee', sections: ['A', 'B'] },
  { name: 'Mechanical', id: 'me', sections: ['A', 'B'] },
  { name: 'IT', id: 'it', sections: ['A', 'B'] },
  { name: 'Chemical', id: 'chemical', sections: ['A'] },
  { name: 'CSE', id: 'cse', sections: ['A', 'B', 'C', 'D'] },
  { name: 'ECE(IOT)', id: 'eceiot', sections: ['A'] },
  { name: 'BBA', id: 'bba', sections: ['A', 'B'] },
  { name: 'B.Pharm', id: 'bpharm', sections: ['A'] },
];

console.log('\n--- AUDIT OF TARGET BRANCHES & SECTIONS ---');
for (const b of branchesToAudit) {
  for (const sec of b.sections) {
    const key = `${b.id} | Sem 1 | Sec ${sec}`;
    const count = counts[key] || 0;
    const status = count > 0 ? (count >= 30 ? 'LOADS (PARTIAL/FULL)' : 'FEW ENTRIES') : 'EMPTY (0 ENTRIES)';
    console.log(`${b.name.padEnd(12)} | Sem I | Sec ${sec} | Entries: ${count.toString().padEnd(3)} | Status: ${status}`);
  }
}
