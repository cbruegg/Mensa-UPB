package com.cbruegg.mensaupb.adapter

import android.databinding.BindingAdapter
import android.widget.ImageView

import com.squareup.picasso.Picasso

/**
 * Used by the data binding feature.
 */
object PicassoBindingAdapter {

    @BindingAdapter("bind:imageUrl", "bind:scaleType", requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, url: String?, scaleType: ImageView.ScaleType?) {
        if (url != null && url.isNotEmpty()) {
            Picasso.with(view.context)
                    .load(url)
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
