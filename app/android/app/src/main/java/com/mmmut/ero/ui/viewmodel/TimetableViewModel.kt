package com.mmmut.ero.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmmut.ero.core.UiState
import com.mmmut.ero.data.local.AcademicData
import com.mmmut.ero.data.local.AcademicDataExtra
import com.mmmut.ero.data.local.ScheduleEngine
import com.mmmut.ero.data.model.StudentProfile
import com.mmmut.ero.data.model.TimetableCell
import com.mmmut.ero.data.model.TimetableEntry
import com.mmmut.ero.data.repository.AuthRepository
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.data.repository.FirebaseProfileRepository
import com.mmmut.ero.data.repository.LocalTimetableRepository
import com.mmmut.ero.data.repository.TimetableRepository
import com.mmmut.ero.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ==========================================================================
 * Timetable — state holder
 * --------------------------------------------------------------------------
 * The schedule ENGINE is untouched. This class only:
 *   1. asks ScheduleEngine for the week of the requested selection,
 *   2. derives the filter option lists from that same data,
 *   3. exposes friendly, pre-formatted state for the screen.
 *
 * Everything the screen needs is computed here so the composables stay cheap:
 * no engine call, no time parsing and no filtering happens during composition.
 * ========================================================================== */

/**
 * What the student is currently looking at.
 *
 * Defaults always come from the logged-in profile, so the first thing a
 * student sees is strictly their own applicable classes. If they pick a
 * different chip, or the stored group value has drifted to a code that no
 * longer exists in the data, the correction is applied automatically instead
 * of leaving them on a permanently empty screen.
 *
 * Group fields are uppercased and normalised to `N/A` — never blank — because
 * that is exactly what ScheduleEngine expects.
 */
@Immutable
data class TimetableFilter(
    val branchId: String,
    val semester: Int,
    val section: String,
    val tutorialGroup: String,
    val practicalGroup: String
)

@Immutable
data class TimetableOption(val value: String, val label: String)

@Immutable
data class TimetableScreenData(
    val profile: StudentProfile,
    /** Branch actually being displayed (may differ from the profile after a chip change). */
    val branchName: String,
    /** Default room of the displayed branch, used when a cell has no room of its own. */
    val room: String,
    /** Every day of the academic week, always Monday..Saturday (incl. genuinely free days). */
    val weekGrid: Map<String, List<TimetableCell>>,
    val todayName: String,
    val todayCells: List<TimetableCell>,
    val currentCell: TimetableCell?,
    val nextCell: TimetableCell?,
    val filter: TimetableFilter,
    /** True only while the selection is the student's own branch/semester/section. */
    val isOwnTimetable: Boolean,
    val branchOptions: List<TimetableOption>,
    val semesterOptions: List<TimetableOption>,
    val sectionOptions: List<TimetableOption>,
    val tutorialOptions: List<TimetableOption>,
    val practicalOptions: List<TimetableOption>
)

