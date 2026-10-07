import fs from 'fs';

// Read existing OfficialTimetableData.kt and extract Civil entries
const content = fs.readFileSync('app/android/app/src/main/java/com/mmmut/ero/data/local/OfficialTimetableData.kt', 'utf8');

const entryRegex = /TimetableEntry\(([^)]+)\)/g;
let match;
const civilEntries = [];

while ((match = entryRegex.exec(content)) !== null) {
  const rawArgs = match[1];
  // Match params
  const parts = rawArgs.split(',').map(s => s.trim().replace(/^"|"$/g, ''));
  if (parts[2]?.toLowerCase() === 'civil') {
    civilEntries.push({
      id: parts[0],
      session: parts[1],
      branch: parts[2],
      section: parts[3],
      semester: parseInt(parts[4]),
      day: parts[5],
      startPeriod: parts[6],
      endPeriod: parts[7],
      startTime: parts[8],
      endTime: parts[9],
      subjectCode: parts[10],
      subjectName: parts[11],
      type: parts[12],
      tutorialGroup: parts[13] === 'null' ? null : parts[13],
      practicalGroup: parts[14] === 'null' ? null : parts[14],
      room: parts[15],
      teacher: parts[16] || ''
    });
  }
}

fs.writeFileSync('civil_baseline_entries.json', JSON.stringify(civilEntries, null, 2));
console.log(`Saved ${civilEntries.length} Civil baseline entries.`);
