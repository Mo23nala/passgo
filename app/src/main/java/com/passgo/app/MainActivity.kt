package com.passgo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.passgo.app.core.navigation.PassGoNavHost
import com.passgo.app.core.ui.theme.PassGoTheme
import com.passgo.app.data.session.SessionManager
import com.passgo.app.data.settings.ThemeMode
import com.passgo.app.data.settings.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        setContent {
            val themeMode by userPreferences.themeMode.collectAsState(
                initial = ThemeMode.SYSTEM
            )
            PassGoTheme(themeMode = themeMode) {
                PassGoNavHost(sessionManager = sessionManager)
            }
        }
    }
}
