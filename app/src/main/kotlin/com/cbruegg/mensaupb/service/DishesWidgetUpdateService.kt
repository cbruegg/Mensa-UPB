package com.cbruegg.mensaupb.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.extensions.filterRight
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Failure
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Success
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.filterNotNull
import rx.lang.kotlin.onError
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * A service that is responsible for updating
 * all dishes widgets.
 */
class DishesWidgetUpdateService : Service() {

    /**
     * Case class for app widget content results.
     *
     * @see Failure
     * @see Success
     */
    private sealed class DishAppWidgetResult(val appWidgetId: Int) {
        class Success(appWidgetId: Int, val restaurant: Restaurant) : DishAppWidgetResult(appWidgetId)
        class Failure(appWidgetId: Int, val reason: Reason) : DishAppWidgetResult(appWidgetId) {
            enum class Reason {
                RESTAURANT_NOT_FOUND
            }
        }

    }

    companion object {
        private const val REQUEST_CODE_RESTAURANT = 0
        private const val REQUEST_CODE_DISH = 1
        private const val ARG_APPWIDGET_IDS = "app_widget_ids"

        private val TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1)

        /**
         * Function for creating intent that start this service.
         */
        fun createStartIntent(context: Context, vararg appWidgetIds: Int): Intent =
                Intent(context, DishesWidgetUpdateService::class.java).apply {
                    putExtra(ARG_APPWIDGET_IDS, appWidgetIds)
                }

    }

    private var subscription: Subscription? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        val downloader = Downloader(this)
        val appWidgetIds = intent.getIntArrayExtra(ARG_APPWIDGET_IDS)
        val configManager = DishesWidgetConfigurationManager(this)

        subscription = downloader.downloadOrRetrieveRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filterRight() // On errors, do nothing
                .map { it.associate { it.id to it } } // Map by id
                .flatMapIterable { restaurantsById ->
                    appWidgetIds.map { appWidgetId ->
                        // When no config exists, just don't update this widget
                        // (null is filtered out later)
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
                .subscribe {
                    updateAppWidget(it)
                    stopSelf()
                }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBInd")
        return null
    }

    /**
     * If the result is successful, update the remote views
     * with the result data. If the result is not successful,
     * [updateWithError] is called internally.
     */
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
        restaurantIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val restaurantPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_RESTAURANT, restaurantIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val dishRemoteViewsServiceIntent = Intent(this, DishRemoteViewsService::class.java)
        dishRemoteViewsServiceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        dishRemoteViewsServiceIntent.data = Uri.parse(dishRemoteViewsServiceIntent.toUri(Intent.URI_INTENT_SCHEME))
        val mainActivityIntent = Intent(this, MainActivity::class.java)
        mainActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val dishPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_DISH, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val remoteViews = RemoteViews(packageName, R.layout.app_widget_dishes)
        remoteViews.setOnClickPendingIntent(R.id.dishes_widget_restaurant_name, restaurantPendingIntent)
        remoteViews.setTextViewText(R.id.dishes_widget_restaurant_name, restaurant.name)
        remoteViews.setRemoteAdapter(R.id.dishes_widget_list, dishRemoteViewsServiceIntent)
        remoteViews.setPendingIntentTemplate(R.id.dishes_widget_list, dishPendingIntent)
        remoteViews.setEmptyView(R.id.dishes_widget_list, R.id.dishes_widget_empty_view)

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
    }

    /**
     * Set an error message in the remote view.
     */
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
        Log.d(TAG, "onDestroy")
        subscription?.unsubscribe()
        super.onDestroy()
    }

}