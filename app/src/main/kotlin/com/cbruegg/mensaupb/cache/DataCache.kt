package com.cbruegg.mensaupb.cache

import android.content.Context
import android.util.Log
import android.util.Pair
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.extensions.withLockAsync
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.locks.ReentrantLock

/**
 * Class responsible for caching data used by the app.
 * Each restaurant has its own SharedPreferences file.
 */
class DataCache @Deprecated("Inject this.") constructor(private val context: Context) {

    // TODO Move this to SQLite db

    private val PREFERENCES_PREFIX = "CACHE_"
    private val MASTER_PREFERENCE_NAME = PREFERENCES_PREFIX + "MASTER"
    private val MASTER_PREFERENCE_RESTAURANT_IDS_KEY = "restaurants"
    private val MASTER_PREFERENCE_RESTAURANTS_SET_KEY = "restaurant_data"
    private val MASTER_PREFERENCE_RESTAURANTS_SET_SAVED_DATE_KEY = "restaurant_data_saved_date"
    private val DATE_FORMAT = "yyyy-MM-dd"

    private val writeLock = ReentrantLock()

    /**
     * SharedPreferences for cache metadata.
     */
    private val masterPreference = context.getSharedPreferences(MASTER_PREFERENCE_NAME, Context.MODE_PRIVATE)

    init {
        cleanUp()
    }

    /**
     * Delete entries in all caches older than the current date.
     */
    private fun cleanUp() {
        writeLock.withLockAsync {
            val restaurantIds: Set<String> = masterPreference.getStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, Collections.emptySet())
            val today = Date().atMidnight()
            val dateFormatter = SimpleDateFormat(DATE_FORMAT)
            restaurantIds.forEach { restaurantId ->
                val store = sharedPreferenceForRestaurantId(restaurantId)
                val storeEditor = store.edit()
                val datesInStore = store.all.keys.map { stringDate -> Pair(stringDate, dateFormatter.parse(stringDate)) }

                datesInStore.filter {
                    it.second.before(today)
                }.forEach { oldStringDatePair ->
                    storeEditor.remove(oldStringDatePair.first)
                }
                storeEditor.apply()
            }

            if (masterPreference.getLong(MASTER_PREFERENCE_RESTAURANTS_SET_SAVED_DATE_KEY, today.time) < today.time) {
                // Clear cached restaurants
                masterPreference.edit().remove(MASTER_PREFERENCE_RESTAURANTS_SET_KEY).apply()
            }
        }
    }

    /**
     * Cache the list of restaurants and return the original list.
     */
    fun cache(restaurants: List<Restaurant>): List<Restaurant> {
        if (restaurants.isEmpty()) {
            return restaurants
        }

        Log.d(TAG, "Storing restaurants")

        writeLock.withLockAsync {
            val serialized = restaurants.map { it.serialize() }.toSet()
            masterPreference
                    .edit()
                    .putStringSet(MASTER_PREFERENCE_RESTAURANTS_SET_KEY, serialized)
                    .putLong(MASTER_PREFERENCE_RESTAURANTS_SET_SAVED_DATE_KEY, System.currentTimeMillis())
                    .apply()
        }

        return restaurants
    }

    /**
     * Retrieve the list of cached restaurants, which may be null
     * if the entry is expired or not entered yet.
     */
    fun retrieveRestaurants(): List<Restaurant>? =
            masterPreference.getStringSet(MASTER_PREFERENCE_RESTAURANTS_SET_KEY, null)
                    ?.map { Restaurant.deserialize(it) }


    /**
     * Put the dishes of the restaurant into the cache.
     * Only the day, month and year of the date are used.
     * @return The original dish list
     */
    fun cache(restaurant: Restaurant, date: Date, dishes: List<Dish>): List<Dish> {
        if (dishes.isEmpty()) {
            return dishes
        }

        Log.d(TAG, "Storing dishes for ${restaurant.id} and date ${date.toString()}")

        writeLock.withLockAsync {
            storeRestaurantId(restaurant)
            val store = sharedPreferenceForRestaurantId(restaurant.id)
            val keyForDate = SimpleDateFormat(DATE_FORMAT).format(date)
            val serializedDishes = dishes.serialize()

            store.edit().putStringSet(keyForDate, serializedDishes.toSet()).apply()
        }

        return dishes
    }

    /**
     * Try to retrieve dishes for the restaurant at the specified date.
     * Only the day, month and year of the date are used.
     * If no data is found in the cache, this returns null.
     */
    fun retrieve(restaurant: Restaurant, date: Date): List<Dish>? {
        val store = sharedPreferenceForRestaurantId(restaurant.id)
        val keyForDate = SimpleDateFormat(DATE_FORMAT).format(date)

        val serializedDishes = store.getStringSet(keyForDate, null)
        val missOrHit = if (serializedDishes == null) "MISS" else "HIT"
        Log.d(TAG, "$missOrHit: ${restaurant.id}, $keyForDate")
        if (serializedDishes == null) {
            return null
        } else {
            return deserializeDishes(serializedDishes)
        }
    }

    /**
     * Add the restaurantId to the list of restaurants in the cache.
     * Required for cleaning the cache.
     */
    private fun storeRestaurantId(restaurant: Restaurant) {
        val restaurantIds = masterPreference.getStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, HashSet())
        restaurantIds.add(restaurant.id)
        masterPreference.edit().putStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, restaurantIds).apply()
    }

    /**
     * Get the SharedPreference for the specified restaurant.
     */
    private fun sharedPreferenceForRestaurantId(restaurantId: String)
            = context.getSharedPreferences(PREFERENCES_PREFIX + restaurantId, Context.MODE_PRIVATE)

    /**
     * Serialize dishes for storing them in a SharedPreference.
     */
    private fun List<Dish>.serialize(): List<String> = map { it.serialize() }

    /**
     * Deserialize dishes from a SharedPreference.
     */
    private fun deserializeDishes(serializedDishes: Set<String>): List<Dish>
            = serializedDishes.map { Dish.deserialize(it) }.filterNotNull()

}

