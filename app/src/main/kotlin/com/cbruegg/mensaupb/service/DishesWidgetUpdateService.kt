package com.cbruegg.mensaupb.service

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.RemoteViews
import arrow.core.orNull
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.main.MainActivity
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Failure
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Success
import com.firebase.jobdispatcher.Constraint
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.Trigger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * A service that is responsible for updating
 * all dishes widgets.
 */
class DishesWidgetUpdateService : JobService() {

    override fun onStopJob(params: JobParameters): Boolean {
        val job = job ?: return false
        val isActive = job.isActive
        job.cancel()
        return isActive
    }

    override fun onStartJob(params: JobParameters): Boolean {
        app.appComponent.inject(this)

        val appWidgetIds = params.extras?.getIntArray(ARG_APPWIDGET_IDS) ?: throw IllegalArgumentException("Missing extras!")
        val configManager = DishesWidgetConfigurationManager(this@DishesWidgetUpdateService)

        job = GlobalScope.launch(Dispatchers.Main) {
            val reschedule = withTimeout(TIMEOUT_MS) {
                val restaurantsById = repository.restaurantsAsync()
                    .await()
                    .orNull()
                    ?.value
                    ?.associateBy { it.id }
                    ?: return@withTimeout true

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

                return@withTimeout false
            }
            jobFinished(params, reschedule)
        }

        return job?.isActive == true
    }

    /**
     * Case class for app widget content results.
     *
     * @see Failure
     * @see Success
     */
    private sealed class DishAppWidgetResult(open val appWidgetId: Int) {
        data class Success(override val appWidgetId: Int, val restaurant: DbRestaurant) : DishAppWidgetResult(appWidgetId)
        data class Failure(override val appWidgetId: Int, val reason: Reason) : DishAppWidgetResult(appWidgetId) {
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
        private fun createStartExtras(vararg appWidgetIds: Int): Bundle =
            Bundle().apply {
                putIntArray(ARG_APPWIDGET_IDS, appWidgetIds)
            }

        /**
         * Schedule an update for the specified widgets within the next
         * few minutes.
         */
        fun scheduleUpdate(context: Context, maxWaitTimeSeconds: Int, vararg appWidgetIds: Int) {
            val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
            val job = dispatcher.newJobBuilder()
                .setService(DishesWidgetUpdateService::class.java)
                .setTag("dishes-widget-update")
                .setTrigger(Trigger.executionWindow(0, maxWaitTimeSeconds))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setExtras(DishesWidgetUpdateService.createStartExtras(*appWidgetIds))
                .build()
            dispatcher.mustSchedule(job)
        }

    }

    private var job: Job? = null
    private val shownDate: Date
        get() = Date(System.currentTimeMillis() + DATE_OFFSET + INTERNAL_DATE_OFFSET)

    @Inject
    lateinit var repository: Repository

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
                    updateWithError(appWidgetResult)
                    return
                }
            }

        val appWidgetId = appWidgetResult.appWidgetId
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val restaurantIntent = MainActivity.createStartIntent(this, restaurant)
        restaurantIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val restaurantPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_RESTAURANT, restaurantIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val dishRemoteViewsServiceIntent = Intent(this, DishRemoteViewsService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        val mainActivityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            makeUnique(appWidgetId)
        }
        val dishPendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_DISH, mainActivityIntent, PendingIntent.FLAG_CANCEL_CURRENT)

        val day = SimpleDateFormat("EEE").format(shownDate)
        val remoteViews = RemoteViews(packageName, R.layout.app_widget_dishes).apply {
            setOnClickPendingIntent(R.id.dishes_widget_restaurant_name, restaurantPendingIntent)
            setTextViewText(R.id.dishes_widget_restaurant_name, "${restaurant.name} ($day)")
            setRemoteAdapter(R.id.dishes_widget_list, dishRemoteViewsServiceIntent)
            setPendingIntentTemplate(R.id.dishes_widget_list, dishPendingIntent)
            setEmptyView(R.id.dishes_widget_list, R.id.dishes_widget_empty_view)
        }

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
