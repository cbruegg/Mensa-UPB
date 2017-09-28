package com.cbruegg.mensaupb

import android.content.Context
import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.downloader.Repository
import dagger.Module
import dagger.Provides
import installStetho
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class NetModule {

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun provideDownloader(context: Context): Repository = Repository(context)

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder().installStetho().build()

    @Provides
    @Singleton
    @Suppress("DEPRECATION")
    fun provideDataCache(context: Context): ModelCache = ModelCache(context)
}