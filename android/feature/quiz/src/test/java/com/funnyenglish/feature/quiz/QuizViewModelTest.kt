package com.funnyenglish.feature.quiz

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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: QuizViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = QuizViewModel()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has questions and score zero`() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.currentQuestionIndex)
            assertEquals(0, state.score)
            assertNull(state.selectedAnswer)
            assertNull(state.isCorrect)
            assertFalse(state.isFinished)
            assertTrue(state.questions.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectCorrectAnswer updates score and isCorrect`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            val question = initial.questions.first()
            val correctIndex = question.options.indexOf(question.correctTranslation)

            viewModel.onAction(QuizAction.SelectAnswer(correctIndex))

            val state = awaitItem()
            assertEquals(correctIndex, state.selectedAnswer)
            assertEquals(true, state.isCorrect)
            assertEquals(10, state.score)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selectIncorrectAnswer does not update score`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            val question = initial.questions.first()
            val incorrectIndex = question.options.indexOfFirst { it != question.correctTranslation }

            viewModel.onAction(QuizAction.SelectAnswer(incorrectIndex))

            val state = awaitItem()
            assertEquals(incorrectIndex, state.selectedAnswer)
            assertEquals(false, state.isCorrect)
            assertEquals(0, state.score)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `nextQuestion advances index`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            val question = initial.questions.first()
            val correctIndex = question.options.indexOf(question.correctTranslation)

            viewModel.onAction(QuizAction.SelectAnswer(correctIndex))
            skipItems(1)

            viewModel.onAction(QuizAction.NextQuestion)

            val state = awaitItem()
            assertEquals(1, state.currentQuestionIndex)
            assertNull(state.selectedAnswer)
            assertNull(state.isCorrect)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `restart resets state`() = runTest {
        viewModel.state.test {
            val initial = awaitItem()
            val question = initial.questions.first()
            val correctIndex = question.options.indexOf(question.correctTranslation)

            viewModel.onAction(QuizAction.SelectAnswer(correctIndex))
            skipItems(1)
            viewModel.onAction(QuizAction.NextQuestion)
            skipItems(1)

            viewModel.onAction(QuizAction.Restart)

            val state = awaitItem()
            assertEquals(0, state.currentQuestionIndex)
            assertEquals(0, state.score)
            assertNull(state.selectedAnswer)
            assertFalse(state.isFinished)
            cancelAndIgnoreRemainingEvents()
        }
    }

    /*
    @Test
    fun `events emits play sound events`() = runTest {
        viewModel.events.test {
            val initial = viewModel.state.value
            val question = initial.questions.first()
            val correctIndex = question.options.indexOf(question.correctTranslation)

            viewModel.onAction(QuizAction.SelectAnswer(correctIndex))

            val event = awaitItem()
            assertTrue(event is QuizEvent.PlayCorrectSound)
            cancelAndIgnoreRemainingEvents()
        }
    }
    */
}
