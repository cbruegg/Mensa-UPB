package com.cbruegg.mensaupb.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import arrow.core.orNull
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.extensions.stackTraceString
import com.cbruegg.mensaupb.main.MainActivity
import com.cbruegg.mensaupb.viewmodel.dishComparator
import kotlinx.coroutines.runBlocking
import java.util.Date
import java.util.concurrent.ExecutionException
import javax.inject.Inject

/**
 * A [RemoteViewsService] that is only responsible
 * for proving dish app widgets with list items.
 */
class DishRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent) = DishRemoteViewsFactory(this, intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID))

    /**
     * Factory for the remote dish views.
     */
    class DishRemoteViewsFactory(private val ctx: Context, private val appWidgetId: Int) : RemoteViewsFactory {

        private val shownDate: Date
            get() = Date(System.currentTimeMillis() + DishesWidgetUpdateService.DATE_OFFSET).atMidnight

        @Volatile
        private var dishes = emptyList<DbDish>()
        @Volatile
        private var restaurant: DbRestaurant? = null
        @Inject
        lateinit var repository: Repository

        init {
            ctx.app.appComponent.inject(this)
        }

        override fun getLoadingView(): RemoteViews? = null // Use default

        override fun getViewAt(position: Int): RemoteViews? {
            // Android causes a race condition between updating and and getCount()
            // Returning null at least avoids an Exception - else nothing much we can do
            val dish = dishes.getOrNullIfOutOfBounds(position) ?: return null

            val thumbnailVisibility = if (dish.thumbnailImageUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            val dishIntent = Intent().apply {
                MainActivity.fillIntent(this, restaurant, dish, selectDay = shownDate)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            val remoteViews = RemoteViews(ctx.packageName, R.layout.row_dish_widget).apply {
                setTextViewText(R.id.dish_widget_name, dish.displayName())
                setViewVisibility(R.id.dish_widget_image, thumbnailVisibility)
                setOnClickFillInIntent(R.id.dish_widget_row, dishIntent)
            }

            if (!dish.thumbnailImageUrl.isNullOrEmpty()) {
                try {
                    val size = ctx.resources.getDimensionPixelSize(R.dimen.row_dish_widget_image_size)
                    val bitmap = GlideApp.with(ctx)
                        .asBitmap()
                        .load(dish.thumbnailImageUrl)
                        .centerCrop()
                        .submit(size, size)
                        .get()
                    remoteViews.setImageViewBitmap(R.id.dish_widget_image, bitmap)
                } catch (e: ExecutionException) {
                    Log.e(TAG, "Downloading '${dish.thumbnailImageUrl}' failed! Not setting an image for '${dish.name}'.")
                    Log.e(TAG, e.stackTraceString)
                    remoteViews.setViewVisibility(R.id.dish_widget_image, View.GONE)
                }
            }

            return remoteViews
        }

        override fun getViewTypeCount() = 1

        override fun onCreate() {
        }

        override fun getItemId(position: Int) = dishes.getOrNullIfOutOfBounds(position).hashCode().toLong()

        override fun onDataSetChanged() = runBlocking {
            val (restaurantId) = DishesWidgetConfigurationManager(ctx)
                .retrieveConfiguration(appWidgetId)
                ?: return@runBlocking

            val restaurant = repository.restaurants()
                .orNull()
                ?.value
                ?.firstOrNull { it -> it.id == restaurantId }
                ?: return@runBlocking
            this@DishRemoteViewsFactory.restaurant = restaurant
            dishes = repository.dishes(restaurant, shownDate)
                .orNull()
                ?.value
                ?.sortedWith(ctx.userType.dishComparator)
                    ?: return@runBlocking
        }

        override fun hasStableIds() = true

        override fun getCount() = dishes.size

        override fun onDestroy() {
        }

        private fun <T : Any> List<T>.getOrNullIfOutOfBounds(pos: Int): T? = if (pos in indices) this[pos] else null

    }
}