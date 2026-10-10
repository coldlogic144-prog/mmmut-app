package com.mmmut.ero.data.local

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mmmut.ero.data.model.SubjectSyllabus
import com.mmmut.ero.data.model.SyllabusUnit
import com.mmmut.ero.data.model.TopicItem

data class RawUnit(val label: String, val title: String, val topics: List<String>, val hours: Int = 9)

data class RawSubjectDetail(
    val name: String,
    val category: String = "",
    val credits: Int = 4,
    val ltp: String = "",
    val prerequisite: String = "",
    val assessment: String = "",
    val objectives: String = "",
    val courseOutcomes: List<String> = emptyList(),
    val referenceBooks: List<String> = emptyList(),
    val units: List<RawUnit> = emptyList()
)

data class LedgerCourse(
    val code: String,
    val name: String,
    val ltp: String = "",
    val credits: Int = 4,
    val detailKey: String = ""
)

data class LedgerExtraGroup(
    val title: String,
    val subjects: List<LedgerCourse>
)

data class LedgerExtra(
    val id: String,
    val label: String,
    val groups: List<LedgerExtraGroup>
)

data class LedgerBranch(
    val id: String,
    val name: String,
    val semesters: Map<String, List<LedgerCourse>>,
    val extras: List<LedgerExtra>
)

object SyllabusData {
    private var isInitialized = false
    private val branchMap = mutableMapOf<String, LedgerBranch>()
    private val detailMap = mutableMapOf<String, RawSubjectDetail>()

