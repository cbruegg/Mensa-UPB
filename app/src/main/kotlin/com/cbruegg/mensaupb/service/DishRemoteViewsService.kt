package com.cbruegg.mensaupb.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.model.Dish
import rx.Subscription
import rx.lang.kotlin.filterNotNull
import java.util.*

class DishRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent)
            = DishRemoteViewsFactory(this, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))

    class DishRemoteViewsFactory(private val context: Context, private val appWidgetId: Int) : RemoteViewsFactory {

        private var dishes = emptyList<Dish>()
        private var subscription: Subscription? = null

        override fun getLoadingView(): RemoteViews? = null // TODO

        override fun getViewAt(position: Int): RemoteViews? {
            val dish = dishes[position]

            val remoteViews = RemoteViews(context.packageName, R.layout.row_dish_widget)
            remoteViews.setTextViewText(R.id.dish_widget_name, dish.germanName)

            // TODO better design, intent

            return remoteViews
        }

        override fun getViewTypeCount() = 1

        override fun onCreate() {
        }

        override fun getItemId(position: Int) = position.toLong()

        override fun onDataSetChanged() {
            val (restaurantId) = DishesWidgetConfigurationManager(context).retrieveConfiguration(appWidgetId) ?: return
            val downloader = Downloader(context)

            subscription = downloader.downloadOrRetrieveRestaurants()
                    .map { it.fold({ null }, { it }) }
                    .filterNotNull()
                    .map { it.firstOrNull { restaurant -> restaurant.id == restaurantId } }
                    .flatMap { it?.let { downloader.downloadOrRetrieveDishes(it, Date()) } }
                    .filterNotNull()
                    .subscribe {
                        it.fold({}) {
                            dishes = it
                        }
                    }
        }

        override fun hasStableIds() = false

        override fun getCount() = dishes.size

        override fun onDestroy() {
            subscription?.unsubscribe()
        }

    }
}