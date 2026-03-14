package com.cbruegg.mensaupb.util

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView

class MaxHeightNestedScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : NestedScrollView(context, attrs) {

    var maxHeightPx: Int = Int.MAX_VALUE
        set(value) {
            field = value
            requestLayout()
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cappedHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightPx, MeasureSpec.AT_MOST)
        super.onMeasure(widthMeasureSpec, cappedHeightMeasureSpec)
    }
}
