package com.funnyenglish.feature.home

sealed interface HomeAction {
    data object OnChatClick : HomeAction
    data object OnDictionaryClick : HomeAction
    data object OnQuizClick : HomeAction
    data object OnGamesClick : HomeAction
    data object OnProfileClick : HomeAction
    data object OnDailyWordClick : HomeAction
}
