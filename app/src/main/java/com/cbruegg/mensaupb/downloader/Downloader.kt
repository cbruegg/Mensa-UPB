package com.cbruegg.mensaupb.downloader

import android.content.Context
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.cache.DataCache
import com.cbruegg.mensaupb.extensions.ioObservable
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.parser.parseDishes
import com.cbruegg.mensaupb.parser.parseRestaurants
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import org.funktionale.either.Either
import rx.Observable
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private val API_ID = BuildConfig.API_ID
private val BASE_URL = "http://www.studentenwerk-pb.de/fileadmin/shareddata/access2.php?id=" + API_ID
private val RESTAURANT_URL = BASE_URL + "&getrestaurants=1";

/**
 * Class responsible for downloading data from the API
 */
class Downloader(context: Context) {

    private val dataCache = DataCache.getInstance(context)

    /**
     * Get a list of all restaurants.
     */
    public fun downloadRestaurants(): Observable<Either<IOException, List<Restaurant>>> {
        val httpClient = OkHttpClient()
        val request = Request.Builder().url(RESTAURANT_URL).build()
        return ioObservable {
            it.onNext(parseRestaurants(httpClient.newCall(request).execute().body().string()))
            it.onCompleted()
        }
    }

    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    public fun downloadOrRetrieveDishes(restaurant: Restaurant, date: Date): Observable<Either<IOException, List<Dish>>> {
        val httpClient = OkHttpClient()
        val request = Request.Builder().url(generateDishesUrl(restaurant, date)).build()
        return ioObservable {
            val cachedDishes = dataCache.retrieve(restaurant, date)
            it.onNext(cachedDishes ?: dataCache.cache(restaurant, date, parseDishes(httpClient.newCall(request).execute().body().string())))
            it.onCompleted()
        }
    }

    /**
     * Generate the URL used for retrieving dishes of a restaurant at a specific date.
     */
    private fun generateDishesUrl(restaurant: Restaurant, date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return BASE_URL + "&date=" + dateFormat.format(date) + "&restaurant=" + restaurant.id
    }
}