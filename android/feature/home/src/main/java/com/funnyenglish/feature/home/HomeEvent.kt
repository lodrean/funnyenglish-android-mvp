package com.funnyenglish.feature.home

sealed interface HomeEvent {
    data class NavigateTo(val route: String) : HomeEvent
}
