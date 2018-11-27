package com.cbruegg.mensaupb.downloader

import android.content.Context
import arrow.core.Either
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.cache.ModelCache
import com.cbruegg.mensaupb.cache.Stale
import com.cbruegg.mensaupb.cache.mapValue
import com.cbruegg.mensaupb.util.AllOpen
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
    suspend fun restaurants(onlyActive: Boolean = true, acceptStale: Boolean = false): IOEither<Stale<List<DbRestaurant>>> {
        val restaurantsEither = tryStale(acceptStale, { modelCache.retrieveRestaurants(acceptStale) }) {
            val restaurants = downloader.downloadRestaurants()
            restaurants.mapRightSuspend { modelCache.cache(it) }
        }
        return restaurantsEither.map { restaurantsStale ->
            restaurantsStale.mapValue { restaurants ->
                restaurants.filter { restaurant -> !onlyActive || restaurant.isActive }
            }
        }
    }

    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    suspend fun dishes(restaurant: DbRestaurant, date: Date, acceptStale: Boolean = false): IOEither<Stale<List<DbDish>>> {
        return tryStale(acceptStale, { modelCache.retrieve(restaurant, date, acceptStale) }) {
            val dishes = downloader.downloadDishes(restaurant, date)
            dishes.mapRightSuspend { modelCache.cache(restaurant, date, it) }
        }
    }

    private inline fun <L, R, X> Either<L, R>.mapRightSuspend(f: (R) -> X): Either<L, X> =
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
                Either.Right(Stale(it, isStale = false))
            }
        } else Either.Right(cached)
    }

}