package com.funnyenglish.feature.games

data class TicTacToeGame(
    val board: List<List<Char>> = List(3) { List(3) { ' ' } },
    val currentPlayer: Char = 'X',
    val winner: Char? = null,
    val isDraw: Boolean = false
) {
    fun makeMove(row: Int, col: Int): TicTacToeGame {
        if (winner != null || isDraw) return this
        if (row !in 0..2 || col !in 0..2) return this
        if (board[row][col] != ' ') return this

        val newBoard = board.mapIndexed { r, cols ->
            cols.mapIndexed { c, ch ->
                if (r == row && c == col) currentPlayer else ch
            }
        }
        val newGame = copy(board = newBoard, currentPlayer = currentPlayer)
        val (newWinner, newIsDraw) = newGame.calculateGameState(currentPlayer)
        return newGame.copy(
            winner = newWinner,
            isDraw = newIsDraw,
            currentPlayer = if (newWinner == null && !newIsDraw) {
                if (currentPlayer == 'X') 'O' else 'X'
            } else currentPlayer
        )
    }

    fun botMove(): TicTacToeGame {
        // Try to win
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') {
                    val test = makeMove(r, c)
                    if (test.winner == 'O') return test
                }
            }
        }
        // Block X
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') {
                    val testBoard = board.mapIndexed { rr, cols ->
                        cols.mapIndexed { cc, ch ->
                            if (rr == r && cc == c) 'X' else ch
                        }
                    }
                    val testGame = copy(board = testBoard)
                    if (testGame.checkWinner('X')) {
                        return makeMove(r, c)
                    }
                }
            }
        }
        // Center
        if (board[1][1] == ' ') return makeMove(1, 1)
        // Corners
        val corners = listOf(0 to 0, 0 to 2, 2 to 0, 2 to 2).shuffled()
        for ((r, c) in corners) {
            if (board[r][c] == ' ') return makeMove(r, c)
        }
        // Any
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') return makeMove(r, c)
            }
        }
        return this
    }

    private fun calculateGameState(lastPlayer: Char): Pair<Char?, Boolean> {
        if (checkWinner(lastPlayer)) return lastPlayer to false
        if (board.all { row -> row.all { it != ' ' } }) return null to true
        return null to false
    }

    private fun checkWinner(player: Char): Boolean {
        for (i in 0..2) {
            if (board[i].all { it == player }) return true
            if ((0..2).all { board[it][i] == player }) return true
        }
        if ((0..2).all { board[it][it] == player }) return true
        if ((0..2).all { board[it][2 - it] == player }) return true
        return false
    }
}
