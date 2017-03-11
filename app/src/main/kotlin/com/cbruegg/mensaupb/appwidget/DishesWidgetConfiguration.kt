package com.cbruegg.mensaupb.appwidget

import android.content.SharedPreferences

/**
 * Configuration data for a Dish widget.
 */
data class DishesWidgetConfiguration(val restaurantId: String) {

    companion object {
        private val SUFFIX_ID = "_id"

        /**
         * Retrieve the Dish widget configuration
         * from the shared preferences or return null
         * if it does not exist.
         */
        fun from(appWidgetId: Int, sharedPreferences: SharedPreferences): DishesWidgetConfiguration? {
            val restaurantId = sharedPreferences.getString("$appWidgetId$SUFFIX_ID", null) ?: return null
            return DishesWidgetConfiguration(restaurantId)
        }
    }

    /**
     * Save the data for the specified appWidgetId in the supplied editor.
     */
    fun writeTo(appWidgetId: Int, editor: SharedPreferences.Editor) {
        editor.putString("$appWidgetId$SUFFIX_ID", restaurantId)
    }
}