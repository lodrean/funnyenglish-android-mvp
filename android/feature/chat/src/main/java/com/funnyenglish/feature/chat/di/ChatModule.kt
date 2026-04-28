package com.funnyenglish.feature.chat.di

import com.funnyenglish.feature.chat.ChatViewModel
import com.funnyenglish.feature.chat.LocalAiRepository
import com.funnyenglish.feature.chat.ModelDownloader
import com.funnyenglish.feature.chat.ModelPathResolver
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chatModule = module {
    single { LocalAiRepository(get()) }
    single { ModelPathResolver(get()) }
    single { ModelDownloader(get()) }
    viewModel { ChatViewModel(get(), get(), get(), get()) }
}
