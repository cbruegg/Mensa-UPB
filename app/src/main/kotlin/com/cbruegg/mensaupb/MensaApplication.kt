package com.cbruegg.mensaupb

import android.content.Context
import android.support.multidex.MultiDexApplication
import android.support.v4.app.Fragment
import installStetho
import okhttp3.OkHttpClient
import javax.inject.Inject

class MensaApplication : MultiDexApplication() {

    // TODO Fix empty screen when launched behind lockscreen

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
        installStetho()
    }

}

val Context.app: MensaApplication
    get() = applicationContext as MensaApplication

val Fragment.app: MensaApplication
    get() = activity.app