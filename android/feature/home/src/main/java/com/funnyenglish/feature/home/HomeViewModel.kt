package com.funnyenglish.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadUserData()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnChatClick -> sendEvent(HomeEvent.NavigateTo("chat"))
            HomeAction.OnDictionaryClick -> sendEvent(HomeEvent.NavigateTo("dictionary"))
            HomeAction.OnQuizClick -> sendEvent(HomeEvent.NavigateTo("quiz"))
            HomeAction.OnGamesClick -> sendEvent(HomeEvent.NavigateTo("games"))
            HomeAction.OnProfileClick -> sendEvent(HomeEvent.NavigateTo("profile"))
            HomeAction.OnDailyWordClick -> {
                // Show word detail or copy to clipboard
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // TODO: Load from DataStore/Repository
            _state.update {
                it.copy(
                    isLoading = false,
                    streakDays = 3,
                    totalXp = 450,
                    dailyWord = "serendipity",
                    dailyWordDefinition = "a fortunate discovery by accident"
                )
            }
        }
    }

    private fun sendEvent(event: HomeEvent) {
        viewModelScope.launch {
            _events.send(event)
        }
    }
}
