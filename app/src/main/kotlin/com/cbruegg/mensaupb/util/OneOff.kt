package com.cbruegg.mensaupb.util

import android.content.Context

class OneOff(context: Context) {

    private val prefs = context.getSharedPreferences("OneOffs", Context.MODE_PRIVATE)

    /**
     * Launch the operation if it never happened before.
     */
    fun launch(key: String, f: () -> Unit) {
        synchronized(this) {
            if (!prefs.contains(key)) {
                prefs.edit().putInt(key, 0).apply()
                f()
            }
        }
    }
}