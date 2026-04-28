package com.funnyenglish

import android.app.Application
import com.funnyenglish.core.data.di.dataModule
import com.funnyenglish.feature.chat.di.chatModule
import com.funnyenglish.feature.dictionary.di.dictionaryModule
import com.funnyenglish.feature.games.di.gamesModule
import com.funnyenglish.feature.home.di.homeModule
import com.funnyenglish.feature.profile.di.profileModule
import com.funnyenglish.feature.quiz.di.quizModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FunnyEnglishApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@FunnyEnglishApp)
            modules(
                dataModule,
                homeModule,
                dictionaryModule,
                quizModule,
                chatModule,
                gamesModule,
                profileModule
            )
        }
    }
}
