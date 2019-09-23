package com.cbruegg.mensaupb.downloader

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

object StudierendenWerkUrlRewriter : Interceptor {

    private val tag = StudierendenWerkUrlRewriter::class.java.simpleName

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url
        return if (!url.isHttps && url.host == "www.studentenwerk-pb.de") {
            val newUrl = url.newBuilder()
                .scheme("https")
                .host("www.studierendenwerk-pb.de")
                .build()
            Log.d(tag, "Rewriting old URL from $url to $newUrl")
            chain.proceed(chain.request().newBuilder().url(newUrl).build())
        } else {
            chain.proceed(chain.request())
        }
    }
}