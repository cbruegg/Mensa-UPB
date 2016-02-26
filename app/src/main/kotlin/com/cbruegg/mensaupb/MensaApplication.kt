package com.cbruegg.mensaupb

import android.app.Application
import com.cbruegg.mensaupb.compat.OkHttp3Downloader
import com.squareup.picasso.Picasso

class MensaApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        initPicasso()
    }

    private fun initPicasso() {
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(OkHttp3Downloader(httpClient)).build())
    }
}