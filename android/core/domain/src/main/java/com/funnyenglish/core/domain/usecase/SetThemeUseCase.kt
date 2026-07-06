package com.funnyenglish.core.domain.usecase

import com.funnyenglish.core.domain.repository.ThemeRepository

class SetThemeUseCase(
    private val themeRepository: ThemeRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        themeRepository.setDarkMode(enabled)
    }
}
