package com.cbruegg.mensaupb.provider

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService

/**
 * AppWidgetProvider for the dishes widget.
 */
class DishesAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context.startService(DishesWidgetUpdateService.createStartIntent(context, *appWidgetIds))
    }

}