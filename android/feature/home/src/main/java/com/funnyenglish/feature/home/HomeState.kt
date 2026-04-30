package com.funnyenglish.feature.home

data class HomeState(
    val userName: String = "Друг",
    val streakDays: Int = 0,
    val totalXp: Int = 0,
    val dailyWord: String = "serendipity",
    val dailyWordDefinition: String = "a fortunate discovery by accident",
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false
)
