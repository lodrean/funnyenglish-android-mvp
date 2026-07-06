package com.funnyenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.funnyenglish.core.domain.repository.ThemeRepository
import com.funnyenglish.core.designsystem.theme.FunnyEnglishTheme
import org.koin.android.ext.android.getKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepository: ThemeRepository = getKoin().get()

        setContent {
            val isDarkMode by themeRepository.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
            val context = LocalContext.current
            val hasSeenOnboarding = remember {
                mutableStateOf(
                    context.getSharedPreferences("funnyenglish", android.content.Context.MODE_PRIVATE)
                        .getBoolean("has_seen_onboarding", false)
                )
            }

            FunnyEnglishTheme(darkTheme = isDarkMode) {
                if (!hasSeenOnboarding.value) {
                    OnboardingScreen(onFinished = { hasSeenOnboarding.value = true })
                } else {
                    AppNavigation()
                }
            }
        }
    }
}
