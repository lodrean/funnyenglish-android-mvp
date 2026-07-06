package com.funnyenglish.core.domain.usecase

import com.funnyenglish.core.domain.repository.WordRepository

class SetFavoriteUseCase(
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(
        word: String,
        isFavorite: Boolean,
    ) {
        wordRepository.toggleFavorite(word, isFavorite)
    }
}
