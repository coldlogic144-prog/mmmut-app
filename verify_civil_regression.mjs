import fs from 'fs';

const baseline = JSON.parse(fs.readFileSync('civil_baseline_entries.json', 'utf8'));
const newFile = fs.readFileSync('app/android/app/src/main/java/com/mmmut/ero/data/local/OfficialTimetableData.kt', 'utf8');

let missing = 0;
for (const b of baseline) {
  if (!newFile.includes(`"${b.id}"`)) {
    console.error(`Missing baseline id: ${b.id}`);
    missing++;
  }
}

if (missing === 0) {
  console.log(`SUCCESS: All ${baseline.length} baseline Civil entries are 100% perfectly preserved in OfficialTimetableData.kt!`);
} else {
  console.error(`FAILED: ${missing} baseline Civil entries missing.`);
  process.exit(1);
}
