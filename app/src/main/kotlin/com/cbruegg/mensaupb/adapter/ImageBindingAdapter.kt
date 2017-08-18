package com.cbruegg.mensaupb.adapter

import android.databinding.BindingAdapter
import android.widget.ImageView
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.cbruegg.mensaupb.GlideApp

@Suppress("unused")
/**
 * Used by the data binding feature.
 */
object ImageBindingAdapter {

    @BindingAdapter("bind:imageUrl", requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, url: String?) {
        if (url.isNullOrEmpty()) {
            GlideApp.with(view).clear(view)
            return
        }

        // TODO add recyclerview integration

        GlideApp.with(view)
                .load(url) // Performs automatic caching based on url (not headers!)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(view)
    }
}
