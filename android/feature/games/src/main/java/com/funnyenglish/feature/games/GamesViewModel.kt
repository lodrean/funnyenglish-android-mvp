package com.funnyenglish.feature.games

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GamesViewModel : ViewModel() {

    private val _state = MutableStateFlow(GamesState())
    val state: StateFlow<GamesState> = _state.asStateFlow()

    fun onAction(action: GamesAction) {
        when (action) {
            is GamesAction.MakeMove -> makeMove(action.row, action.col)
            GamesAction.Restart -> restart()
            GamesAction.DismissConfetti -> dismissConfetti()
        }
    }

    private fun makeMove(row: Int, col: Int) {
        val currentGame = _state.value.game
        if (currentGame.winner != null || currentGame.isDraw) return

        val afterPlayer = currentGame.makeMove(row, col)
        if (afterPlayer === currentGame) return // invalid move

        when {
            afterPlayer.winner == 'X' -> {
                _state.update {
                    it.copy(
                        game = afterPlayer,
                        message = "🎉 Ты победил!",
                        showConfetti = true
                    )
                }
                return
            }
            afterPlayer.isDraw -> {
                _state.update {
                    it.copy(
                        game = afterPlayer,
                        message = "🤝 Ничья!"
                    )
                }
                return
            }
        }

        val afterBot = afterPlayer.botMove()
        when {
            afterBot.winner == 'O' -> {
                _state.update {
                    it.copy(
                        game = afterBot,
                        message = "🤖 Арчи победил!"
                    )
                }
            }
            afterBot.isDraw -> {
                _state.update {
                    it.copy(
                        game = afterBot,
                        message = "🤝 Ничья!"
                    )
                }
            }
            else -> {
                _state.update {
                    it.copy(
                        game = afterBot,
                        message = "Твой ход (X)"
                    )
                }
            }
        }
    }

    private fun restart() {
        _state.value = GamesState()
    }

    private fun dismissConfetti() {
        _state.update { it.copy(showConfetti = false) }
    }
}
