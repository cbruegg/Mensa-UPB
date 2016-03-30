package com.cbruegg.mensaupb.appwidget

import android.content.SharedPreferences

data class DishesWidgetConfiguration(val restaurantId: String) {

    companion object {
        private val SUFFIX_ID = "_id"

        fun from(appWidgetId: Int, sharedPreferences: SharedPreferences): DishesWidgetConfiguration? {
            val restaurantId = sharedPreferences.getString("$appWidgetId$SUFFIX_ID", null) ?: return null
            return DishesWidgetConfiguration(restaurantId)
        }
    }

    fun writeTo(appWidgetId: Int, editor: SharedPreferences.Editor) {
        editor.apply {
            putString("$appWidgetId$SUFFIX_ID", restaurantId)
        }
    }
}