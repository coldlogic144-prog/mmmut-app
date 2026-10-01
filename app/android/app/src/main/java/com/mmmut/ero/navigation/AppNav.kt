package com.mmmut.ero.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.mmmut.ero.data.repository.FirebaseAuthRepository
import com.mmmut.ero.notifications.NotificationDeepLink
import com.mmmut.ero.ui.screens.*

@Composable
fun AppNav(initialDeepLink: String? = null) {
    val nav = rememberNavController()
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

    val baseTabs = mutableListOf(
        Routes.HOME to "Home",
        Routes.TIMETABLE to "Schedule",
        Routes.SYLLABUS to "Syllabus",
        Routes.NOTICES to "Notices",
        Routes.TELEGRAM to "Telegram",
        Routes.PROFILE to "Profile"
    )
    if (isAdminUser) {
        baseTabs.add(Routes.ADMIN to "Admin")
    }

    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route
    val showBar = authed && (route in baseTabs.map { it.first } || route == Routes.ACADEMICS || route == Routes.HOSTEL || route == Routes.NOTIFICATIONS || route?.startsWith("notice/") == true)

    Scaffold(bottomBar = {
        if (showBar) NavigationBar {
            baseTabs.forEach { (r, label) ->
                NavigationBarItem(
                    selected = route == r || (r == Routes.NOTICES && route?.startsWith("notice/") == true),
                    onClick = { nav.navigate(r) { popUpTo(Routes.HOME); launchSingleTop = true } },
                    icon = {
                        Icon(when (r) {
                            Routes.HOME -> Icons.Default.Home
                            Routes.TIMETABLE -> Icons.Default.Schedule
                            Routes.SYLLABUS -> Icons.AutoMirrored.Filled.MenuBook
                            Routes.NOTICES -> Icons.Default.Notifications
                            Routes.TELEGRAM -> Icons.AutoMirrored.Filled.Send
                            Routes.PROFILE -> Icons.Default.Person
                            Routes.ADMIN -> Icons.Default.AdminPanelSettings
                            else -> Icons.Default.School
                        }, contentDescription = label)
                    },
                    label = { Text(label, maxLines = 1) }
                )
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
                    onNotifications = { nav.navigate(Routes.NOTIFICATIONS) },
                    onTelegram = { nav.navigate(Routes.TELEGRAM) }
                )
            }
            composable(Routes.TIMETABLE) { TimetableScreen() }
            composable(Routes.SYLLABUS) { SyllabusScreen() }
            composable(Routes.ACADEMICS) { AcademicsScreen() }
            composable(Routes.NOTICES) { NoticesScreen(onOpen = { nav.navigate(Routes.noticeDetail(it)) }) }
            composable(Routes.NOTICE_DETAIL, arguments = listOf(navArgument("noticeId") { type = NavType.StringType })) {
                NoticeDetailScreen(it.arguments?.getString("noticeId") ?: "")
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onLoggedOut = {
                        authed = false
                        nav.navigate(Routes.AUTH) { popUpTo(Routes.HOME) { inclusive = true } }
                    },
                    onVerifyRoll = { nav.navigate(Routes.VERIFY_ROLL) },
                    onTelegram = { nav.navigate(Routes.TELEGRAM) }
                )
            }
            composable(Routes.HOSTEL) { HostelScreen(onOpen = { nav.navigate(Routes.noticeDetail(it)) }) }
            composable(Routes.NOTIFICATIONS) { NotificationsScreen(onOpen = { nav.navigate(Routes.noticeDetail(it)) }) }
            composable(Routes.TELEGRAM) { TelegramScreen(onBack = { nav.popBackStack() }) }
            composable(Routes.ADMIN) { AdminScreen() }
        }
    }
}