    private val FALLBACK_DETAILS = mapOf(
        "BSM-110" to RawSubjectDetail(
            name = "Engineering Mathematics I",
            category = "Basic Sciences & Maths (BSM)",
            credits = 4,
            ltp = "3-1-0",
            units = listOf(
                RawUnit("Unit I", "Differential Calculus", listOf(
                    "Limit, Continuity and Differentiability",
                    "Mean value theorems & Rolle's Theorem",
                    "Leibnitz theorem for nth derivative",
                    "Partial derivatives & Euler's theorem for homogeneous functions",
                    "Total derivative & Change of variables",
                    "Taylor's and Maclaurin's series for two variables",
                    "Jacobian matrices & Coordinate transformations",
                    "Extrema of functions of several variables & Lagrange multipliers"
                ), 9),
                RawUnit("Unit II", "Linear Algebra", listOf(
                    "Symmetric, Skew-symmetric, Hermitian & Skew-Hermitian matrices",
                    "Orthogonal & Unitary matrices and basic properties",
                    "Linear independence and dependence of vectors",
                    "Rank of a Matrix & Inverse of a Matrix by Elementary transformations",
                    "Consistency of linear system of equations & Solutions",
                    "Characteristic equation, Eigenvalues & Eigenvectors",
                    "Cayley-Hamilton theorem & Diagonalization of matrices"
                ), 9),
                RawUnit("Unit III", "Multiple Integrals", listOf(
                    "Double and triple integrals",
                    "Change of order of integration & Change of variables",
                    "Applications of multiple integrals to surface area and volume",
                    "Beta and Gamma functions & Properties",
                    "Dirichlet integral and applications"
                ), 9),
                RawUnit("Unit IV", "Vector Calculus", listOf(
                    "Gradient, Divergence and Curl",
                    "Directional derivatives & Scalar potential",
                    "Line, surface and volume integrals",
                    "Green's Theorem in a plane",
                    "Stoke's Theorem & Gauss Divergence Theorem"
                ), 9)
            )
        ),
        "BSM-131" to RawSubjectDetail(
            name = "Engineering Physics",
            category = "Basic Sciences & Maths (BSM)",
            credits = 4,
            ltp = "3-0-2",
            units = listOf(
                RawUnit("Unit I", "Optics & Lasers", listOf(
                    "Interference of light & Interference in thin films",
                    "Newton's rings & Wavelength determination",
                    "Fresnel and Fraunhofer class of diffraction",
                    "Diffraction grating & Resolving power",
                    "Double refraction, Nicol prism & Retardation plates",
                    "Production & analysis of plane, circular & elliptically polarized light",
                    "Laser: Spontaneous and stimulated emission",
                    "Population inversion, Ruby laser & He-Ne laser"
                ), 9),
                RawUnit("Unit II", "Quantum Mechanics & Fiber Optics", listOf(
                    "de Broglie hypothesis & Davisson-Germer experiment",
                    "Concept of Phase & Group velocities",
                    "Heisenberg Uncertainty principle & Applications",
                    "Schrodinger time-dependent & time-independent wave equations",
                    "Particle in a 1D infinite potential box",
                    "Fiber Optics: Numerical aperture & Acceptance angle",
                    "Single-mode & Multi-mode step index & graded index fibers"
                ), 9),
                RawUnit("Unit III", "Electrodynamics", listOf(
                    "Scalar & Vector fields, Gradient, Divergence & Curl",
                    "Displacement current & Maxwell's equations in differential/integral forms",
                    "Poynting vector & Electromagnetic wave propagation in free space",
                    "Maxwell's equations in dielectric & conducting media",
                    "Skin depth & Transverse nature of EM waves"
                ), 9),
                RawUnit("Unit IV", "Physics of Advanced Materials", listOf(
                    "Energy bands in solids & Direct/Indirect band gaps",
                    "Carrier concentration in intrinsic & extrinsic semiconductors",
                    "Superconductivity: Meissner effect, Type I & Type II superconductors",
                    "BCS theory (Qualitative) & London equations",
                    "Nanotechnology & Applications of nanomaterials"
                ), 9)
            )
        ),
        "BIT-103" to RawSubjectDetail(
            name = "Programming in C",
            category = "Engineering Fundamentals (EF)",
            credits = 4,
            ltp = "3-0-2",
            units = listOf(
                RawUnit("Unit I", "Basics of Computers & Programming", listOf(
                    "Functional diagram of computer & Language Processors", "Algorithms & Flowcharts",
                    "Data types, Tokens, Identifiers & Keywords", "Variable declaration, Initialization & Enum",
                    "Format specifiers & Standard I/O operations", "Operators: Types, Precedence & Associativity",
                    "Type conversion & Expressions"
                ), 9),
                RawUnit("Unit II", "Conditional & Iterative Statements", listOf(
                    "If, if-else, nested if-else & else-if ladder", "Switch statements & Nested switch",
                    "Iterative statements: for, while & do-while loops", "Nested loops & Pattern printing",
                    "Control statements: break, continue & goto"
                ), 9),
                RawUnit("Unit III", "Arrays, Strings, Pointers & Functions", listOf(
                    "Single-dimensional & Multi-dimensional arrays", "Strings & String handling functions",
                    "Pointers, Pointer arithmetic & Dereferencing", "Dynamic memory allocation (malloc, calloc, free)",
                    "Functions: Prototypes, Actual vs Formal parameters", "Call by value vs Call by reference",
                    "Recursion & Storage classes"
                ), 9),
                RawUnit("Unit IV", "Structures, File Handling & Preprocessor", listOf(
                    "Structures & Unions declaration and initialization", "Array of structures & Nested structures",
                    "File Handling: Text files, Opening, Closing & EOF", "I/O operations on files & Random access",
                    "C Preprocessor directives, Macros & Conditional compilation", "Command line arguments"
                ), 9)
            )
        ),
        "BCE-121" to RawSubjectDetail(
            name = "Engineering Graphics",
            category = "Professional Skill (PS)",
            credits = 4,
            ltp = "2-0-4",
            units = listOf(
                RawUnit("Unit I", "Conic Sections & Orthographic Projections", listOf(
                    "Principles of Engineering Graphics & Drawing instruments",
                    "Conic sections (General method) & Cycloidal curves",
                    "Scales: Plain, Diagonal & Vernier scales",
                    "Orthographic Projections: 1st angle & 3rd angle projection",
                    "Projections of Points & Lines inclined to both planes",
                    "Projections of Planes & Auxiliary plane projections"
                ), 9),
                RawUnit("Unit II", "Projections & Sections of Regular Solids", listOf(
                    "Projections of Prisms, Pyramids, Cylinders & Cones",
                    "Sections & Sectional views of Right Regular Solids",
                    "Development of Lateral Surfaces of Prisms, Pyramids & Cones"
                ), 9),
                RawUnit("Unit III", "Isometric Projections", listOf(
                    "Principles of Isometric Projection & Isometric Scale",
                    "Isometric Views of Lines, Planes & Simple/Compound Solids",
                    "Conversion of Isometric to Orthographic Views & Vice-versa"
                ), 9),
                RawUnit("Unit IV", "Computer Aided Drafting (CAD) Overview", listOf(
                    "Theory of CAD software, Menus, Toolbars & Viewports",
                    "2D Drafting, Object Snaps & Layering in CAD",
                    "3D Wireframe & Solid Modeling concepts"
                ), 9)
            )
        ),
        "BHS-101" to RawSubjectDetail(
            name = "Universal Human Values",
            category = "Humanities & Social Sciences (HSS)",
            credits = 4,
            ltp = "3-1-0",
            units = listOf(
                RawUnit("Unit I", "Introduction to Value Education", listOf(
                    "Origin, Definition, Meaning & Types of Values",
                    "Value Education vs Professional Ethics",
                    "Self-Exploration: Content & Process",
                    "Natural Acceptance & Experiential Validation",
                    "Continuous Happiness & Prosperity as Basic Human Aspirations",
                    "Right Understanding, Relationship & Physical Facilities"
                ), 9),
                RawUnit("Unit II", "Harmony in Myself & Body", listOf(
                    "Human Being as Co-existence of Self ('I') & Body",
                    "Needs of Self ('I') vs Needs of Body",
                    "Body as Instrument of 'I'",
                    "Characteristics & Activities of 'I'",
                    "Harmony of 'I' with Body: Sanyam and Health"
                ), 9),
                RawUnit("Unit III", "Harmony in Family & Society", listOf(
                    "Values in Family Relationships: Trust and Respect",
                    "Justice & Mutual Happiness in Relationships",
                    "Trust (Intention vs Competence) & Respect",
                    "Comprehensive Human Goals in Society",
                    "Undivided Society & Universal Order"
                ), 9),
                RawUnit("Unit IV", "Harmony in Nature & Professional Ethics", listOf(
                    "Interconnectedness & Mutual Fulfillment in 4 Orders of Nature",
                    "Existence as Co-existence in All-pervasive Space",
                    "Definitiveness of Ethical Human Conduct",
                    "Competence in Professional Ethics & Humanistic Constitution"
                ), 9)
            )
        )
    )

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        ensureLoaded(appContext)
    }

    private fun ensureLoaded(context: Context? = null) {
        if (isInitialized && branchMap.isNotEmpty()) return
        synchronized(this) {
            if (isInitialized && branchMap.isNotEmpty()) return
            try {
                val jsonString = loadJsonText(context)
                if (!jsonString.isNullOrBlank()) {
                    val root = JsonParser.parseString(jsonString).asJsonObject
                    if (root.has("details") && root.get("details").isJsonObject) {
                        val detailsObj = root.getAsJsonObject("details")
                        for ((key, elem) in detailsObj.entrySet()) {
                            if (elem.isJsonObject) {
                                val d = parseDetail(elem.asJsonObject)
                                detailMap[key] = d
                                detailMap[key.replace(" ", "-")] = d
                                detailMap[key.replace("/", "-")] = d
                            }
                        }
                    }
                    if (root.has("branches") && root.get("branches").isJsonObject) {
                        val branchesObj = root.getAsJsonObject("branches")
                        for ((bId, bElem) in branchesObj.entrySet()) {
                            if (bElem.isJsonObject) {
                                branchMap[bId.lowercase()] = parseBranch(bId.lowercase(), bElem.asJsonObject)
                            }
                        }
                    }
                }
            } catch (_: Exception) { }

            // Safe fallback so branchMap is NEVER empty
            if (branchMap.isEmpty()) {
                val fallbackCourses = FALLBACK_DETAILS.map { (code, d) ->
                    LedgerCourse(code = code, name = d.name, ltp = d.ltp, credits = d.credits, detailKey = code)
                }
                val branchesList = listOf(
                    "civil" to "Civil Engineering",
                    "cse" to "Computer Science & Engineering",
                    "it" to "Information Technology",
                    "ece" to "Electronics & Communication Engineering",
                    "eceiot" to "ECE (IoT)",
                    "ee" to "Electrical Engineering",
                    "me" to "Mechanical Engineering",
                    "chemical" to "Chemical Engineering",
                    "bba" to "BBA",
                    "bpharm" to "B.Pharm"
                )
                for ((bId, bName) in branchesList) {
                    branchMap[bId] = LedgerBranch(bId, bName, (1..8).associate { it.toString() to fallbackCourses }, emptyList())
                }
            }
            isInitialized = true
        }
    }

    private fun loadJsonText(context: Context?): String? {
        val ctx = context ?: appContext ?: runCatching { com.mmmut.ero.MmmutApp.instance }.getOrNull()
        if (ctx != null) {
            try {
                return ctx.assets.open("ledger_syllabus.json").bufferedReader().use { it.readText() }
            } catch (_: Exception) {}
        }
        try {
            val stream = SyllabusData::class.java.classLoader?.getResourceAsStream("assets/ledger_syllabus.json")
                ?: SyllabusData::class.java.classLoader?.getResourceAsStream("ledger_syllabus.json")
                ?: SyllabusData::class.java.getResourceAsStream("/ledger_syllabus.json")
                ?: SyllabusData::class.java.getResourceAsStream("/assets/ledger_syllabus.json")
            if (stream != null) {
                return stream.bufferedReader().use { it.readText() }
            }
        } catch (_: Exception) {}
        try {
            val file = java.io.File("src/main/assets/ledger_syllabus.json")
            if (file.exists()) return file.readText()
        } catch (_: Exception) {}
        try {
            val file = java.io.File("app/src/main/assets/ledger_syllabus.json")
            if (file.exists()) return file.readText()
        } catch (_: Exception) {}
        return null
    }

    private fun parseDetail(obj: JsonObject): RawSubjectDetail {
        val name = obj.get("n")?.asString ?: ""
        val category = obj.get("c")?.asString ?: ""
        val prerequisite = obj.get("p")?.asString ?: ""
        val ltp = obj.get("k")?.asString ?: ""
        val credits = obj.get("cr")?.asInt ?: 4
        val assessment = obj.get("a")?.asString ?: ""
        val objectives = obj.get("o")?.asString ?: ""
        val co = obj.getAsJsonArray("co")?.map { it.asString } ?: emptyList()
        val books = obj.getAsJsonArray("b")?.map { it.asString } ?: emptyList()
        val units = mutableListOf<RawUnit>()
        val unitsArr = obj.getAsJsonArray("u")
        if (unitsArr != null) {
            for (uItem in unitsArr) {
                if (uItem.isJsonObject) {
                    val uObj = uItem.asJsonObject
                    val label = uObj.get("l")?.asString ?: ""
                    val title = uObj.get("t")?.asString ?: ""
                    val hours = uObj.get("h")?.asInt ?: 9
                    val topics = uObj.getAsJsonArray("p")?.map { it.asString } ?: emptyList()
                    val normLabel = if (label.startsWith("Unit", ignoreCase = true)) label else "Unit $label"
                    units.add(RawUnit(normLabel, title, topics, hours))
                }
            }
        }
        return RawSubjectDetail(
            name = name,
            category = category,
            credits = credits,
            ltp = ltp,
            prerequisite = prerequisite,
            assessment = assessment,
            objectives = objectives,
            courseOutcomes = co,
            referenceBooks = books,
            units = units
        )
    }

    private fun parseBranch(branchId: String, obj: JsonObject): LedgerBranch {
        val name = obj.get("name")?.asString ?: branchId.uppercase()
        val semMap = mutableMapOf<String, List<LedgerCourse>>()
        val semObj = obj.getAsJsonObject("semesters")
        if (semObj != null) {
            for ((semNum, semArrElem) in semObj.entrySet()) {
                if (semArrElem.isJsonArray) {
                    val courseList = mutableListOf<LedgerCourse>()
                    for (cElem in semArrElem.asJsonArray) {
                        if (cElem.isJsonObject) {
                            val cObj = cElem.asJsonObject
                            courseList.add(
                                LedgerCourse(
                                    code = cObj.get("code")?.asString ?: "",
                                    name = cObj.get("name")?.asString ?: "",
                                    ltp = cObj.get("ltp")?.asString ?: "",
                                    credits = cObj.get("credits")?.asInt ?: 4,
                                    detailKey = cObj.get("d")?.asString ?: ""
                                )
                            )
                        }
                    }
                    semMap[semNum] = courseList
                }
            }
        }
        val extrasList = mutableListOf<LedgerExtra>()
        val extrasArr = obj.getAsJsonArray("extras")
        if (extrasArr != null) {
            for (exElem in extrasArr) {
                if (exElem.isJsonObject) {
                    val exObj = exElem.asJsonObject
                    val id = exObj.get("id")?.asString ?: ""
                    val label = exObj.get("label")?.asString ?: ""
                    val groups = mutableListOf<LedgerExtraGroup>()
                    val groupsArr = exObj.getAsJsonArray("groups")
                    if (groupsArr != null) {
                        for (gElem in groupsArr) {
                            if (gElem.isJsonObject) {
                                val gObj = gElem.asJsonObject
                                val gTitle = gObj.get("title")?.asString ?: ""
                                val gSubjects = mutableListOf<LedgerCourse>()
                                val sArr = gObj.getAsJsonArray("subjects")
                                if (sArr != null) {
                                    for (sElem in sArr) {
                                        if (sElem.isJsonObject) {
                                            val sObj = sElem.asJsonObject
                                            gSubjects.add(
                                                LedgerCourse(
                                                    code = sObj.get("code")?.asString ?: "",
                                                    name = sObj.get("name")?.asString ?: "",
                                                    ltp = sObj.get("ltp")?.asString ?: "",
                                                    credits = sObj.get("credits")?.asInt ?: 4,
                                                    detailKey = sObj.get("d")?.asString ?: ""
                                                )
                                            )
                                        }
                                    }
                                }
                                groups.add(LedgerExtraGroup(gTitle, gSubjects))
                            }
                        }
                    }
                    extrasList.add(LedgerExtra(id, label, groups))
                }
            }
        }
        return LedgerBranch(branchId, name, semMap, extrasList)
    }

    fun getAllBranches(): List<Pair<String, String>> {
        ensureLoaded()
        if (branchMap.isEmpty()) {
            return listOf(
                "civil" to "Civil Engineering",
                "cse" to "Computer Science & Engineering",
                "it" to "Information Technology",
                "ece" to "Electronics & Communication Engineering",
                "eceiot" to "ECE (IoT)",
                "ee" to "Electrical Engineering",
                "me" to "Mechanical Engineering",
                "chemical" to "Chemical Engineering",
                "bba" to "BBA",
                "bpharm" to "B.Pharm"
            )
        }
        return branchMap.values.map { it.id to it.name }
    }

    fun getBranch(branchId: String): LedgerBranch? {
        ensureLoaded()
        val canonical = ScheduleEngine.canonicalBranchId(branchId).ifBlank { "civil" }
        return branchMap[canonical] ?: branchMap[branchId.lowercase()] ?: branchMap["civil"] ?: branchMap.values.firstOrNull()
    }

    fun getAvailableViewsForBranch(branchId: String): List<Pair<String, String>> {
        ensureLoaded()
        val branch = getBranch(branchId) ?: return (1..8).map { it.toString() to "Sem $it" }
        val views = mutableListOf<Pair<String, String>>()
        val sortedSemesters = branch.semesters.keys.sortedBy { it.toIntOrNull() ?: 99 }
        for (s in sortedSemesters) {
            views.add(s to "Sem $s")
        }
        for (ex in branch.extras) {
            views.add(ex.id to ex.label)
        }
        return views
    }

    fun getCoursesForView(branchId: String, view: String): List<LedgerCourse> {
        ensureLoaded()
        val branch = getBranch(branchId) ?: return emptyList()
        branch.semesters[view]?.let { return it }
        val extra = branch.extras.firstOrNull { it.id == view }
        if (extra != null) {
            return extra.groups.flatMap { it.subjects }
        }
        return branch.semesters.values.firstOrNull() ?: emptyList()
    }

    fun getAllCoursesForBranch(branchId: String): List<LedgerCourse> {
        ensureLoaded()
        val branch = getBranch(branchId) ?: return emptyList()
        val list = mutableListOf<LedgerCourse>()
        branch.semesters.values.forEach { list.addAll(it) }
        branch.extras.forEach { ex -> ex.groups.forEach { list.addAll(it.subjects) } }
        return list.distinctBy { it.code }
    }

    fun getSyllabusForCourse(
        branch: String,
        course: LedgerCourse,
        completedKeys: Set<String>
    ): SubjectSyllabus {
        return getSyllabusForSubject(
            branch = branch,
            code = course.code,
            name = course.name,
            completedKeys = completedKeys,
            explicitDetailKey = course.detailKey,
            ltp = course.ltp,
            credits = course.credits
        )
    }

    fun getSyllabusForSubject(
        branch: String,
        code: String,
        name: String,
        completedKeys: Set<String>,
        explicitDetailKey: String = "",
        ltp: String = "",
        credits: Int = 4
    ): SubjectSyllabus {
        ensureLoaded()
        val dKey = if (explicitDetailKey.isNotBlank()) explicitDetailKey else code
        val detail = findDetail(dKey, code, name)

        val detailIdentifier = if (explicitDetailKey.isNotBlank()) explicitDetailKey else code
        val units = detail.units.mapIndexed { uIdx, u ->
            val topics = u.topics.mapIndexed { tIdx, tName ->
                val key = "t|$branch|$detailIdentifier|$uIdx|$tIdx"
                val altKey = "t|$branch|$code|$uIdx|$tIdx"
                val isDone = completedKeys.contains(key) || completedKeys.contains(altKey)
                TopicItem(key = key, name = tName, completed = isDone)
            }
            SyllabusUnit(unitNumber = u.label, unitTitle = u.title, hours = u.hours, topics = topics)
        }

        return SubjectSyllabus(
            subjectCode = code,
            subjectName = if (detail.name.isNotBlank()) detail.name else name,
            detailKey = detailIdentifier,
            category = if (detail.category.isNotBlank()) detail.category else "Core Engineering",
            credits = if (detail.credits > 0) detail.credits else credits,
            ltp = if (detail.ltp.isNotBlank()) detail.ltp else ltp,
            units = units,
            prerequisite = detail.prerequisite,
            assessment = detail.assessment,
            objectives = detail.objectives,
            courseOutcomes = detail.courseOutcomes,
            referenceBooks = detail.referenceBooks
        )
    }

    private fun findDetail(detailKey: String, code: String, name: String): RawSubjectDetail {
        if (detailKey.isNotBlank()) {
            detailMap[detailKey]?.let { return it }
            detailMap[detailKey.replace("-", " ")]?.let { return it }
        }
        detailMap[code]?.let { return it }
        detailMap[code.replace("-", " ")]?.let { return it }
        FALLBACK_DETAILS[code.uppercase()]?.let { return it }
        FALLBACK_DETAILS[code]?.let { return it }
        detailMap.values.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }
        FALLBACK_DETAILS.values.firstOrNull { it.name.equals(name, ignoreCase = true) }?.let { return it }

        return RawSubjectDetail(
            name = name,
            category = "Core Engineering",
            credits = 4,
            ltp = "3-1-0",
            units = listOf(
                RawUnit("Unit I", "Module 1: Fundamentals", listOf("Core Concepts", "Definitions & Theory", "Basic Analysis"), 9),
                RawUnit("Unit II", "Module 2: Advanced Topics", listOf("Methodology", "Mathematical Formulation", "Applications"), 9),
                RawUnit("Unit III", "Module 3: Design & Practice", listOf("Design Principles", "Case Studies", "Laboratory Methods"), 9),
                RawUnit("Unit IV", "Module 4: Standards & Ethics", listOf("Codal Provisions", "Future Trends", "Professional Ethics"), 9)
            )
        )
    }
}
