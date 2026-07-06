package com.funnyenglish.feature.quiz

sealed interface QuizEvent {
    data object PlayCorrectSound : QuizEvent
    data object PlayIncorrectSound : QuizEvent
}
