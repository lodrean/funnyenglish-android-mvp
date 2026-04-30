package com.funnyenglish.feature.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen() {
    var game by remember { mutableStateOf(TicTacToeGame()) }
    var message by remember { mutableStateOf("Твой ход (X)") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Крестики-нолики") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                TicTacToeBoard(
                    game = game,
                    onMove = { row, col ->
                        if (game.makeMove(row, col)) {
                            message = when {
                                game.winner == 'X' -> "🎉 Ты победил!"
                                game.isDraw -> "🤝 Ничья!"
                                else -> {
                                    game.botMove()
                                    when {
                                        game.winner == 'O' -> "🤖 Арчи победил!"
                                        game.isDraw -> "🤝 Ничья!"
                                        else -> "Твой ход (X)"
                                    }
                                }
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    game = TicTacToeGame()
                    message = "Твой ход (X)"
                }
            ) {
                Text("Новая игра")
            }
        }
    }
}

@Composable
private fun TicTacToeBoard(
    game: TicTacToeGame,
    onMove: (Int, Int) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .size(300.dp)
            .padding(8.dp)
            .pointerInput(game.board.contentDeepToString()) {
                detectTapGestures { offset ->
                    val cellSize = size.width / 3f
                    val col = (offset.x / cellSize).toInt()
                    val row = (offset.y / cellSize).toInt()
                    if (row in 0..2 && col in 0..2) {
                        onMove(row, col)
                    }
                }
            }
    ) {
        // Track previous board to detect new moves for animation
        var prevBoard by remember { mutableStateOf(Array(3) { CharArray(3) { '\u0000' } }) }
        val newMoves = remember(game.board) {
            val moves = mutableListOf<Pair<Int, Int>>()
            for (r in 0..2) {
                for (c in 0..2) {
                    if (game.board[r][c] != '\u0000' && prevBoard[r][c] == '\u0000') {
                        moves.add(r to c)
                    }
                }
            }
            prevBoard = game.board.map { it.copyOf() }.toTypedArray()
            moves
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / 3f
            val lineWidth = 4.dp.toPx()

            // Grid lines
            for (i in 1..2) {
                drawLine(
                    color = gridColor,
                    start = Offset(i * cellSize, 0f),
                    end = Offset(i * cellSize, size.height),
                    strokeWidth = lineWidth
                )
                drawLine(
                    color = gridColor,
                    start = Offset(0f, i * cellSize),
                    end = Offset(size.width, i * cellSize),
                    strokeWidth = lineWidth
                )
            }

            // Draw X and O
            for (r in 0..2) {
                for (c in 0..2) {
                    val x = c * cellSize + cellSize / 2
                    val y = r * cellSize + cellSize / 2
                    val padding = cellSize * 0.25f
                    val isNewMove = (r to c) in newMoves
                    val animatedScale = if (isNewMove) {
                        // Use a static value for Canvas — animation handled by overlay
                        1f
                    } else 1f

                    when (game.board[r][c]) {
                        'X' -> {
                            drawLine(
                                color = primaryColor,
                                start = Offset(x - padding, y - padding),
                                end = Offset(x + padding, y + padding),
                                strokeWidth = lineWidth * 1.5f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = primaryColor,
                                start = Offset(x + padding, y - padding),
                                end = Offset(x - padding, y + padding),
                                strokeWidth = lineWidth * 1.5f,
                                cap = StrokeCap.Round
                            )
                        }
                        'O' -> {
                            drawCircle(
                                color = tertiaryColor,
                                radius = padding,
                                center = Offset(x, y),
                                style = Stroke(width = lineWidth * 1.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}
