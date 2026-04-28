package com.funnyenglish.feature.quiz

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class QuizViewModel : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    fun onAction(action: QuizAction) {
        when (action) {
            is QuizAction.SelectAnswer -> checkAnswer(action.index)
            QuizAction.NextQuestion -> nextQuestion()
            QuizAction.Restart -> restart()
        }
    }

    private fun checkAnswer(index: Int) {
        val question = _state.value.questions.getOrNull(_state.value.currentQuestionIndex) ?: return
        val correct = question.options[index] == question.correctTranslation
        _state.update {
            it.copy(
                selectedAnswer = index,
                isCorrect = correct,
                score = if (correct) it.score + 10 else it.score
            )
        }
    }

    private fun nextQuestion() {
        val currentIndex = _state.value.currentQuestionIndex
        val questions = _state.value.questions
        if (currentIndex >= questions.size - 1) {
            _state.update { it.copy(isFinished = true) }
        } else {
            _state.update {
                it.copy(
                    currentQuestionIndex = currentIndex + 1,
                    selectedAnswer = null,
                    isCorrect = null
                )
            }
        }
    }

    private fun restart() {
        _state.value = QuizState(questions = quizQuestions.shuffled())
    }
}
