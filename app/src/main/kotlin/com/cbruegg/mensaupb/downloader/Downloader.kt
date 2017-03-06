package com.cbruegg.mensaupb.downloader

import android.content.Context
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DataCache
import com.cbruegg.mensaupb.extensions.ioEither
import com.cbruegg.mensaupb.extensions.ioObservable
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.parser.parseDishes
import com.cbruegg.mensaupb.parser.parseRestaurantsFromApi
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import okhttp3.OkHttpClient
import okhttp3.Request
import org.funktionale.either.Either
import rx.Observable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

private val API_ID = BuildConfig.API_ID
private val BASE_URL = "http://www.studentenwerk-pb.de/fileadmin/shareddata/access2.php?id=" + API_ID
private val RESTAURANT_URL = BASE_URL + "&getrestaurants=1";

/**
 * Class responsible for downloading data from the API
 */
class Downloader(context: Context) {

    @Inject lateinit var dataCache: DataCache
    @Inject lateinit var httpClient: OkHttpClient

    init {
        context.app.appComponent.inject(this)
    }

    /**
     * Get a list of all restaurants.
     *
     * @param onlyActive If true, only return restaurants marked as active.
     */
    fun downloadOrRetrieveRestaurantsAsync(onlyActive: Boolean = true):
            Deferred<Either<IOException, List<Restaurant>>> = async(CommonPool) {
        ioEither {
            val request = Request.Builder().url(RESTAURANT_URL).build()
            val cachedRestaurants = dataCache.retrieveRestaurants()

            val restaurants = cachedRestaurants ?:
                    dataCache.cache(parseRestaurantsFromApi(httpClient.newCall(request).execute().body().source()))
            restaurants.filter { !onlyActive || it.isActive }
        }
    }

    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    fun downloadOrRetrieveDishesAsync(restaurant: Restaurant, date: Date):
            Deferred<Either<IOException, List<Dish>>> = async(CommonPool) {
        ioEither {
            val request = Request.Builder().url(generateDishesUrl(restaurant, date)).build()
            val cachedDishes = dataCache.retrieve(restaurant, date)
            cachedDishes ?:
                    dataCache.cache(restaurant, date, parseDishes(httpClient.newCall(request).execute().body().source()))
        }
    }

    /**
     * Generate the URL used for retrieving dishes of a restaurant at a specific date.
     */
    private fun generateDishesUrl(restaurant: Restaurant, date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return "$BASE_URL&date=${dateFormat.format(date)}&restaurant=${restaurant.id}"
    }
}