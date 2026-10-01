package com.mmmut.ero.data.local

import com.mmmut.ero.data.model.SubjectSyllabus
import com.mmmut.ero.data.model.SyllabusUnit
import com.mmmut.ero.data.model.TopicItem

data class RawUnit(val label: String, val title: String, val topics: List<String>)
data class RawSubjectDetail(val name: String, val category: String, val credits: Int, val ltp: String, val units: List<RawUnit>)

object SyllabusData {
    private val DETAILS = mapOf(
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
                )),
                RawUnit("Unit II", "Linear Algebra", listOf(
                    "Symmetric, Skew-symmetric, Hermitian & Skew-Hermitian matrices",
                    "Orthogonal & Unitary matrices and basic properties",
                    "Linear independence and dependence of vectors",
                    "Rank of a Matrix & Inverse of a Matrix by Elementary transformations",
                    "Consistency of linear system of equations & Solutions",
                    "Characteristic equation, Eigenvalues & Eigenvectors",
                    "Cayley-Hamilton theorem & Diagonalization of matrices"
                )),
                RawUnit("Unit III", "Multiple Integrals", listOf(
                    "Double and triple integrals",
                    "Change of order of integration & Change of variables",
                    "Applications of multiple integrals to surface area and volume",
                    "Beta and Gamma functions & Properties",
                    "Dirichlet integral and applications"
                )),
                RawUnit("Unit IV", "Vector Calculus", listOf(
                    "Gradient, Divergence and Curl",
                    "Directional derivatives & Scalar potential",
                    "Line, surface and volume integrals",
                    "Green's Theorem in a plane",
                    "Stoke's Theorem & Gauss Divergence Theorem"
                ))
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
                )),
                RawUnit("Unit II", "Quantum Mechanics & Fiber Optics", listOf(
                    "de Broglie hypothesis & Davisson-Germer experiment",
                    "Concept of Phase & Group velocities",
                    "Heisenberg Uncertainty principle & Applications",
                    "Schrodinger time-dependent & time-independent wave equations",
                    "Particle in a 1D infinite potential box",
                    "Fiber Optics: Numerical aperture & Acceptance angle",
                    "Single-mode & Multi-mode step index & graded index fibers"
                )),
                RawUnit("Unit III", "Electrodynamics", listOf(
                    "Scalar & Vector fields, Gradient, Divergence & Curl",
                    "Displacement current & Maxwell's equations in differential/integral forms",
                    "Poynting vector & Electromagnetic wave propagation in free space",
                    "Maxwell's equations in dielectric & conducting media",
                    "Skin depth & Transverse nature of EM waves"
                )),
                RawUnit("Unit IV", "Physics of Advanced Materials", listOf(
                    "Energy bands in solids & Direct/Indirect band gaps",
                    "Carrier concentration in intrinsic & extrinsic semiconductors",
                    "Superconductivity: Meissner effect, Type I & Type II superconductors",
                    "BCS theory (Qualitative) & London equations",
                    "Nanotechnology & Applications of nanomaterials"
                ))
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
                )),
                RawUnit("Unit II", "Conditional & Iterative Statements", listOf(
                    "If, if-else, nested if-else & else-if ladder", "Switch statements & Nested switch",
                    "Iterative statements: for, while & do-while loops", "Nested loops & Pattern printing",
                    "Control statements: break, continue & goto"
                )),
                RawUnit("Unit III", "Arrays, Strings, Pointers & Functions", listOf(
                    "Single-dimensional & Multi-dimensional arrays", "Strings & String handling functions",
                    "Pointers, Pointer arithmetic & Dereferencing", "Dynamic memory allocation (malloc, calloc, free)",
                    "Functions: Prototypes, Actual vs Formal parameters", "Call by value vs Call by reference",
                    "Recursion & Storage classes"
                )),
                RawUnit("Unit IV", "Structures, File Handling & Preprocessor", listOf(
                    "Structures & Unions declaration and initialization", "Array of structures & Nested structures",
                    "File Handling: Text files, Opening, Closing & EOF", "I/O operations on files & Random access",
                    "C Preprocessor directives, Macros & Conditional compilation", "Command line arguments"
                ))
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
                )),
                RawUnit("Unit II", "Projections & Sections of Regular Solids", listOf(
                    "Projections of Prisms, Pyramids, Cylinders & Cones",
                    "Sections & Sectional views of Right Regular Solids",
                    "Development of Lateral Surfaces of Prisms, Pyramids & Cones"
                )),
                RawUnit("Unit III", "Isometric Projections", listOf(
                    "Principles of Isometric Projection & Isometric Scale",
                    "Isometric Views of Lines, Planes & Simple/Compound Solids",
                    "Conversion of Isometric to Orthographic Views & Vice-versa"
                )),
                RawUnit("Unit IV", "Computer Aided Drafting (CAD) Overview", listOf(
                    "Theory of CAD software, Menus, Toolbars & Viewports",
                    "2D Drafting, Object Snaps & Layering in CAD",
                    "3D Wireframe & Solid Modeling concepts"
                ))
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
                )),
                RawUnit("Unit II", "Harmony in Myself & Body", listOf(
                    "Human Being as Co-existence of Self ('I') & Body",
                    "Needs of Self ('I') vs Needs of Body",
                    "Body as Instrument of 'I'",
                    "Characteristics & Activities of 'I'",
                    "Harmony of 'I' with Body: Sanyam and Health"
                )),
                RawUnit("Unit III", "Harmony in Family & Society", listOf(
                    "Values in Family Relationships: Trust and Respect",
                    "Justice & Mutual Happiness in Relationships",
                    "Trust (Intention vs Competence) & Respect",
                    "Comprehensive Human Goals in Society",
                    "Undivided Society & Universal Order"
                )),
                RawUnit("Unit IV", "Harmony in Nature & Professional Ethics", listOf(
                    "Interconnectedness & Mutual Fulfillment in 4 Orders of Nature",
                    "Existence as Co-existence in All-pervasive Space",
                    "Definitiveness of Ethical Human Conduct",
                    "Competence in Professional Ethics & Humanistic Constitution"
                ))
            )
        )
    )

    fun getSyllabusForSubject(branch: String, code: String, name: String, completedKeys: Set<String>): SubjectSyllabus {
        val detail = DETAILS[code.uppercase()]
            ?: DETAILS.values.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: RawSubjectDetail(
                name = name,
                category = "Core Engineering",
                credits = 4,
                ltp = "3-1-0",
                units = listOf(
                    RawUnit("Unit I", "Module 1: Fundamentals", listOf("Core Concepts", "Definitions & Theory", "Basic Analysis")),
                    RawUnit("Unit II", "Module 2: Advanced Topics", listOf("Methodology", "Mathematical Formulation", "Applications")),
                    RawUnit("Unit III", "Module 3: Design & Practice", listOf("Design Principles", "Case Studies", "Laboratory Methods")),
                    RawUnit("Unit IV", "Module 4: Standards & Ethics", listOf("Codal Provisions", "Future Trends", "Professional Ethics"))
                )
            )

        val units = detail.units.mapIndexed { uIdx, u ->
            val topics = u.topics.mapIndexed { tIdx, tName ->
                val key = "t|$branch|$code|$uIdx|$tIdx"
                TopicItem(key = key, name = tName, completed = completedKeys.contains(key))
            }
            SyllabusUnit(unitNumber = u.label, unitTitle = u.title, topics = topics)
        }

        return SubjectSyllabus(
            subjectCode = code,
            subjectName = detail.name,
            detailKey = code,
            category = detail.category,
            credits = detail.credits,
            ltp = detail.ltp,
            units = units
        )
    }
}
