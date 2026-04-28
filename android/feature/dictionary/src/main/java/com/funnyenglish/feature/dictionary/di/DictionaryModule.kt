package com.funnyenglish.feature.dictionary.di

import com.funnyenglish.feature.dictionary.DictionaryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dictionaryModule = module {
    viewModel { DictionaryViewModel(get()) }
}
