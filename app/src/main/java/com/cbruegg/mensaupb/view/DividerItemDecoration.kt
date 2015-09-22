package com.cbruegg.mensaupb.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import com.cbruegg.mensaupb.extensions.children

/**
 * RecyclerView item decoration that adds divider lines.
 */
public class DividerItemDecoration : RecyclerView.ItemDecoration {

    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }

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
    public constructor(context: Context, @DrawableRes resId: Int) {
        divider = ContextCompat.getDrawable(context, resId)
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State?) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (child in parent.children) {
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

}