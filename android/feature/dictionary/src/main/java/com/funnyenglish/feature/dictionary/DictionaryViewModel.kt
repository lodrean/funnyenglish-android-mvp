package com.funnyenglish.feature.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funnyenglish.core.domain.repository.WordRepository
import com.funnyenglish.core.domain.util.onError
import com.funnyenglish.core.domain.util.onSuccess
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class DictionaryViewModel(
    private val repository: WordRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DictionaryState())
    val state: StateFlow<DictionaryState> = _state.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        searchQuery
            .debounce(500)
            .onEach { query ->
                if (query.length >= 2) {
                    searchWord(query)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: DictionaryAction) {
        when (action) {
            is DictionaryAction.Search -> {
                _state.update { it.copy(query = action.query, error = null) }
                searchQuery.value = action.query
            }
            DictionaryAction.ToggleFavorite -> {
                val word = _state.value.word ?: return
                viewModelScope.launch {
                    val newFavorite = !_state.value.isFavorite
                    repository.toggleFavorite(word.spelling, newFavorite)
                    _state.update { it.copy(isFavorite = newFavorite) }
                }
            }
        }
    }

    private fun searchWord(query: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, word = null, error = null) }
            repository.search(query)
                .onSuccess { word ->
                    val favorites = repository.getFavorites()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            word = word,
                            isFavorite = favorites.any { f -> f.spelling == word.spelling }
                        )
                    }
                }
                .onError { error ->
                    _state.update {
                        it.copy(isLoading = false, error = "Слово не найдено")
                    }
                }
        }
    }
}
