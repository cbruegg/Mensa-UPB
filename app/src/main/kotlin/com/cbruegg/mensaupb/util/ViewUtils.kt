package com.cbruegg.mensaupb.util

import android.content.res.Resources

/**
 * Convert dp to pixels.
 */
fun dpToPx(dp: Int): Int = dp * Resources.getSystem().displayMetrics.density.toInt()

/**
 * Convert pixels to dp.
 */
fun pxToDp(px: Int) = px / Resources.getSystem().displayMetrics.density.toInt()