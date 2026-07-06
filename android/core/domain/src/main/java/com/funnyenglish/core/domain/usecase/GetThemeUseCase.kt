package com.funnyenglish.core.domain.usecase

import com.funnyenglish.core.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.Flow

class GetThemeUseCase(
    private val themeRepository: ThemeRepository,
) {
    operator fun invoke(): Flow<Boolean> = themeRepository.isDarkMode
}
