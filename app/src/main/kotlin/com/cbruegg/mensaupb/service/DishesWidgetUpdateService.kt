package com.cbruegg.mensaupb.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.main.MainActivity
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Failure
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService.DishAppWidgetResult.Success
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import androidx.core.net.toUri

/**
 * A service that is responsible for updating
 * all dishes wuidgets.
 */
class DishesWidgetUpdateService(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        applicationContext.app.appComponent.inject(this)

        val appWidgetIds = inputData.getIntArray(ARG_APPWIDGET_IDS) ?: throw IllegalArgumentException("Missing input data!")
        val configManager = DishesWidgetConfigurationManager(applicationContext)

        val restaurantsById = repository.restaurants()
            .getOrNull()
            ?.value
            ?.associateBy { it.id }
            ?: return Result.retry()

        appWidgetIds
            .map { appWidgetId ->
                val config = configManager.retrieveConfiguration(appWidgetId)
                    ?: return@map null
                val restaurant = restaurantsById[config.restaurantId]
                    ?: return@map Failure(appWidgetId, Failure.Reason.RESTAURANT_NOT_FOUND)

                Success(appWidgetId, restaurant)
            }
            .filterNotNull()
            .forEach { updateAppWidget(it) }

        return Result.success()

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

        /**
         * Function for creating intent that start this service.
         */
        private fun createStartExtras(vararg appWidgetIds: Int): Data =
            workDataOf(ARG_APPWIDGET_IDS to appWidgetIds)

        /**
         * Schedule an update for the specified widgets within the next
         * few minutes.
         */
        fun scheduleUpdate(maxWaitTimeSeconds: Long, context: Context, vararg appWidgetIds: Int) {
            val inputData = createStartExtras(*appWidgetIds)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<DishesWidgetUpdateService>(maxWaitTimeSeconds, TimeUnit.SECONDS)
                .addTag("dishes-widget-update")
                .setConstraints(constraints)
                .setInputData(inputData)
                .build()
            val workManager = WorkManager.getInstance(context)
            workManager.beginWith(
                OneTimeWorkRequestBuilder<DishesWidgetUpdateService>()
                    .setConstraints(constraints)
                    .setInputData(inputData)
                    .build()
            ).enqueue()
            workManager.enqueueUniquePeriodicWork("dishes-widget-update", ExistingPeriodicWorkPolicy.UPDATE, workRequest)
        }

    }

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
                is Success -> {
                    appWidgetResult.restaurant
                }
                is Failure -> {
                    updateWithError(appWidgetResult)
                    return
                }
            }

        val appWidgetId = appWidgetResult.appWidgetId
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val restaurantIntent = MainActivity.createStartIntent(applicationContext, restaurant)
        restaurantIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val restaurantPendingIntent = PendingIntent.getActivity(applicationContext, REQUEST_CODE_RESTAURANT, restaurantIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

        val dishRemoteViewsServiceIntent = Intent(applicationContext, DishRemoteViewsService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = toUri(Intent.URI_INTENT_SCHEME).toUri()
        }
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            makeUnique(appWidgetId)
        }
        val dishPendingIntent = PendingIntent.getActivity(applicationContext, REQUEST_CODE_DISH, mainActivityIntent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)

        @SuppressLint("SimpleDateFormat")
        val day = SimpleDateFormat("EEE").format(shownDate)
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.app_widget_dishes).apply {
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
    private fun updateWithError(appWidgetResult: Failure) {
        val errorText = when (appWidgetResult.reason) {
            Failure.Reason.RESTAURANT_NOT_FOUND -> {
                applicationContext.getString(R.string.dish_app_widget_error_restaurant_not_found)
            }
        }

        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val remoteViews = RemoteViews(applicationContext.packageName, R.layout.app_widget_dishes)
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
    data = Uri.withAppendedPath("madandroid://widget/id/".toUri(), id.toString())
}
