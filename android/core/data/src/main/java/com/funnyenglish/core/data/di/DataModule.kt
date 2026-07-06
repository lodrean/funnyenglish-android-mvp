package com.funnyenglish.core.data.di

import android.content.Context
import androidx.room.Room
import com.funnyenglish.core.data.local.AppDatabase
import com.funnyenglish.core.data.local.ThemeRepositoryImpl
import com.funnyenglish.core.domain.repository.ThemeRepository
import com.funnyenglish.core.data.local.WordDao
import com.funnyenglish.core.data.remote.DictionaryApi
import com.funnyenglish.core.data.remote.FunnyEnglishApi
import com.funnyenglish.core.data.remote.HttpClientFactory
import com.funnyenglish.core.data.repository.WordRepositoryImpl
import com.funnyenglish.core.domain.repository.WordRepository
import org.koin.dsl.module

val dataModule = module {
    single { HttpClientFactory.create() }
    single { DictionaryApi(get()) }
    single { FunnyEnglishApi(get()) }

    single {
        Room.databaseBuilder(
            get<Context>(),
            AppDatabase::class.java,
            "funnyenglish.db"
        ).build()
    }

    single<WordDao> { get<AppDatabase>().wordDao() }

    single<WordRepository> { WordRepositoryImpl(get(), get()) }

    single<ThemeRepository> { ThemeRepositoryImpl(get()) }
}
