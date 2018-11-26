package com.cbruegg.mensaupb

import android.content.Context
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import java.io.InputStream
import javax.inject.Inject

@GlideModule
class AppGlideModule : AppGlideModule() {

    @Inject
    lateinit var client: OkHttpClient

    private val cacheSizeBytes = 50 * 1024 * 1024L

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        super.registerComponents(context, glide, registry)
        context.app.appComponent.inject(this)
        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, cacheSizeBytes))
        if (BuildConfig.DEBUG) {
            builder.setLogLevel(Log.DEBUG)
        }
    }
}