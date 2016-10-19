package com.cbruegg.mensaupb

import android.content.Context
import com.cbruegg.mensaupb.cache.DataCache
import com.cbruegg.mensaupb.downloader.Downloader
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class NetModule() {

    @Provides @Singleton
    fun provideDownloader(context: Context): Downloader = Downloader(context)

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient()

    @Provides @Singleton @Suppress("DEPRECATION")
    fun provideDataCache(context: Context): DataCache = DataCache(context)
}