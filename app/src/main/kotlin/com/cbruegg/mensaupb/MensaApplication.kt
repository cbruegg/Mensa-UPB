package com.cbruegg.mensaupb

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.multidex.MultiDexApplication
import installStetho

class MensaApplication : MultiDexApplication() {

    val appComponent: AppComponent = DaggerAppComponent.builder()
        .appModule(AppModule(this))
        .netModule(NetModule())
        .build()

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            System.setProperty("kotlinx.coroutines.debug", "")
        }

        installStetho()
    }

}

val Context.app: MensaApplication
    get() = applicationContext as MensaApplication

val Fragment.app: MensaApplication
    get() = (activity ?: context!!).app