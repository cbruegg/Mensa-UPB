package com.cbruegg.mensaupb.appwidget

import android.content.Context

/**
 * Wrapper for a shared preferences instance
 * specific for storing [DishesWidgetConfiguration]s
 * for app widget instances.
 */
class DishesWidgetConfigurationManager(context: Context) {

    private val store = context.getSharedPreferences("dishes_widget_config", Context.MODE_PRIVATE)

    /**
     * Save a [DishesWidgetConfiguration] for an app widget.
     */
    fun putConfiguration(appWidgetId: Int, dishesWidgetConfiguration: DishesWidgetConfiguration) {
        store.edit().apply { dishesWidgetConfiguration.writeTo(appWidgetId, this) }.apply()
    }

    /**
     * @see [DishesWidgetConfiguration.Companion.from]
     */
    fun retrieveConfiguration(appWidgetId: Int): DishesWidgetConfiguration? = DishesWidgetConfiguration.from(appWidgetId, store)
}