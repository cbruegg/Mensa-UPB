package com.cbruegg.mensaupb.downloader

import android.annotation.SuppressLint
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.IOPool
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.extensions.eitherTryIo
import com.cbruegg.mensaupb.parser.parseDishes
import com.cbruegg.mensaupb.parser.parseRestaurantsFromApi
import com.cbruegg.mensaupb.util.await
import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.Restaurant
import kotlinx.coroutines.experimental.CoroutineDispatcher
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.funktionale.either.Either
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

private const val API_ID = BuildConfig.API_ID
private const val BASE_URL = "https://mensaupb.cbruegg.com"
private const val RESTAURANT_URL = BASE_URL + "/restaurants?apiId=$API_ID"
private const val TIMEOUT_MS = 10_000L

typealias IOEither<T> = Either<IOException, T>

class Downloader @Inject constructor(private val httpClient: OkHttpClient) {

    /**
     * Generate the URL used for retrieving dishes of a restaurant at a specific date.
     */
    @SuppressLint("SimpleDateFormat")
    private fun generateDishesUrl(restaurant: DbRestaurant, date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return "$BASE_URL/dishes?apiId=$API_ID&date=${dateFormat.format(date)}&restaurantId=${restaurant.id}"
    }

    fun downloadRestaurantsAsync(): Deferred<IOEither<List<Restaurant>>> = networkAsync {
        withTimeoutOrNull(TIMEOUT_MS) {
            val request = Request.Builder().url(RESTAURANT_URL).build()
            val response = httpClient.newCall(request).await()
            if (response.code() != 200) throw IOException("Server error!")

            parseRestaurantsFromApi(response.body()!!.source())
        } ?: throw IOException("Network timeout!")
    }

    fun downloadDishesAsync(restaurant: DbRestaurant, date: Date): Deferred<IOEither<List<Dish>>> = networkAsync {
        withTimeoutOrNull(TIMEOUT_MS) {
            val request = Request.Builder().url(generateDishesUrl(restaurant, date)).build()
            val response = httpClient.newCall(request).await()
            if (response.code() != 200) throw IOException("Server error!")

            parseDishes(response.body()!!.source())
        } ?: throw IOException("Network timeout!")
    }

    /**
     * Perform the action with the [dispatcher] and wrap it in [eitherTryIo].
     */
    private fun <T : Any> networkAsync(dispatcher: CoroutineDispatcher = IOPool, f: suspend () -> T): Deferred<IOEither<T>> =
            async(dispatcher) {
                eitherTryIo {
                    f()
                }
            }

}