package com.mmmut.ero

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.mmmut.ero.ui.theme.MmmutTheme
import com.mmmut.ero.ui.screens.AuthScreen
import org.junit.Rule
import org.junit.Test

class AuthComposeTest {
    @get:Rule val rule = createComposeRule()

    @Test fun authScreen_showsLoginAndBrand() {
        rule.setContent { MmmutTheme { AuthScreen(onLoggedIn = {}) } }
        rule.onNodeWithText("Login").assertExists()
        rule.onNodeWithText("MMMUT").assertExists()
    }
}
