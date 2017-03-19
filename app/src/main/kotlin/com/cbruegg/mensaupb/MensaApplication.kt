package com.cbruegg.mensaupb

import android.app.Application
import android.content.Context
import android.support.v4.app.Fragment
import com.cbruegg.mensaupb.compat.OkHttp3Downloader
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
        val picasso = Picasso.Builder(this)
                .downloader(OkHttp3Downloader(httpClient))
                .indicatorsEnabled(BuildConfig.DEBUG)
                .listener { _, _, exception ->
                    if (BuildConfig.DEBUG) {
                        exception?.printStackTrace()
                    }
                }
                .build()
        Picasso.setSingletonInstance(picasso)
    }
}

val Context.app: MensaApplication
    get() = applicationContext as MensaApplication

val Fragment.app: MensaApplication
    get() = activity.app