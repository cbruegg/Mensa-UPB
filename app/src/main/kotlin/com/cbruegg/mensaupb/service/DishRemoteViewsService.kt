package com.cbruegg.mensaupb.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.extensions.stackTraceString
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.viewmodel.dishComparator
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.withTimeout
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * A [RemoteViewsService] that is only responsible
 * for proving dish app widgets with list items.
 */
class DishRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent)
            = DishRemoteViewsFactory(this, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))

    /**
     * Factory for the remote dish views.
     */
    class DishRemoteViewsFactory(private val ctx: Context, private val appWidgetId: Int) : RemoteViewsFactory {

        private val shownDate: Date
            get() = Date(System.currentTimeMillis() + DishesWidgetUpdateService.DATE_OFFSET)

        private val TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1)
        private var dishes = emptyList<Dish>()
        private var restaurant: Restaurant? = null
        private var job: Job? = null

        override fun getLoadingView(): RemoteViews? = null // Use default

        override fun getViewAt(position: Int): RemoteViews? {
            Log.d(TAG, "CALLED getViewAt($position)")
            val dish = dishes[position]
            val thumbnailVisibility = if (dish.thumbnailImageUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            val dishIntent = Intent().apply { MainActivity.fillIntent(this, restaurant, dish) }
            val remoteViews = RemoteViews(ctx.packageName, R.layout.row_dish_widget)
            remoteViews.setTextViewText(R.id.dish_widget_name, dish.germanName)
            remoteViews.setViewVisibility(R.id.dish_widget_image, thumbnailVisibility)
            remoteViews.setOnClickFillInIntent(R.id.dish_widget_row, dishIntent)

            if (!dish.thumbnailImageUrl.isNullOrEmpty()) {
                try {
                    val bitmap = Picasso.with(ctx)
                            .load(dish.thumbnailImageUrl)
                            .resizeDimen(R.dimen.row_dish_widget_image_size, R.dimen.row_dish_widget_image_size)
                            .onlyScaleDown()
                            .centerCrop()
                            .get()
                    remoteViews.setImageViewBitmap(R.id.dish_widget_image, bitmap)
                } catch (e: IOException) {
                    Log.e(TAG, "Downloading '${dish.thumbnailImageUrl}' failed! Not setting an image for '${dish.germanName}'.")
                    Log.e(TAG, e.stackTraceString)
                }
            }

            Log.d(TAG, "RETURNING getViewAt($position)")

            return remoteViews
        }

        override fun getViewTypeCount() = 1

        override fun onCreate() {
            Log.d(TAG, "CALLED onCreate()")
            Log.d(TAG, "RETURNED onCreate()")
        }

        override fun getItemId(position: Int) = dishes[position].hashCode().toLong()

        override fun onDataSetChanged() = runBlocking {
            Log.d(TAG, "CALLED onDataSetChanged()")

            val (restaurantId) = DishesWidgetConfigurationManager(ctx)
                    .retrieveConfiguration(appWidgetId)
                    ?: return@runBlocking
            val downloader = Downloader(ctx)

            withTimeout(TIMEOUT_MS) {
                val restaurant = downloader.downloadOrRetrieveRestaurantsAsync()
                        .also { job = it }
                        .await()
                        .component2()
                        ?.firstOrNull { (id) -> id == restaurantId }
                        ?: return@withTimeout
                dishes = downloader.downloadOrRetrieveDishesAsync(restaurant, shownDate)
                        .also { job = it }
                        .await()
                        .component2()
                        ?.sortedWith(ctx.userType.dishComparator)
                        ?: return@withTimeout
            }
            Log.d(TAG, "RETURNED onDataSetChanged()")
        }

        override fun hasStableIds() = true

        override fun getCount(): Int {
            Log.d(TAG, "CALLED getCount()")
            Log.d(TAG, "RETURNING getCount(): ${dishes.size}")
            return dishes.size
        }

        override fun onDestroy() {
            Log.d(TAG, "CALLED onDestroy()")
            job?.cancel()
        }

    }
}