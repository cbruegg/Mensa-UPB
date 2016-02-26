package com.cbruegg.mensaupb.extensions

import android.support.v4.widget.DrawerLayout
import android.view.View
import android.view.ViewGroup

/**
 * Sequence of all direct children of the ViewGroup.
 * This is a non-recursive operation.
 */
val ViewGroup.children: Sequence<View>
    get() {
        var i = 0
        return generateSequence {
            if (i < childCount) {
                getChildAt(i++)
            } else {
                null
            }
        }
    }

/**
 * Switch the drawer state (closed/open).
 */
fun DrawerLayout.toggleDrawer(gravity: Int) {
    if (isDrawerOpen(gravity)) {
        closeDrawer(gravity)
    } else {
        openDrawer(gravity)
    }
}