class TimetableViewModel(
    private val auth: AuthRepository = FirebaseAuthRepository(),
    private val ttRepo: TimetableRepository = LocalTimetableRepository()
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<TimetableScreenData>>(UiState.Loading)
    val state: StateFlow<UiState<TimetableScreenData>> = _state

    private val profileRepo = FirebaseProfileRepository()

    /** Chip selections made on top of the stored profile. Null = follow the profile. */
    private var override: TimetableFilter? = null

    private var _lastError: Throwable? = null

    /** Branch id -> display name, built once instead of per recomposition. */
    private val branchNames: Map<String, String> by lazy {
        AcademicDataExtra.ALL_BRANCHES.associate { it.id to it.name }
    }

    /** "branchId|semester" -> sections that actually carry timetable data. */
    private val sectionCache = mutableMapOf<String, List<String>>()

    fun load() {
        viewModelScope.launch {
            _lastError = null
            _state.value = UiState.Loading
            try {
                val profile = try {
                    auth.currentProfile()
                } catch (_: Exception) {
                    null
                }
                if (profile == null) {
                    _state.value = UiState.Error("Session expired. Please log in again.")
                    return@launch
                }
                _state.value = UiState.Success(build(profile))
            } catch (e: Exception) {
                // The technical cause is handed to the screen so it can run it
                // through `friendlyError`; it is never shown as-is.
                _lastError = e
                _state.value = UiState.Error("We couldn't build your timetable. Please try again.")
            }
        }
    }

    /**
     * The throwable behind the most recent failure, if any. The screen runs it
     * through `friendlyError` (shared component) before showing it, so a raw
     * exception message can never reach the student.
     */
    fun lastError(): Throwable? = _lastError

    /**
     * Re-runs only the current/next class lookup against the schedule already on
     * screen. Used by the 30-second ticker: a full [load] would drop the screen
     * back to `Loading` and flash a skeleton list every half minute.
     */
    fun refreshNow() {
        val data = (_state.value as? UiState.Success)?.data ?: return
        val todayCells = data.todayCells
        val now = nowTime()
        val current = try {
            ScheduleEngine.getCurrentClass(todayCells, now)
        } catch (_: Exception) {
            null
        }
        val next = try {
            ScheduleEngine.getNextClass(todayCells, now)
        } catch (_: Exception) {
            null
        }
        if (current == data.currentCell && next == data.nextCell) return
        _state.value = UiState.Success(data.copy(currentCell = current, nextCell = next))
    }

    /* ------------------------------------------------------------ filtering -- */

    /**
     * Records an explicit chip selection and reloads. Group changes are also
     * written back to the profile so the student's own groups stay in sync with
     * what they are looking at; branch/semester/section are view-only and never
     * overwrite the stored profile.
     */
    fun setFilter(filter: TimetableFilter) {
        val current = (_state.value as? UiState.Success)?.data ?: return
        val sectionChanged = canonicalSection(filter.section) != current.filter.section
        val normalized = normalize(filter, current.profile, keepSection = sectionChanged)
        override = normalized
        persistGroupsIfChanged(normalized, current.profile)
        load()
    }

    /** Back to the student's own branch / semester / section / groups. */
    fun resetFilter() {
        override = null
        load()
    }

    fun updateGroups(tutorialGroup: String, practicalGroup: String) {
        val cur = (_state.value as? UiState.Success)?.data ?: return
        val normalized = normalize(
            cur.filter.copy(
                tutorialGroup = tutorialGroup,
                practicalGroup = practicalGroup
            ),
            cur.profile
        )
        override = normalized
        persistGroupsIfChanged(normalized, cur.profile)
        load()
    }

    private fun persistGroupsIfChanged(filter: TimetableFilter, profile: StudentProfile) {
        val tut = filter.tutorialGroup
        val prac = filter.practicalGroup
        val changed = !tut.equals(profile.tutorialGroup.trim().uppercase(), ignoreCase = true) ||
            !prac.equals(profile.practicalGroup.trim().uppercase(), ignoreCase = true)
        if (!changed) return
        // Persisted before the reload so the write and the re-read cannot race:
        // reloading first would read the old groups back and undo the selection.
        viewModelScope.launch {
            try {
                profileRepo.updateProfile(
                    profile.uid,
                    mapOf("tutorialGroup" to tut, "practicalGroup" to prac)
                )
            } catch (_: Exception) {
                // Presentation-only failure: the selection still applies on screen.
            }
        }
    }

    /* -------------------------------------------------------------- assembling */

    private fun build(profile: StudentProfile): TimetableScreenData {
        val query = normalize(override ?: fromProfile(profile), profile)

        // Scan the full timetable data once so the option lists can never offer
        // a group code that does not exist for this branch/semester/section.
        val allForQuery = try {
            ScheduleEngine.getTimetableForStudent(
                branchId = query.branchId,
                semester = query.semester,
                section = query.section,
                tutorialGroup = "N/A",
                practicalGroup = "N/A"
            )
        } catch (_: Exception) {
            emptyList()
        }

        val week = try {
            ScheduleEngine.getWeek(
                branchId = query.branchId,
                semester = query.semester,
                section = query.section,
                tutorialGroup = query.tutorialGroup,
                practicalGroup = query.practicalGroup
            )
        } catch (_: Exception) {
            emptyMap()
        }

        val todayName = TimeUtils.todayName() ?: "Monday"
        val todayCells = week[todayName] ?: emptyList()

        val nowTime = nowTime()
        val currentCell = try {
            ScheduleEngine.getCurrentClass(todayCells, nowTime)
        } catch (_: Exception) {
            null
        }
        val nextCell = try {
            ScheduleEngine.getNextClass(todayCells, nowTime)
        } catch (_: Exception) {
            null
        }

        val normalizedWeek = AcademicData.DAYS.associateWith { day -> week[day] ?: emptyList() }
        val availableSections = sectionIdsFor(query.branchId, query.semester)

        return TimetableScreenData(
            profile = profile,
            branchName = branchNameOf(query.branchId),
            room = AcademicDataExtra.getBranch(query.branchId).room,
            weekGrid = normalizedWeek,
            todayName = todayName,
            todayCells = todayCells,
            currentCell = currentCell,
            nextCell = nextCell,
            filter = query,
            isOwnTimetable = query.branchId == canonical(profile.branchId) &&
                query.semester == profile.semester &&
                query.section == canonicalSection(profile.section),
            branchOptions = branchOptions(),
            semesterOptions = semesterOptions(),
            sectionOptions = availableSections.map { TimetableOption(it, "Section $it") },
            tutorialOptions = groupOptions("T1", allForQuery),
            practicalOptions = groupOptions("P1", allForQuery)
        )
    }

    /* ---------------------------------------------------------- normalisation */

    private fun fromProfile(profile: StudentProfile) = TimetableFilter(
        branchId = canonical(profile.branchId),
        semester = profile.semester,
        section = canonicalSection(profile.section),
        tutorialGroup = groupOf(profile.tutorialGroup, "T1"),
        practicalGroup = groupOf(profile.practicalGroup, "P1")
    )

    /**
     * Keeps a selection coherent: an unknown branch falls back to the profile's,
     * and a section survives only while it actually exists for that branch and
     * semester. When the student changes branch, the previous section becomes
     * meaningless, so the profile's own section is restored unless they picked a
     * section in the same interaction.
     */
    private fun normalize(
        filter: TimetableFilter,
        profile: StudentProfile,
        keepSection: Boolean = false
    ): TimetableFilter {
        val branchId = canonical(filter.branchId).takeIf { branchNames.containsKey(it) }
            ?: canonical(profile.branchId)
        val semester = filter.semester.takeIf { it in SEMESTERS } ?: profile.semester
        val available = sectionIdsFor(branchId, semester)
        val profileSection = canonicalSection(profile.section)
        val section = when {
            keepSection -> canonicalSection(filter.section).takeIf { it in available }
            profileSection in available -> profileSection
            else -> null
        } ?: available.firstOrNull() ?: "A"
        return TimetableFilter(
            branchId = branchId,
            semester = semester,
            section = section,
            tutorialGroup = groupOf(filter.tutorialGroup, "N/A"),
            practicalGroup = groupOf(filter.practicalGroup, "N/A")
        )
    }

    private fun branchNameOf(branchId: String): String =
        branchNames[branchId] ?: AcademicDataExtra.getBranch(branchId).name

    /**
     * Group codes that actually occur for this branch/semester/section, plus
     * "N/A" for the classes that apply to every student. Read-only: it never
     * alters what the engine returns.
     *
     * The stored and currently selected codes are deliberately *not* injected
     * into this list. If they are missing, the chip renders them as unavailable
     * and the student can pick one that exists instead of staring at an empty
     * week they have no way to explain.
     */
    private fun groupOptions(
        fallback: String,
        entries: List<TimetableEntry>
    ): List<TimetableOption> {
        val isTutorial = fallback.startsWith("T")
        val found = entries.asSequence()
            .filter { entry ->
                if (isTutorial) entry.type.equals("TUTORIAL", ignoreCase = true)
                else entry.type.equals("PRACTICAL", ignoreCase = true)
            }
            .mapNotNull { if (isTutorial) it.tutorialGroup else it.practicalGroup }
            .map { it.trim().uppercase() }
            .filter { it.isNotEmpty() && it != "N/A" }
            .distinct()
            .sortedWith(compareBy({ codeRank(it) }, { it }))
            .toList()

        val values = LinkedHashSet<String>()
        values += found
        values += "N/A"

        return values.map { code ->
            if (code == "N/A") TimetableOption("N/A", "Not applicable")
            else TimetableOption(code, code)
        }
    }

    /** "T1" / "P2"; anything unusable falls back rather than producing a blank filter. */
    private fun groupOf(raw: String, fallback: String): String {
        val clean = raw.trim().uppercase()
        return if (clean.isEmpty()) fallback else clean
    }

    private fun codeRank(code: String): Int =
        code.drop(1).toIntOrNull()?.takeIf { it > 0 } ?: Int.MAX_VALUE

    private fun branchOptions(): List<TimetableOption> =
        AcademicDataExtra.ALL_BRANCHES.map { TimetableOption(it.id, it.name) }

    private fun semesterOptions(): List<TimetableOption> =
        SEMESTERS.map { TimetableOption(it.toString(), "Semester $it") }

    /**
     * Sections that genuinely have timetable data for this branch/semester.
     * The declared `Branch.sections` list is used when the data has no opinion,
     * but a section is never offered when it would lead to an empty screen.
     *
     * Memoised because a single load asks more than once (once to normalise the
     * selection, once to build the chip options) and each probe scans the whole
     * timetable dataset.
     */
    private fun sectionIdsFor(branchId: String, semester: Int): List<String> {
        val cacheKey = "$branchId|$semester"
        sectionCache[cacheKey]?.let { return it }

        val declared = try {
            AcademicDataExtra.getBranch(branchId).sections
        } catch (_: Exception) {
            emptyList()
        }
        val candidates = (declared + SECTION_PROBES)
            .map { canonicalSection(it) }
            .filter { it.isNotBlank() }
            .distinct()
        val withData = candidates.filter { section ->
            AcademicData.DAYS.any { day ->
                try {
                    ScheduleEngine.getWeek(
                        branchId = branchId,
                        semester = semester,
                        section = section,
                        tutorialGroup = "N/A",
                        practicalGroup = "N/A"
                    )[day].orEmpty().isNotEmpty()
                } catch (_: Exception) {
                    false
                }
            }
        }
        val resolved = withData.ifEmpty { declared.ifEmpty { listOf("A") } }
        sectionCache[cacheKey] = resolved
        return resolved
    }

    /* --------------------------------------------------------------- helpers -- */

    private fun canonical(raw: String): String = try {
        ScheduleEngine.canonicalBranchId(raw)
    } catch (_: Exception) {
        raw.trim().lowercase()
    }

    private fun canonicalSection(raw: String): String = try {
        ScheduleEngine.canonicalSection(raw)
    } catch (_: Exception) {
        raw.trim().uppercase()
    }

    /** "HH:mm" in the device timezone; the same format the schedule data uses. */
    private fun nowTime(): String = try {
        SimpleDateFormat("HH:mm", Locale.US).format(Date())
    } catch (_: Exception) {
        "00:00"
    }

    private companion object {
        val SEMESTERS = listOf(1, 2, 3, 4, 5, 6, 7, 8)

        /** Probe order for section letters the branch definition may not list. */
        val SECTION_PROBES = listOf("A", "B", "C", "D", "E")
    }
}
