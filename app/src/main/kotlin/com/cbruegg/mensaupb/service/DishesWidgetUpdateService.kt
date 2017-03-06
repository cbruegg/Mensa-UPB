package com.cbruegg.mensaupb.service

import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.widget.RemoteViews
import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Failure
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Success
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withTimeout
import java.text.SimpleDateFormat
import java.util.*
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
    sealed class DishAppWidgetResult(val appWidgetId: Int) {
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

        /**
         * Show the dishes for the current date + this offset (in ms).
         * This is useful since we don't want to show the dishes for the
         * current day at 22:00.
         */
        val DATE_OFFSET = TimeUnit.HOURS.toMillis(2)

        /**
         * In addition to the [DATE_OFFSET], add this
         * to the current Date to obtain the text that represents
         * the displayed date on the widget. This is used to account
         * for the delay introduced by the network, since the actual
         * dish data is fetched later in [DishRemoteViewsService] and
         * we don't want to show "TUE" when the [DishRemoteViewsService]
         * actually already fetches data for "WED".
         */
        private val INTERNAL_DATE_OFFSET = TimeUnit.MINUTES.toMillis(5)

        private val TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1)

        /**
         * Function for creating intent that start this service.
         */
        fun createStartIntent(context: Context, vararg appWidgetIds: Int): Intent =
                Intent(context, DishesWidgetUpdateService::class.java).apply {
                    putExtra(ARG_APPWIDGET_IDS, appWidgetIds)
                }

    }

    private var job: Job? = null
    private val shownDate: Date
        get() = Date(System.currentTimeMillis() + DATE_OFFSET + INTERNAL_DATE_OFFSET)

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val downloader = Downloader(this@DishesWidgetUpdateService)
        val appWidgetIds = intent.getIntArrayExtra(ARG_APPWIDGET_IDS)
        val configManager = DishesWidgetConfigurationManager(this@DishesWidgetUpdateService)

        job = launch(MainThread) {
            withTimeout(TIMEOUT_MS) {
                val restaurantsById = downloader.downloadOrRetrieveRestaurantsAsync()
                        .await()
                        .component2()
                        ?.associateBy { it.id }
                        ?: return@withTimeout

                appWidgetIds
                        .map { appWidgetId ->
                            val config = configManager.retrieveConfiguration(appWidgetId)
                                    ?: return@map null
                            val restaurant = restaurantsById[config.restaurantId]
                                    ?: return@map DishAppWidgetResult.Failure(appWidgetId, DishAppWidgetResult.Failure.Reason.RESTAURANT_NOT_FOUND)

                            DishAppWidgetResult.Success(appWidgetId, restaurant)
                        }
                        .filterNotNull()
                        .forEach { updateAppWidget(it) }
                stopSelf()
            }
        }

        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
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
        mainActivityIntent.makeUnique(appWidgetId)
        val dishPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_DISH, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val day = SimpleDateFormat("EEE").format(shownDate)
        val remoteViews = RemoteViews(packageName, R.layout.app_widget_dishes)
        remoteViews.setOnClickPendingIntent(R.id.dishes_widget_restaurant_name, restaurantPendingIntent)
        remoteViews.setTextViewText(R.id.dishes_widget_restaurant_name, "${restaurant.name} ($day)")
        remoteViews.setRemoteAdapter(R.id.dishes_widget_list, dishRemoteViewsServiceIntent)
        remoteViews.setPendingIntentTemplate(R.id.dishes_widget_list, dishPendingIntent)
        remoteViews.setEmptyView(R.id.dishes_widget_list, R.id.dishes_widget_empty_view)

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.dishes_widget_list)
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
        job?.cancel()
        super.onDestroy()
    }

}

/**
 * To avoid PendingIntents being collapsed when they're actually
 * different, use this method. WARNING: This overrides the data Uri!
 *
 * See also http://stackoverflow.com/questions/4011178/multiple-instances-of-widget-only-updating-last-widget.
 */
private fun Intent.makeUnique(id: Int) {
    data = Uri.withAppendedPath(Uri.parse("madandroid://widget/id/"), id.toString())
}
