package com.cbruegg.mensaupb.adapter

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlin.platform.platformStatic

class PicassoBindingAdapter {
    companion object {
        @BindingAdapter("bind:imageUrl")
        platformStatic fun loadImage(imageView: ImageView, url: String) {
            Picasso.with(imageView.getContext())
                    .load(url)
                    .into(imageView)
        }
    }

}