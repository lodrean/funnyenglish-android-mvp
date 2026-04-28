package com.funnyenglish.feature.quiz

sealed interface QuizAction {
    data class SelectAnswer(val index: Int) : QuizAction
    data object NextQuestion : QuizAction
    data object Restart : QuizAction
}
