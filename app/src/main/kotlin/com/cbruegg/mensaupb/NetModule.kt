package com.cbruegg.mensaupb

import android.content.Context
import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.downloader.StudierendenWerkUrlRewriter
import dagger.Module
import dagger.Provides
import installStetho
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TIMEOUT_MS = 10_000L

@Module(includes = [AppModule::class])
class NetModule {

    @Suppress("DEPRECATION")
    @Provides
    @Singleton
    fun provideDownloader(context: Context): Repository = Repository(context)

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .callTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .addInterceptor(StudierendenWerkUrlRewriter)
        .installStetho()
        .build()

    @Provides
    @Singleton
    @Suppress("DEPRECATION")
    fun provideDataCache(context: Context): ModelCache = ModelCache(context)
}