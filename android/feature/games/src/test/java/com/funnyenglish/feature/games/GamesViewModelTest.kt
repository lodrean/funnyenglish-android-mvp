package com.funnyenglish.feature.games

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: GamesViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GamesViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty board and correct message`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Твой ход (X)", state.message)
            assertFalse(state.showConfetti)
            assertEquals(null, state.game.winner)
            assertFalse(state.game.isDraw)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `makeMove places X and bot responds`() = runTest {
        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onAction(GamesAction.MakeMove(0, 0))

            val state = awaitItem()
            assertEquals('X', state.game.board[0][0])
            // Bot should have made a move too
            val botMoves = state.game.board.flatten().count { it == 'O' }
            assertEquals(1, botMoves)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `makeMove on occupied cell does nothing`() = runTest {
        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onAction(GamesAction.MakeMove(0, 0))
            val afterFirstMove = awaitItem()

            viewModel.onAction(GamesAction.MakeMove(0, 0))
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restart resets game state`() = runTest {
        viewModel.state.test {
            skipItems(1) // initial state

            viewModel.onAction(GamesAction.MakeMove(0, 0))
            skipItems(1) // after first move

            viewModel.onAction(GamesAction.Restart)

            val state = awaitItem()
            assertEquals("Твой ход (X)", state.message)
            assertFalse(state.showConfetti)
            assertTrue(state.game.board.flatten().all { it == ' ' })
            cancelAndIgnoreRemainingEvents()
        }
    }

}
