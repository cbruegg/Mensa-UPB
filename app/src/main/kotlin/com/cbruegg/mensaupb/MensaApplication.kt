package com.cbruegg.mensaupb

import android.app.Application
import android.content.Context
import android.support.v4.app.Fragment
import com.cbruegg.mensaupb.compat.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient

class MensaApplication : Application() {

    val netComponent: NetComponent = DaggerNetComponent.builder()
            .appModule(AppModule(this))
            .netModule(NetModule())
            .build()
    lateinit var httpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        netComponent.inject(this)
        initPicasso()
    }

    private fun initPicasso() {
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(OkHttp3Downloader(httpClient)).build())
    }
}

val Context.app: MensaApplication
    get() = applicationContext as MensaApplication

val Fragment.app: MensaApplication
    get() = activity.app