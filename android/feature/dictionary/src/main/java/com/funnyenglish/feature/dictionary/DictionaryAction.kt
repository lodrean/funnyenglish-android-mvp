package com.funnyenglish.feature.dictionary

sealed interface DictionaryAction {
    data class Search(val query: String) : DictionaryAction
    data object ToggleFavorite : DictionaryAction
}
