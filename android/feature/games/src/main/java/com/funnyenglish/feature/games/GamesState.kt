package com.funnyenglish.feature.games

data class GamesState(
    val game: TicTacToeGame = TicTacToeGame(),
    val message: String = "Твой ход (X)",
    val showConfetti: Boolean = false
)
