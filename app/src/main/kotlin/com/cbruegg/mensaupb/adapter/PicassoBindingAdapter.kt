package com.cbruegg.mensaupb.adapter

import android.databinding.BindingAdapter
import android.net.Uri
import android.widget.ImageView
import com.cbruegg.mensaupb.downloader.forceCached

import com.squareup.picasso.Picasso

@Suppress("unused")
/**
 * Used by the data binding feature.
 */
object PicassoBindingAdapter {

    @BindingAdapter("bind:imageUrl", "bind:scaleType", "bind:forceCache", requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, url: String?, scaleType: ImageView.ScaleType?, forceCache: Boolean?) {
        if (url != null && url.isNotEmpty()) {
            val uri = Uri.parse(url)

            Picasso.with(view.context)
                    .load(if (forceCache ?: false) uri.forceCached else uri)
                    .apply {
                        when (scaleType) {
                            ImageView.ScaleType.CENTER_CROP -> {
                                fit()
                                centerCrop()
                            }
                            ImageView.ScaleType.CENTER_INSIDE -> {
                                fit()
                                centerInside()
                            }
                            null -> {
                            }
                            else -> view.scaleType = scaleType
                        }
                    }
                    .into(view)
        }
    }
}
