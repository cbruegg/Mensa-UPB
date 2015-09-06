package com.cbruegg.mensaupb.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.cbruegg.mensaupb.extensions.children

private val ATTRS = intArrayOf(android.R.attr.listDivider)

public class DividerItemDecoration : RecyclerView.ItemDecoration {

    private val divider: Drawable

    /**
     * Default divider will be used
     */
    public constructor(context: Context) {
        val styledAttributes = context.obtainStyledAttributes(ATTRS)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    /**
     * Custom divider will be used
     */
    public constructor(context: Context, DrawableRes resId: Int) {
        divider = ContextCompat.getDrawable(context, resId)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val left = parent.getPaddingLeft()
        val right = parent.getWidth() - parent.getPaddingRight()

        for (child in parent.children) {
            val params = child.getLayoutParams() as RecyclerView.LayoutParams

            val top = child.getBottom() + params.bottomMargin
            val bottom = top + divider.getIntrinsicHeight()

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

}