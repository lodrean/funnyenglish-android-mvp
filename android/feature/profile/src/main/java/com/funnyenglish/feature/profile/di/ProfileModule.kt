package com.funnyenglish.feature.profile.di

import com.funnyenglish.feature.profile.ProfileViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {
    viewModel { ProfileViewModel(get()) }
}
