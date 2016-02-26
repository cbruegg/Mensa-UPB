package com.cbruegg.mensaupb.model

import android.support.annotation.StringRes
import com.cbruegg.mensaupb.R

/**
 * Enum of badges applicable for a dish.
 */
enum class Badge(private val id: String, @StringRes val descriptionId: Int) {
    VEGAN("vegan", R.string.vegan), VEGETARIAN("vegetarian", R.string.vegetarian),
    NONFAT("nonfat", R.string.nonfat), LACTOSE_FREE("lactose-free", R.string.lactose_free);

    companion object {
        /**
         * Each Badge has an id that is used by the API. This method retrieves a Badge by its id.
         * Return value will be null if there's no matching element.
         */
        fun findById(id: String): Badge? = values().firstOrNull { it.id == id }
    }
}