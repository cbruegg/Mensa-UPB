package com.cbruegg.mensaupb.cache

import android.content.Context
import android.util.Log
import com.cbruegg.mensaupb.DbThread
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.extensions.minus
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Class responsible for caching data used by the app.
 * Each restaurant has its own SharedPreferences file.
 */
class DataCache @Deprecated("Inject this.") constructor(context: Context) {

    @Inject lateinit var data: BlockingEntityStore<Persistable>

    private val cacheValidity = TimeUnit.DAYS.toMillis(1)

    /**
     * If any cache entry is older than this, discard it.
     */
    private val oldestAllowedCacheDate: Date
        get() = Date() - cacheValidity

    init {
        context.app.appComponent.inject(this)
        cleanUp()
    }

    /**
     * Delete entries in all caches older than the current date.
     */
    private fun cleanUp() {
        val threshold = oldestAllowedCacheDate
        runBlocking {
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
                upsert(DbRestaurantListCacheMetaEntity().apply { setLastUpdate(Date()) })
            }
        }
    }

    /**
     * Retrieve the list of cached restaurants.
     */
    fun retrieveRestaurants(): Deferred<List<DbRestaurant>?> = async(DbThread) {
        data {
            val cacheValid = select(DbRestaurantListCacheMetaEntity::class)
                    .where(DbRestaurantListCacheMetaEntity.LAST_UPDATE gte oldestAllowedCacheDate)
                    .get()
                    .any()
            val result = if (cacheValid) {
                select(DbRestaurantEntity::class).get().toList()
            } else null

            Log.d(TAG, "${if (result != null) "HIT" else "MISS"}: retrieveRestaurants()")

            result
        }
    }

    /**
     * Put the dishes of the restaurant into the cache.
     * Only the day, month and year of the date are used.
     * @return The original dish list
     */
    fun cache(restaurant: DbRestaurant, date: Date, dishes: List<Dish>): Deferred<List<DbDish>> = async(DbThread) {
        val dateAtMidnight = date.atMidnight()
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
                setLastUpdate(Date())
            })
        }
        dbDishes
    }

    /**
     * Try to retrieve dishes for the restaurant at the specified date.
     * Only the day, month and year of the date are used.
     * If no data is found in the cache, this returns null.
     */
    fun retrieve(restaurant: DbRestaurant, date: Date): Deferred<List<DbDish>?> = async(DbThread) {
        val dateAtMidnight = date.atMidnight()
        data {
            val cacheValid = select(DbRestaurantCacheEntry::class)
                    .where(DbRestaurantCacheEntryEntity.RESTAURANT eq restaurant)
                    .and(DbRestaurantCacheEntryEntity.DISHES_FOR_DATE eq dateAtMidnight)
                    .and(DbRestaurantCacheEntryEntity.LAST_UPDATE gte oldestAllowedCacheDate)
                    .get()
                    .any()
            val result = if (cacheValid) {
                select(DbDish::class)
                        .where(DbDishEntity.DATE eq dateAtMidnight)
                        .and(DbDishEntity.RESTAURANT eq restaurant)
                        .get()
                        .toList()

            } else null

            if (result != null) {
                Log.d(TAG, "HIT: ${restaurant.name} at $dateAtMidnight")
            } else {
                Log.d(TAG, "MISS: ${restaurant.name} at $dateAtMidnight")
            }

            result
        }
    }

}

