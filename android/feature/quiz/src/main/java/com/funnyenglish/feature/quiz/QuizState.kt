package com.funnyenglish.feature.quiz

data class QuizState(
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val selectedAnswer: Int? = null,
    val isCorrect: Boolean? = null,
    val isFinished: Boolean = false,
    val questions: List<QuizQuestion> = quizQuestions.shuffled()
)
