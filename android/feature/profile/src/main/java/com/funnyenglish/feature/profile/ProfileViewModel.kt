package com.funnyenglish.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.core.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            themeRepository.isDarkMode.collect { isDark ->
                _state.update { it.copy(isDarkMode = isDark) }
            }
        }
        // Load mock user data (replace with real repository call)
        _state.update {
            it.copy(
                userName = "Ученик",
                level = 3,
                levelTitle = "English Explorer",
                xp = 450,
                xpToNextLevel = 500,
                streakDays = 3,
                wordsLearned = 12,
                achievements = listOf(
                    Achievement("🌱", "Первые шаги", true),
                    Achievement("📚", "Словарный запас", true),
                    Achievement("🏆", "Непобедимый", false),
                    Achievement("🔥", "Марафонец", false),
                    Achievement("👑", "Чемпион", false),
                    Achievement("🌍", "Полиглот", false)
                )
            )
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            themeRepository.setDarkMode(enabled)
        }
    }
}

data class ProfileState(
    val isDarkMode: Boolean = false,
    val userName: String = "",
    val level: Int = 1,
    val levelTitle: String = "",
    val xp: Int = 0,
    val xpToNextLevel: Int = 100,
    val streakDays: Int = 0,
    val wordsLearned: Int = 0,
    val achievements: List<Achievement> = emptyList()
)

data class Achievement(
    val icon: String,
    val name: String,
    val isUnlocked: Boolean
)
