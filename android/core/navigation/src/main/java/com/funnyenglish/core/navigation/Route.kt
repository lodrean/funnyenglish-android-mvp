package com.funnyenglish.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route

@Serializable
data object HomeRoute : Route

@Serializable
data object DictionaryRoute : Route

@Serializable
data object QuizRoute : Route

@Serializable
data object ChatRoute : Route

@Serializable
data object ProfileRoute : Route

@Serializable
data object GamesRoute : Route
