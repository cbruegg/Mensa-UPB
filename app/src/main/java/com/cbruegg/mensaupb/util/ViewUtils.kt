package com.cbruegg.mensaupb.util

import android.content.res.Resources

object ViewUtils {
    fun dpToPx(dp: Int): Int = dp * Resources.getSystem().getDisplayMetrics().density.toInt()

    fun pxToDp(px: Int) = px / Resources.getSystem().getDisplayMetrics().density.toInt()
}