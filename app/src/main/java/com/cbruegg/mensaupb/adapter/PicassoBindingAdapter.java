package com.cbruegg.mensaupb.adapter;

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Used by the data binding feature.
 */
@SuppressWarnings("unused") public class PicassoBindingAdapter {
    @BindingAdapter({"bind:imageUrl"})
    public static void loadImage(ImageView view, String url) {
        if (url != null && url.length() > 0) {
            Picasso.with(view.getContext()).load(url).into(view);
        }
    }
}
