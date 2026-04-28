package com.funnyenglish.feature.games

class TicTacToeGame {
    var board: Array<CharArray> = Array(3) { CharArray(3) { ' ' } }
        private set
    var currentPlayer: Char = 'X'
        private set
    var winner: Char? = null
        private set
    var isDraw: Boolean = false
        private set

    fun makeMove(row: Int, col: Int): Boolean {
        if (winner != null || isDraw) return false
        if (row !in 0..2 || col !in 0..2) return false
        if (board[row][col] != ' ') return false

        board[row][col] = currentPlayer
        checkGameState()
        if (winner == null && !isDraw) {
            currentPlayer = if (currentPlayer == 'X') 'O' else 'X'
        }
        return true
    }

    fun botMove(): Pair<Int, Int> {
        // Try to win
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') {
                    board[r][c] = 'O'
                    if (checkWinner('O')) {
                        board[r][c] = ' '
                        makeMove(r, c)
                        return r to c
                    }
                    board[r][c] = ' '
                }
            }
        }
        // Block X
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') {
                    board[r][c] = 'X'
                    if (checkWinner('X')) {
                        board[r][c] = ' '
                        makeMove(r, c)
                        return r to c
                    }
                    board[r][c] = ' '
                }
            }
        }
        // Center
        if (board[1][1] == ' ') {
            makeMove(1, 1)
            return 1 to 1
        }
        // Corners
        val corners = listOf(0 to 0, 0 to 2, 2 to 0, 2 to 2).shuffled()
        for ((r, c) in corners) {
            if (board[r][c] == ' ') {
                makeMove(r, c)
                return r to c
            }
        }
        // Any
        for (r in 0..2) {
            for (c in 0..2) {
                if (board[r][c] == ' ') {
                    makeMove(r, c)
                    return r to c
                }
            }
        }
        return -1 to -1
    }

    fun reset() {
        board = Array(3) { CharArray(3) { ' ' } }
        currentPlayer = 'X'
        winner = null
        isDraw = false
    }

    private fun checkGameState() {
        if (checkWinner(currentPlayer)) {
            winner = currentPlayer
            return
        }
        if (board.all { row -> row.all { it != ' ' } }) {
            isDraw = true
        }
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
