package com.cbruegg.mensaupb.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

fun Context.xmlDrawableToBitmap(@DrawableRes res: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(this, res)!!.apply {
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    }
    val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.draw(canvas)
    return bitmap
}