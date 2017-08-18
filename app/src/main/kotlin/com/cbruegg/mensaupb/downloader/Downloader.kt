package com.cbruegg.mensaupb.downloader

import android.annotation.SuppressLint
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.IOPool
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.extensions.eitherTryIo
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.parser.parseDishes
import com.cbruegg.mensaupb.parser.parseRestaurantsFromApi
import com.cbruegg.mensaupb.util.await
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
private const val BASE_URL = "http://www.studierendenwerk-pb.de/fileadmin/shareddata/access2.php?id=" + API_ID
private const val RESTAURANT_URL = BASE_URL + "&getrestaurants=1"
private const val TIMEOUT_MS = 10_000L

typealias IOEither<T> = Either<IOException, T>

class Downloader @Inject constructor(private val httpClient: OkHttpClient) {

    /**
     * Generate the URL used for retrieving dishes of a restaurant at a specific date.
     */
    @SuppressLint("SimpleDateFormat")
    private fun generateDishesUrl(restaurant: DbRestaurant, date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return "$BASE_URL&date=${dateFormat.format(date)}&restaurant=${restaurant.id}"
    }

    fun downloadRestaurantsAsync(): Deferred<IOEither<List<Restaurant>>> = networkAsync {
        withTimeoutOrNull(TIMEOUT_MS) {
            val request = Request.Builder().url(RESTAURANT_URL).build()
            val response = httpClient.newCall(request).await()
            parseRestaurantsFromApi(response.body()!!.source())
        } ?: throw IOException("Network timeout!")
    }

    fun downloadDishesAsync(restaurant: DbRestaurant, date: Date): Deferred<IOEither<List<Dish>>> = networkAsync {
        withTimeoutOrNull(TIMEOUT_MS) {
            val request = Request.Builder().url(generateDishesUrl(restaurant, date)).build()
            val response = httpClient.newCall(request).await()
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