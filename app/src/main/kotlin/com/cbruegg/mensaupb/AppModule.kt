package com.cbruegg.mensaupb

import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule(private val app: MensaApplication) {

    @Provides @Singleton
    fun provideApplication(): MensaApplication = app

    @Provides @Singleton
    fun provideContext(): Context = app
}