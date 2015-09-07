package com.cbruegg.mensaupb.model

import android.support.annotation.StringRes
import com.cbruegg.mensaupb.R

enum class Badge(private val id: String, public StringRes val descriptionId: Int) {
    VEGAN("vegan", R.string.vegan), VEGETARIAN("vegetarian", R.string.vegetarian), NONFAT("nonfat", R.string.nonfat), LACTOSE_FREE("lactose-free", R.string.lactose_free);

    companion object {
        fun findById(id: String): Badge? = values().firstOrNull { it.id == id }
    }
}