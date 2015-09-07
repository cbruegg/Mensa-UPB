package com.cbruegg.mensaupb.extensions

import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.ViewGroup

val ViewGroup.children: Sequence<View>
    get() {
        var i = 0
        return sequence {
            if (i < getChildCount()) {
                getChildAt(i++)
            } else {
                null
            }
        }
    }

fun DrawerLayout.toggleDrawer(gravity: Int) {
    if (isDrawerOpen(gravity)) {
        closeDrawer(gravity)
    } else {
        openDrawer(gravity)
    }
}