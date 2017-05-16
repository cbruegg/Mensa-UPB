package com.cbruegg.mensaupb.downloader

import android.annotation.SuppressLint
import android.content.Context
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.*
import com.cbruegg.mensaupb.extensions.eitherTryIo
import com.cbruegg.mensaupb.parser.parseDishes
import com.cbruegg.mensaupb.parser.parseRestaurantsFromApi
import com.cbruegg.mensaupb.util.AllOpen
import kotlinx.coroutines.experimental.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.funktionale.either.Either
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

typealias IOEither<T> = Either<IOException, T>

private const val API_ID = BuildConfig.API_ID
private const val BASE_URL = "http://www.studentenwerk-pb.de/fileadmin/shareddata/access2.php?id=" + API_ID
private const val RESTAURANT_URL = BASE_URL + "&getrestaurants=1"
private const val TIMEOUT_MS = 10_000L

/**
 * Class responsible for downloading data from the API
 */
@AllOpen class Downloader @Deprecated("Inject this.") constructor(context: Context) {

    @Inject lateinit var modelCache: ModelCache
    @Inject lateinit var httpClient: OkHttpClient

    init {
        context.app.appComponent.inject(this)
    }

    /**
     * Get a list of all restaurants.
     *
     * @param onlyActive If true, only return restaurants marked as active.
     */
    fun downloadOrRetrieveRestaurantsAsync(onlyActive: Boolean = true, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbRestaurant>>>> = networkAsync {
        val restaurants = tryStale(acceptStale, { modelCache.retrieveRestaurants(acceptStale).await() }) {
            val restaurants = withTimeout(TIMEOUT_MS) {
                val request = Request.Builder().url(RESTAURANT_URL).build()
                parseRestaurantsFromApi(httpClient.newCall(request).execute().body().source())
            }
            modelCache.cache(restaurants).await()
        }
        restaurants.copy(value = restaurants.value.filter { !onlyActive || it.isActive })
    }

    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    fun downloadOrRetrieveDishesAsync(restaurant: DbRestaurant, date: Date, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbDish>>>> = networkAsync {
        tryStale(acceptStale, { modelCache.retrieve(restaurant, date, acceptStale).await() }) {
            val request = Request.Builder().url(generateDishesUrl(restaurant, date)).build()
            val dishes = withTimeout(TIMEOUT_MS) {
                parseDishes(httpClient.newCall(request).execute().body().source())
            }
            modelCache.cache(restaurant, date, dishes).await()
        }
    }

    private suspend fun <T : Any> tryStale(
            acceptStale: Boolean,
            cacheRetriever: suspend () -> Stale<T>?,
            downloadAndCache: suspend () -> T
    ): Stale<T> {
        val cached = cacheRetriever()
        return if (cached == null || cached.isStale) {
            try {
                withTimeout(TIMEOUT_MS) {
                    downloadAndCache()
                }.toNonStale()
            } catch (e: Exception) {
                if (!acceptStale || cached == null || e !is CancellationException && e !is IOException) throw e
                else cached
            }
        } else cached
    }

    /**
     * Perform the action with the [dispatcher] and wrap it in [eitherTryIo].
     */
    private fun <T : Any> networkAsync(dispatcher: CoroutineDispatcher = CommonPool, f: suspend () -> T): Deferred<IOEither<T>> = // TODO create proper coroutine w/ okhttp callbacks
            async(dispatcher) {
                eitherTryIo {
                    f()
                }
            }

    /**
     * Generate the URL used for retrieving dishes of a restaurant at a specific date.
     */
    @SuppressLint("SimpleDateFormat")
    private fun generateDishesUrl(restaurant: DbRestaurant, date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return "$BASE_URL&date=${dateFormat.format(date)}&restaurant=${restaurant.id}"
    }
}