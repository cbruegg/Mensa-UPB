package com.cbruegg.mensaupb.util

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.CenterInside

/**
 * A transformation strategy based on [CenterInside] that never upscales.
 */
object OnlyScaleDownCenterInside : CenterInside() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap =
            if (toTransform.height > outHeight || toTransform.width > outWidth)
                super.transform(pool, toTransform, outWidth, outHeight)
            else toTransform
}