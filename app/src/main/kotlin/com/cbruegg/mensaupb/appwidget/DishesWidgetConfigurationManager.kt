package com.cbruegg.mensaupb.appwidget

import android.content.Context

class DishesWidgetConfigurationManager(context: Context) {

    private val PREF_DISHES_WIDGET_CONFIG = "dishes_widget_config"
    private val store = context.getSharedPreferences(PREF_DISHES_WIDGET_CONFIG, Context.MODE_PRIVATE)

    fun putConfiguration(appWidgetId: Int, dishesWidgetConfiguration: DishesWidgetConfiguration) {
        store.edit().apply { dishesWidgetConfiguration.writeTo(appWidgetId, this) }.commit()
    }

    fun retrieveConfiguration(appWidgetId: Int): DishesWidgetConfiguration?
            = DishesWidgetConfiguration.from(appWidgetId, store)
}