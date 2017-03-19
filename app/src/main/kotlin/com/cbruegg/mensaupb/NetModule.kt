package com.cbruegg.mensaupb

import android.content.Context
import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.downloader.forceCacheInterceptChain
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module(includes = arrayOf(AppModule::class))
class NetModule {

    @Suppress("DEPRECATION")
    @Provides @Singleton
    fun provideDownloader(context: Context): Downloader = Downloader(context)

    @Provides @Singleton
    fun provideOkHttp(context: Context, data: BlockingEntityStore<Persistable>): OkHttpClient =
            OkHttpClient.Builder()
                    .addInterceptor { forceCacheInterceptChain(data, context, it) }
                    .build()

    @Provides @Singleton @Suppress("DEPRECATION")
    fun provideDataCache(context: Context): ModelCache = ModelCache(context)
}