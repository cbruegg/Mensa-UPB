package com.cbruegg.mensaupb.downloader

import android.content.Context
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.*
import com.cbruegg.mensaupb.util.AllOpen
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.Unconfined
import kotlinx.coroutines.experimental.async
import org.funktionale.either.Either
import org.funktionale.either.RightProjection
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Class responsible for downloading data from the API
 */
@AllOpen class Repository @Deprecated("Inject this.") constructor(context: Context) {

    @Inject lateinit var modelCache: ModelCache
    @Inject lateinit var downloader: Downloader

    init {
        context.app.appComponent.inject(this)
    }

    /**
     * Get a list of all restaurants.
     *
     * @param onlyActive If true, only return restaurants marked as active.
     */
    suspend fun restaurantsAsync(onlyActive: Boolean = true, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbRestaurant>>>> = async(Unconfined) {
        val restaurants = tryStale(acceptStale, { modelCache.retrieveRestaurants(acceptStale).await() }) {
            val restaurants = downloader.downloadRestaurantsAsync().await()
            restaurants.right().mapSuspend { modelCache.cache(it).await() }
        }
        restaurants.right().map { it.copy(value = it.value.filter { !onlyActive || it.isActive }) }
    }

    // TODO Can this be made reactive by extending LiveData? Should it?
    /**
     * Get a list of all dishes in a restaurant at the specified date. The list might be empty.
     */
    suspend fun dishesAsync(restaurant: DbRestaurant, date: Date, acceptStale: Boolean = false):
            Deferred<IOEither<Stale<List<DbDish>>>> = async(Unconfined) {
        tryStale(acceptStale, { modelCache.retrieve(restaurant, date, acceptStale).await() }) {
            val dishes = downloader.downloadDishesAsync(restaurant, date).await()
            dishes.right().mapSuspend { modelCache.cache(restaurant, date, it).await() }
        }
    }

    private suspend fun <L, R, X> RightProjection<L, R>.mapSuspend(f: suspend (R) -> X): Either<L, X> =
            if (e.isLeft()) {
                Either.Left<L, X>(e.left().get())
            } else {
                Either.Right<L, X>(f(e.right().get()))
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
                    Either.Left<IOException, Stale<T>>(e)
                } else {
                    Either.Right<IOException, Stale<T>>(cached)
                }
            }) {
                Either.Right<IOException, Stale<T>>(it.toNonStale())
            }
        } else Either.Right(cached)
    }

}