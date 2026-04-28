package com.funnyenglish.feature.dictionary

import com.funnyenglish.core.domain.model.Word

data class DictionaryState(
    val query: String = "",
    val isLoading: Boolean = false,
    val word: Word? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)
