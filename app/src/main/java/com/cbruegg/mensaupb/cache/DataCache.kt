package com.cbruegg.mensaupb.cache

import android.content.Context
import android.util.Log
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Collections
import java.util.Date
import java.util.HashSet

class DataCache(private val context: Context) {

    private val TAG = "DataCache"
    private val PREFERENCES_PREFIX = "CACHE_"
    private val MASTER_PREFERENCE_NAME = PREFERENCES_PREFIX + "MASTER"
    private val MASTER_PREFERENCE_RESTAURANT_IDS_KEY = "restaurants"
    private val DATE_FORMAT = "yyyy-MM-dd"

    private val masterPreference = context.getSharedPreferences(MASTER_PREFERENCE_NAME, Context.MODE_PRIVATE)

    init {
        cleanUp()
    }

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

    fun cache(restaurant: Restaurant, date: Date, dishes: List<Dish>): List<Dish> {
        if (dishes.isEmpty()) {
            return dishes
        }

        Log.d(TAG, "Storing dishes for " + restaurant.id + " and date " + date.toString())

        storeRestaurantId(restaurant)
        val store = sharedPreferenceForRestaurantId(restaurant.id)
        val keyForDate = SimpleDateFormat(DATE_FORMAT).format(date)
        store.edit().putStringSet(keyForDate, dishes.serialize()).apply()
        return dishes
    }

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

    private fun storeRestaurantId(restaurant: Restaurant) {
        val restaurantIds = masterPreference.getStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, HashSet())
        restaurantIds.add(restaurant.id)
        masterPreference.edit().putStringSet(MASTER_PREFERENCE_RESTAURANT_IDS_KEY, restaurantIds).apply()
    }

    private fun sharedPreferenceForRestaurantId(restaurantId: String)
            = context.getSharedPreferences(PREFERENCES_PREFIX + restaurantId, Context.MODE_PRIVATE)

    private fun List<Dish>.serialize(): Set<String> {
        val serializedDishes = HashSet<String>(size())
        for (dish in this) {
            serializedDishes.add(dish.serialize())
        }
        return serializedDishes
    }

    private fun deserializeDishes(serializedDishes: Set<String>): List<Dish> {
        val deserializedDishes = ArrayList<Dish>(serializedDishes.size())
        for (serializedDish in serializedDishes) {
            deserializedDishes.add(Dish.deserialize(serializedDish))
        }
        return deserializedDishes
    }

}

