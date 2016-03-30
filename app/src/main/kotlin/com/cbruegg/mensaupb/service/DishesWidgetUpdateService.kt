package com.cbruegg.mensaupb.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.widget.RemoteViews
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.filterRight
import com.cbruegg.mensaupb.model.Restaurant
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.onError
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class DishesWidgetUpdateService : Service() {

    sealed class DishAppWidgetResult(val appWidgetId: Int) {
        class Success(appWidgetId: Int, val restaurant: Restaurant) : DishAppWidgetResult(appWidgetId)
        class Failure(appWidgetId: Int, val reason: Reason) : DishAppWidgetResult(appWidgetId) {
            enum class Reason {
                RESTAURANT_NOT_FOUND
            }
        }

    }

    companion object {
        private const val ARG_APPWIDGET_IDS = "app_widget_ids"
        private const val REQUEST_CODE_MAIN_ACTIVITY = 0

        private val TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1)
        fun createStartIntent(context: Context, vararg appWidgetIds: Int): Intent =
                Intent(context, DishesWidgetUpdateService::class.java).apply {
                    putExtra(ARG_APPWIDGET_IDS, appWidgetIds)
                }

    }

    private var subscription: Subscription? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val downloader = Downloader(this)
        val appWidgetIds = intent.getIntArrayExtra(ARG_APPWIDGET_IDS)
        val configManager = DishesWidgetConfigurationManager(this)

        subscription = downloader.downloadOrRetrieveRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filterRight()
                .map { it.associate { it.id to it } } // Map by id
                .flatMapIterable { restaurantsById ->
                    appWidgetIds.map { appWidgetId ->
                        val config = configManager.retrieveConfiguration(appWidgetId)
                                ?: return@map null
                        val restaurant = restaurantsById[config.restaurantId]
                                ?: return@map DishAppWidgetResult.Failure(appWidgetId, DishAppWidgetResult.Failure.Reason.RESTAURANT_NOT_FOUND)

                        return@map DishAppWidgetResult.Success(appWidgetId, restaurant)
                    }
                }
                .filterNotNull()
                .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .onError { stopSelf() }
                .doOnCompleted { stopSelf() }
                .subscribe {
                    updateAppWidget(it)
                }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun updateAppWidget(appWidgetResult: DishAppWidgetResult) {
        val restaurant =
                when (appWidgetResult) {
                    is DishAppWidgetResult.Success -> {
                        appWidgetResult.restaurant
                    }
                    is DishAppWidgetResult.Failure -> {
                        return updateWithError(appWidgetResult)
                    }
                }

        val appWidgetId = appWidgetResult.appWidgetId
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val restaurantIntent = MainActivity.createStartIntent(this, restaurant)
        val restaurantPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_MAIN_ACTIVITY, restaurantIntent, 0)

        val dishRemoteViewsServiceIntent = Intent(this, DishRemoteViewsService::class.java)
        dishRemoteViewsServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        dishRemoteViewsServiceIntent.data = Uri.parse(dishRemoteViewsServiceIntent.toUri(Intent.URI_INTENT_SCHEME))
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val dishPendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val remoteViews = RemoteViews(packageName, R.layout.app_widget_dishes)
        remoteViews.setOnClickPendingIntent(R.id.dishes_widget_restaurant_name, restaurantPendingIntent)
        remoteViews.setTextViewText(R.id.dishes_widget_restaurant_name, restaurant.name)
        remoteViews.setRemoteAdapter(R.id.dishes_widget_list, dishRemoteViewsServiceIntent)
        remoteViews.setPendingIntentTemplate(R.id.dishes_widget_list, dishPendingIntent)
        remoteViews.setEmptyView(R.id.dishes_widget_list, R.id.dishes_widget_empty_view)

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    private fun updateWithError(appWidgetResult: DishAppWidgetResult.Failure) {
        val errorText = when (appWidgetResult.reason) {
            DishAppWidgetResult.Failure.Reason.RESTAURANT_NOT_FOUND -> {
                getString(R.string.dish_app_widget_error_restaurant_not_found)
            }
        }

        val appWidgetManager = AppWidgetManager.getInstance(this)
        val remoteViews = RemoteViews(packageName, R.layout.app_widget_dishes)
        remoteViews.setTextViewText(R.id.dishes_widget_restaurant_name, errorText)
        appWidgetManager.updateAppWidget(appWidgetResult.appWidgetId, remoteViews)
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}