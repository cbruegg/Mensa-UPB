package com.cbruegg.mensaupb.extensions

import androidx.drawerlayout.widget.DrawerLayout

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