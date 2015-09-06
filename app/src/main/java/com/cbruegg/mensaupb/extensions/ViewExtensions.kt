package com.cbruegg.mensaupb.extensions

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