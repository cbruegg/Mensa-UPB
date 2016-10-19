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

    override fun onEnabled(context: Context?) {
        super.onEnabled(context)
        Log.d(TAG, "onEnabled")
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        Log.d(TAG, "onReceive")
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        Log.d(TAG, "onUpdate")
        context.startService(DishesWidgetUpdateService.createStartIntent(context, *appWidgetIds))
    }

}