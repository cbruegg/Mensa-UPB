package com.cbruegg.mensaupb.cache

import android.content.Context
import android.util.Log
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.HashSet

/**
 * Class responsible for caching data used by the app.
 * Each restaurant has its own SharedPreferences file.
 */
class DataCache private constructor(private val context: Context) {

    companion object {
        // Multiple instances of this class shouldn't happen,
        // otherwise race conditions can occur while saving.
        private var dataCacheRef: WeakReference<DataCache?> = WeakReference(null)

        /**
         * Get a singleton instance of this class.
         */
        fun getInstance(context: Context): DataCache {
            return dataCacheRef.get() ?: createNewInstance(context)
        }

        private fun createNewInstance(context: Context): DataCache {
            val dataCache = DataCache(context)
            dataCacheRef = WeakReference(dataCache)
            return dataCache
        }
    }

    /**
     * Log tag
     */
    private val TAG = "DataCache"

    private val PREFERENCES_PREFIX = "CACHE_"
    private val MASTER_PREFERENCE_NAME = PREFERENCES_PREFIX + "MASTER"
    private val MASTER_PREFERENCE_RESTAURANT_IDS_KEY = "restaurants"
    private val DATE_FORMAT = "yyyy-MM-dd"

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
        val restaurantIds: Set<String> = masterPreference.getStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, Collections.emptySet())
        val today = Date()
        val dateFormatter = SimpleDateFormat(DATE_FORMAT)
        restaurantIds.forEach { restaurantId ->
            val store = sharedPreferenceForRestaurantId(restaurantId)
            val storeEditor = store.edit()
            val datesInStore = store.getAll().keySet().map { stringDate -> Pair(stringDate, dateFormatter.parse(stringDate)) }

            datesInStore.filter {
                it.second.before(today)
            }.forEach { oldStringDatePair ->
                storeEditor.remove(oldStringDatePair.first)
            }
            storeEditor.apply()
        }
    }

    /**
     * Put the dishes of the restaurant into the cache.
     * Only the day, month and year of the date are used.
     */
    synchronized fun cache(restaurant: Restaurant, date: Date, dishes: List<Dish>): List<Dish> {
        if (dishes.isEmpty()) {
            return dishes
        }

        Log.d(TAG, "Storing dishes for " + restaurant.id + " and date " + date.toString())

        storeRestaurantId(restaurant)
        val store = sharedPreferenceForRestaurantId(restaurant.id)
        val keyForDate = SimpleDateFormat(DATE_FORMAT).format(date)
        val serializedDishes = dishes.serialize()

        synchronized(this) {
            store.edit().putStringSet(keyForDate, serializedDishes).apply()
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
        Log.d(TAG, (if (serializedDishes == null) "MISS" else "HIT") + ": " + restaurant.id + ", " + date.toString())
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
    private fun List<Dish>.serialize(): Set<String> {
        val serializedDishes = HashSet<String>(size())
        for (dish in this) {
            serializedDishes.add(dish.serialize())
        }
        return serializedDishes
    }

    /**
     * Deserialize dishes from a SharedPreference.
     */
    private fun deserializeDishes(serializedDishes: Set<String>): List<Dish> {
        val deserializedDishes = ArrayList<Dish>(serializedDishes.size())
        for (serializedDish in serializedDishes) {
            deserializedDishes.add(Dish.deserialize(serializedDish))
        }
        return deserializedDishes
    }

}

