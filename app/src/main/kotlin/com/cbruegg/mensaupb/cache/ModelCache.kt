package com.cbruegg.mensaupb.cache

import android.content.Context
import android.util.Log
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.DbThread
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.extensions.*
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.util.*
import javax.inject.Inject

@Suppress("SimplifyBooleanWithConstants")
private val debugIgnoreCache = false && BuildConfig.DEBUG

/**
 * If any cache entry is older than this, don't return it by default.
 */
val oldestAllowedCacheDate: Date
    get() = if (debugIgnoreCache) now else now - 4 * 60 * 60 * 1000

/**
 * If any cache entry is older than this, discard it.
 */
val oldestAllowedStaleCacheDate: Date
    get() = now - 2L * 24 * 60 * 60 * 1000

/**
 * Class responsible for caching data used by the app.
 */
class ModelCache @Deprecated("Inject this.") constructor(context: Context) {

    @Inject lateinit var data: BlockingEntityStore<Persistable>

    init {
        context.app.appComponent.inject(this)
        cleanUp()
    }

    /**
     * Delete entries in all caches older than the current date.
     */
    private fun cleanUp() {
        val threshold = oldestAllowedStaleCacheDate
        launch(DbThread) {
            data.withTransaction {
                // Restaurants don't need to be cleaned here since they are cleaned on every update in cache(restaurants)
                delete(DbRestaurantCacheEntry::class)
                        .where(DbRestaurantCacheEntryEntity.LAST_UPDATE lt threshold)
                        .get()
                        .call()
                delete(DbDish::class)
                        .where()
                        .notExists(
                                select(DbRestaurantCacheEntry::class)
                                        .where(DbRestaurantCacheEntryEntity.LAST_UPDATE gte threshold)
                                        .and(DbRestaurantCacheEntryEntity.RESTAURANT eq DbDishEntity.RESTAURANT)
                                        .and(DbRestaurantCacheEntryEntity.DISHES_FOR_DATE eq DbDishEntity.DATE)
                        )
                        .get()
                        .call()
            }
        }
    }

    /**
     * Cache the list of restaurants and return the list of DB entries.
     */
    fun cache(restaurants: List<Restaurant>): Deferred<List<DbRestaurant>> = async(DbThread) {
        Log.d(TAG, "Caching new list of restaurants.")
        restaurants.map { (id, name, location, isActive) ->
            DbRestaurantEntity().apply {
                setId(id)
                setActive(isActive)
                setLocation(location)
                setName(name)
            }
        }.also {
            data.withTransaction {
                // Don't just delete everything and re-insert here,
                // otherwise dishes will be deleted (cascade)
                delete(DbRestaurant::class)
                        .where(DbRestaurantEntity.ID notIn restaurants.map { it.id })
                        .get()
                        .call()
                upsert(it)
                upsert(DbRestaurantListCacheMetaEntity().apply { setLastUpdate(now) })
            }
        }
    }

    /**
     * Retrieve the list of cached restaurants.
     */
    fun retrieveRestaurants(acceptStale: Boolean = false): Deferred<Stale<List<DbRestaurant>>?> = async(DbThread) {
        data {
            val threshold = if (acceptStale) oldestAllowedStaleCacheDate else oldestAllowedCacheDate
            val lastUpdate = select(DbRestaurantListCacheMetaEntity::class)
                    .where(DbRestaurantListCacheMetaEntity.LAST_UPDATE gte threshold)
                    .get()
                    .firstOrNull()
                    ?.lastUpdate ?: Date(0)
            val stale = lastUpdate < oldestAllowedCacheDate
            val result = if (!stale || acceptStale && lastUpdate >= oldestAllowedStaleCacheDate) {
                Stale(select(DbRestaurantEntity::class).get().toList(), stale)
            } else null

            Log.d(TAG, "retrieveRestaurants(): ${if (result != null) "HIT, isStale: $stale" else "MISS"}")

            result
        }
    }

    /**
     * Put the dishes of the restaurant into the cache.
     * Only the day, month and year of the date are used.
     * @return The original dish list
     */
    fun cache(restaurant: DbRestaurant, date: Date, dishes: List<Dish>): Deferred<List<DbDish>> = async(DbThread) {
        val dateAtMidnight = date.atMidnight
        val dbDishes = dishes.toDbDishes(restaurant)
        Log.d(TAG, "Storing dishes for ${restaurant.id} and date $dateAtMidnight")

        data.withTransaction {
            delete(DbDishEntity::class)
                    .where(DbDishEntity.DATE eq dateAtMidnight)
                    .and(DbDishEntity.RESTAURANT eq restaurant)
                    .get()
                    .call()
            insert(dbDishes)
            delete(DbRestaurantCacheEntry::class)
                    .where(DbRestaurantCacheEntryEntity.DISHES_FOR_DATE eq dateAtMidnight)
                    .and(DbRestaurantCacheEntryEntity.RESTAURANT eq restaurant)
                    .get()
                    .call()
            insert(DbRestaurantCacheEntryEntity().apply {
                setDishesForDate(dateAtMidnight)
                setRestaurant(restaurant)
                setLastUpdate(now)
            })
        }
        dbDishes
    }

    /**
     * Try to retrieve dishes for the restaurant at the specified date.
     * Only the day, month and year of the date are used.
     * If no data is found in the cache, this returns null.
     */
    fun retrieve(restaurant: DbRestaurant, date: Date, acceptStale: Boolean = false): Deferred<Stale<List<DbDish>>?> = async(DbThread) {
        val dateAtMidnight = date.atMidnight
        data {
            val threshold = if (acceptStale) oldestAllowedStaleCacheDate else oldestAllowedCacheDate
            val lastUpdate = select(DbRestaurantCacheEntry::class)
                    .where(DbRestaurantCacheEntryEntity.RESTAURANT eq restaurant)
                    .and(DbRestaurantCacheEntryEntity.DISHES_FOR_DATE eq dateAtMidnight)
                    .and(DbRestaurantCacheEntryEntity.LAST_UPDATE gte threshold)
                    .get()
                    .firstOrNull()
                    ?.lastUpdate ?: Date(0)
            val stale = lastUpdate < oldestAllowedCacheDate
            val result = if (!stale || acceptStale && lastUpdate >= oldestAllowedStaleCacheDate) {
                Stale(
                        select(DbDish::class)
                                .where(DbDishEntity.DATE eq dateAtMidnight)
                                .and(DbDishEntity.RESTAURANT eq restaurant)
                                .get()
                                .toList(),
                        stale
                )

            } else null

            if (result != null) {
                Log.d(TAG, "retrieve(${restaurant.name}, $dateAtMidnight): HIT, $stale")
            } else {
                Log.d(TAG, "retrieve(${restaurant.name}, $dateAtMidnight): MISS")
            }

            result
        }
    }

}

data class Stale<out T>(val value: T, val isStale: Boolean)

fun <T : Any> T.toStale() = Stale(this, isStale = true)
fun <T : Any> T.toNonStale() = Stale(this, isStale = false)