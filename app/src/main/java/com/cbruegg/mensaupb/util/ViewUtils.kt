package com.cbruegg.mensaupb.util

import android.content.res.Resources

object ViewUtils {
    /**
     * Convert dp to pixels.
     */
    fun dpToPx(dp: Int): Int = dp * Resources.getSystem().getDisplayMetrics().density.toInt()

    /**
     * Convert pixels to dp.
     */
    fun pxToDp(px: Int) = px / Resources.getSystem().getDisplayMetrics().density.toInt()
}