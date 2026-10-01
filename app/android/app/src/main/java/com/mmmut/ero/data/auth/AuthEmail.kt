package com.mmmut.ero.data.auth

import java.util.Locale

object AuthEmail {
    const val SUFFIX = "@mmmut.local"
    private val USERNAME_RE = Regex("^[a-z0-9._-]+$")

    fun toEmail(username: String): String {
        val u = username.trim().lowercase(Locale.US)
        return if (u.endsWith(SUFFIX)) u else u + SUFFIX
    }

    fun validateUsername(username: String): String? {
        val u = username.trim().lowercase(Locale.US)
        if (u.length < 3) return "Username should be at least 3 characters."
        if (!USERNAME_RE.matches(u.removeSuffix(SUFFIX))) return "Username can only contain letters, numbers, dots, hyphens, and underscores."
        return null
    }

    fun validatePassword(password: String): String? {
        if (password.length < 6) return "Password should be at least 6 characters."
        return null
    }

    fun friendlyAuthError(rawCode: String?, mode: String): String {
        val code = rawCode?.lowercase(Locale.US)?.removePrefix("auth/") ?: ""
        if (code.contains("wrong-password") || code.contains("wrong_password") ||
            code.contains("invalid-credential") || code.contains("invalid_credential") ||
            code.contains("invalid-password") || code.contains("invalid_password")) {
            return "Incorrect username or password."
        }
        if (code.contains("user-not-found") || code.contains("user_not_found")) {
            return "No account with that username. Try signing up."
        }
        if (code.contains("email-already-in-use") || code.contains("email_already_in_use") || code.contains("email-already-exists")) {
            return "That username is already taken."
        }
        if (code.contains("weak-password") || code.contains("weak_password")) {
            return "Password should be at least 6 characters."
        }
        if (code.contains("too-many-requests") || code.contains("too_many_requests")) {
            return "Too many failed attempts. Please try again later."
        }
        if (code.contains("user-disabled") || code.contains("user_disabled")) {
            return "This account has been disabled."
        }
        if (code.contains("network-request-failed") || code.contains("network")) {
            return "Network error. Check your connection and try again."
        }
        if (code.contains("permission-denied") || code.contains("permission_denied")) {
            return "Account saved, but Firestore rules blocked the profile. Contact admin."
        }
        if (rawCode.isNullOrBlank()) {
            return if (mode == "signup") "Something went wrong creating your account." else "Could not log in — please try again."
        }
        return (if (mode == "signup") "Signup failed: " else "Login failed: ") + rawCode
    }
}
