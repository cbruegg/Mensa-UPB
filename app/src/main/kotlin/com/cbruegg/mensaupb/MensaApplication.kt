package com.cbruegg.mensaupb

import android.app.Application
import android.content.Context
import android.support.v4.app.Fragment
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import javax.inject.Inject

class MensaApplication : Application() {

    val appComponent: AppComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .netModule(NetModule())
            .build()
    @Inject lateinit var httpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "")
        }

        appComponent.inject(this)
        initPicasso()
    }

    private fun initPicasso() {
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(com.cbruegg.mensaupb.compat.OkHttp3Downloader(httpClient)).build())
    }
}

val Context.app: MensaApplication
    get() = applicationContext as MensaApplication

val Fragment.app: MensaApplication
    get() = activity.app