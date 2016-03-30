package com.cbruegg.mensaupb.service

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.MainActivity
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.filterRight
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.viewmodel.dishComparator
import com.squareup.picasso.Picasso
import org.funktionale.either.Either
import rx.Subscription
import rx.lang.kotlin.filterNotNull
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
    class DishRemoteViewsFactory(private val context: Context, private val appWidgetId: Int) : RemoteViewsFactory {

        private val TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1)
        private var dishes = emptyList<Dish>()
        private var restaurant: Restaurant? = null
        private var subscription: Subscription? = null

        override fun getLoadingView(): RemoteViews? = null // Use default

        override fun getViewAt(position: Int): RemoteViews? {
            val dish = dishes[position]
            val thumbnailVisibility = if (dish.thumbnailImageUrl.isNullOrEmpty()) View.GONE else View.VISIBLE

            val dishIntent = Intent().apply { MainActivity.fillIntent(this, restaurant, dish) }
            val remoteViews = RemoteViews(context.packageName, R.layout.row_dish_widget)
            remoteViews.setTextViewText(R.id.dish_widget_name, dish.germanName)
            remoteViews.setViewVisibility(R.id.dish_widget_image, thumbnailVisibility)
            remoteViews.setOnClickFillInIntent(R.id.dish_widget_row, dishIntent)

            if (!dish.thumbnailImageUrl.isNullOrEmpty()) {
                val bitmap = Picasso.with(context)
                        .load(dish.thumbnailImageUrl)
                        .resizeDimen(R.dimen.row_dish_widget_image_size, R.dimen.row_dish_widget_image_size)
                        .onlyScaleDown()
                        .centerCrop()
                        .get()
                remoteViews.setImageViewBitmap(R.id.dish_widget_image, bitmap)
            }

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
                    .filterRight() // Just do nothing on errors
                    .map {
                        it.firstOrNull { restaurant -> restaurant.id == restaurantId }
                                ?.apply { restaurant = this }
                    }
                    .flatMap { it?.let { downloader.downloadOrRetrieveDishes(it, Date()) } }
                    .filterNotNull()
                    .timeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .onErrorReturn { Either.Left(IOException("Timeout.")) }
                    .subscribe {
                        it.fold({}) {
                            dishes = it.sortedWith(context.userType.dishComparator)
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