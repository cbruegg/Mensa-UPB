package com.cbruegg.mensaupb.adapter

import android.databinding.BindingAdapter
import android.widget.ImageView

import com.squareup.picasso.Picasso

/**
 * Used by the data binding feature.
 */
object PicassoBindingAdapter {

    @BindingAdapter("bind:imageUrl")
    @JvmStatic
    fun loadImage(view: ImageView, url: String?) {
        if (url != null && url.isNotEmpty()) {
            Picasso.with(view.context).load(url).into(view)
        }
    }
}
