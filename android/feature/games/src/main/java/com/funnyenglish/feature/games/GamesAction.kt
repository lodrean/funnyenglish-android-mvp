package com.funnyenglish.feature.games

sealed interface GamesAction {
    data class MakeMove(val row: Int, val col: Int) : GamesAction
    data object Restart : GamesAction
    data object DismissConfetti : GamesAction
}
