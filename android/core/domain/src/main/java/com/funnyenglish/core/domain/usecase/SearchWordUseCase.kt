package com.funnyenglish.core.domain.usecase

import com.funnyenglish.core.domain.model.Word
import com.funnyenglish.core.domain.repository.WordRepository
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result

class SearchWordUseCase(
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(query: String): Result<Pair<Word, Boolean>, DataError> {
        return when (val result = wordRepository.search(query)) {
            is Result.Success -> {
                val favorites = wordRepository.getFavorites()
                val isFavorite = favorites.any { it.spelling == result.data.spelling }
                Result.Success(result.data to isFavorite)
            }
            is Result.Error -> Result.Error(result.error)
        }
    }
}
