package com.cbruegg.mensaupb.downloader

import android.content.Context
import arrow.core.Either
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.cache.Stale
import com.cbruegg.mensaupb.cache.toNonStale
import com.cbruegg.mensaupb.util.AllOpen
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.Date
import javax.inject.Inject

/**
 * Class responsible for downloading data from the API
 */
@AllOpen
class Repository @Deprecated("Inject this.") constructor(context: Context) {

    @Inject
    lateinit var modelCache: ModelCache
    @Inject
    lateinit var downloader: Downloader

    init {
        context.app.appComponent.inject(this)
    }

    /**
     * Get a list of all restaurants.
     *
     * @param onlyActive If true, only return restaurants marked as active.
     */
    suspend fun restaurantsAsync(onlyActive: Boolean = true, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbRestaurant>>>> = GlobalScope.async {
        val restaurants = tryStale(acceptStale, { modelCache.retrieveRestaurants(acceptStale).await() }) {
            val restaurants = downloader.downloadRestaurantsAsync().await()
            restaurants.mapRightSuspend { modelCache.cache(it).await() }
        }
        restaurants.map { it.copy(value = it.value.filter { !onlyActive || it.isActive }) }
    }

    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    suspend fun dishesAsync(restaurant: DbRestaurant, date: Date, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbDish>>>> = GlobalScope.async {
        tryStale(acceptStale, { modelCache.retrieve(restaurant, date, acceptStale).await() }) {
            val dishes = downloader.downloadDishesAsync(restaurant, date).await()
            dishes.mapRightSuspend { modelCache.cache(restaurant, date, it).await() }
        }
    }

    private suspend inline fun <L, R, X> Either<L, R>.mapRightSuspend(f: (R) -> X): Either<L, X> =
        when (this) {
            is Either.Left -> Either.left(a)
            is Either.Right -> Either.right(f(b))
        }

    private suspend fun <T : Any> tryStale(
        acceptStale: Boolean,
        cacheRetriever: suspend () -> Stale<T>?,
        downloadAndCache: suspend () -> IOEither<T>
    ): IOEither<Stale<T>> {
        val cached = cacheRetriever()
        return if (cached == null || cached.isStale) {
            downloadAndCache().fold({ e ->
                if (!acceptStale || cached == null) {
                    Either.left(e)
                } else {
                    Either.right(cached)
                }
            }) {
                Either.Right(it.toNonStale())
            }
        } else Either.Right(cached)
    }

}