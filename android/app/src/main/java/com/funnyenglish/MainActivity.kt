package com.funnyenglish

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.funnyenglish.core.data.local.ThemeRepository
import com.funnyenglish.core.designsystem.theme.FunnyEnglishTheme
import org.koin.android.ext.android.getKoin

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themeRepository: ThemeRepository = getKoin().get()

        setContent {
            val isDarkMode by themeRepository.isDarkMode.collectAsState(initial = isSystemInDarkTheme())

            FunnyEnglishTheme(darkTheme = isDarkMode) {
                AppNavigation()
            }
        }
    }
}
