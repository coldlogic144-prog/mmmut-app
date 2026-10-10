package com.mmmut.ero.navigation

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.notifications.NotificationDeepLink
import com.mmmut.ero.ui.screens.*
import com.mmmut.ero.ui.three.ThreeDActivity

/* ==========================================================================
 * Top-level navigation
 * --------------------------------------------------------------------------
 * The bar is deliberately FIVE destinations. It previously carried up to seven
 * items (Home, Schedule, Syllabus, Notices, Telegram, Profile, +Admin), which
 * on a 320dp phone means labels clipped to a few characters and ~45dp targets.
 *
 * Where the removed entries went:
 *   Syllabus, Subjects, Results, Notices -> inside Academics (a hub screen)
 *   Telegram -> promoted onto Home and Profile as full-width labelled entries
 *   Admin    -> inside Profile, rendered ONLY for admin accounts
 *
 * Deep-link routes (notice/{id}, exam, event/{id}, hostel) are unchanged so the
 * FCM notification routing keeps working exactly as before.
 * ========================================================================== */

@Composable
fun AppNav(initialDeepLink: String? = null) {
    val nav = rememberNavController()
    val context = LocalContext.current
    var authed by remember { mutableStateOf(false) }
    var booted by remember { mutableStateOf(false) }
    var isAdminUser by remember { mutableStateOf(false) }

    val authRepo = remember { FirebaseAuthRepository() }

    LaunchedEffect(authed) {
        if (authed) {
            val p = try { authRepo.currentProfile() } catch (_: Exception) { null }
            isAdminUser = p?.isAdmin == true
        } else {
            isAdminUser = false
        }
    }

    LaunchedEffect(initialDeepLink) {
        NotificationDeepLink.parseUri(initialDeepLink)?.let {
            if (booted && authed) {
                try { nav.navigate(it) } catch (_: Exception) { }
            }
        }
    }

    // Primary destinations. Order matters: Home, the two things a student checks
    // daily (Timetable, Attendance), then the academic hub, then Profile.
    val tabs = remember {
        listOf(
            Routes.HOME to "Home",
            Routes.TIMETABLE to "Timetable",
            Routes.ATTENDANCE to "Attendance",
            Routes.ACADEMICS to "Academics",
            Routes.PROFILE to "Profile"
        )
    }
    val tabRoutes = remember(tabs) { tabs.map { it.first }.toSet() }

    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route

    // Show the bar on the tabs and on anything reached *from* a tab, so the
    // student is never stranded without a way back to the top level.
    val showBar = authed && (
        route in tabRoutes ||
            route == Routes.NOTICES ||
            route == Routes.NOTIFICATIONS ||
            route == Routes.HOSTEL ||
            route == Routes.MESS_MENU ||
            route == Routes.SYLLABUS ||
            route == Routes.TELEGRAM ||
            route?.startsWith("notice/") == true
        )

    Scaffold(bottomBar = {
        if (showBar) {
            NavigationBar {
                tabs.forEach { (r, label) ->
                    val selected = route == r ||
                        (r == Routes.ACADEMICS && (route == Routes.SYLLABUS || route == Routes.NOTICES)) ||
                        (r == Routes.HOME && route?.startsWith("notice/") == true)

                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (route != r) {
                                nav.navigate(r) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                when (r) {
                                    Routes.HOME -> Icons.Default.Home
                                    Routes.TIMETABLE -> Icons.Default.Schedule
                                    Routes.ATTENDANCE -> Icons.Default.CheckCircle
                                    Routes.ACADEMICS -> Icons.Default.School
                                    else -> Icons.Default.Person
                                },
                                contentDescription = label
                            )
                        },
                        label = {
                            // labelSmall + no wrap keeps five labels legible at
                            // 320dp instead of clipping mid-word.
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
        }
    }) { pad ->
        NavHost(nav, startDestination = Routes.SPLASH, modifier = Modifier.padding(pad)) {
            composable(Routes.SPLASH) {
                SplashScreen(onSession = {
                    authed = it; booted = true
                    nav.navigate(if (it) Routes.HOME else Routes.AUTH) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.AUTH) {
                AuthScreen(onLoggedIn = {
                    authed = true
                    nav.navigate(Routes.HOME) { popUpTo(Routes.AUTH) { inclusive = true } }
                })
            }
            composable(Routes.VERIFY_ROLL) { VerifyRollScreen(onDone = { nav.popBackStack() }) }

            composable(Routes.HOME) {
                HomeScreen(
                    onNotices = { nav.navigate(Routes.NOTICES) },
                    onNotice = { nav.navigate(Routes.noticeDetail(it)) },
                    onAcademics = { nav.navigate(Routes.ACADEMICS) },
                    onProfile = { nav.navigate(Routes.PROFILE) },
                    onHostel = { nav.navigate(Routes.HOSTEL) },
                    onMessMenu = { nav.navigate(Routes.MESS_MENU) },
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                    onTelegram = { nav.navigate(Routes.TELEGRAM) },
                    onCampus3D = { context.startActivity(Intent(context, ThreeDActivity::class.java)) },
                    onTimetable = { nav.navigate(Routes.TIMETABLE) },
                    onAttendance = { nav.navigate(Routes.ATTENDANCE) },
                    onSyllabus = { nav.navigate(Routes.SYLLABUS) }
                )
            }

            composable(Routes.TIMETABLE) { TimetableScreen() }
            composable(Routes.ATTENDANCE) { AttendanceScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ACADEMICS) {
                AcademicsScreen(onSyllabus = { nav.navigate(Routes.SYLLABUS) })
            }
            composable(Routes.SYLLABUS) {
                SyllabusScreen(onBack = { nav.popBackStack() })
            }

            composable(Routes.NOTICES) { NoticesScreen(onOpen = { nav.navigate(Routes.noticeDetail(it)) }) }
            composable(
                Routes.NOTICE_DETAIL,
                arguments = listOf(navArgument("noticeId") { type = NavType.StringType })
            ) {
                NoticeDetailScreen(it.arguments?.getString("noticeId") ?: "")
            }

            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLoggedOut = {
                        authed = false
                        nav.navigate(Routes.AUTH) { popUpTo(Routes.HOME) { inclusive = true } }
                    },
                    onVerifyRoll = { nav.navigate(Routes.VERIFY_ROLL) },
                    onTelegram = { nav.navigate(Routes.TELEGRAM) },
                    // Admin entry lives inside Profile and is only passed to a
                    // confirmed admin, so it cannot be reached by a student.
                    onAdmin = if (isAdminUser) {
                        { nav.navigate(Routes.ADMIN) }
                    } else null
                )
            }

            composable(Routes.HOSTEL) {
                HostelScreen(
                    onOpen = { nav.navigate(Routes.noticeDetail(it)) },
                    onMessMenu = { nav.navigate(Routes.MESS_MENU) },
                    onBack = { nav.popBackStack() }
                )
            }
            composable(Routes.MESS_MENU) { MessMenuScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(onOpen = { nav.navigate(Routes.noticeDetail(it)) }) }
            composable(Routes.TELEGRAM) { TelegramScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ADMIN) { AdminScreen() }
        }
    }
}
