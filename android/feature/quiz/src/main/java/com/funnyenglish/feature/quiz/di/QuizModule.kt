package com.funnyenglish.feature.quiz.di

import com.funnyenglish.feature.quiz.QuizViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val quizModule = module {
    viewModel { QuizViewModel() }
}
