package com.funnyenglish.core.domain.repository

import com.funnyenglish.core.domain.model.Word
import com.funnyenglish.core.domain.util.DataError
import com.funnyenglish.core.domain.util.Result

interface WordRepository {
    suspend fun search(word: String): Result<Word, DataError>
    suspend fun getFavorites(): List<Word>
    suspend fun toggleFavorite(word: String, isFavorite: Boolean)
}
