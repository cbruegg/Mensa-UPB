package com.cbruegg.mensaupb.parser

import android.util.Log
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import okio.BufferedSource

// Assume API does not return null objects

/**
 * Parse restaurants from the API response.
 */
fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> {
    val moshi = MoshiProvider.provideJsonAdapter<Map<String, Map<String, *>>>()
    val deserialized =
            if (BuildConfig.DEBUG) {
                val restaurantListStr = restaurantListSource.readString(Charsets.UTF_8)
                Log.d("parseRestaurantsFromApi", restaurantListStr)
                moshi.fromJson(restaurantListStr)
            } else moshi.fromJson(restaurantListSource)
    return deserialized!!.map {
        Restaurant(it.key, it.value["name"] as String, it.value["location"] as String, it.value["active"] as Boolean)
    }
}

/**
 * Parse dishes from the API response.
 */
fun parseDishes(dishSource: BufferedSource): List<Dish> {
    return if (BuildConfig.DEBUG) {
        dishSource.use {
            val dishesStr = dishSource.readUtf8()
            Log.d("parseDishes", dishesStr)
            MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishesStr)!!
        }
    } else MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishSource)!!
}