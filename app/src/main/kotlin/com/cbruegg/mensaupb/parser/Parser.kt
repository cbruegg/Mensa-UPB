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
fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> =
        MoshiProvider.provideJsonAdapter<Map<String, Map<String, *>>>()
                .fromJson(restaurantListSource)!!
                .map {
                    Restaurant(it.key, it.value["name"] as String, it.value["location"] as String, it.value["active"] as Boolean)
                }

/**
 * Parse dishes from the API response.
 */
fun parseDishes(dishSource: BufferedSource): List<Dish> {
    if (BuildConfig.DEBUG) {
        return dishSource.use {
            val dishesStr = dishSource.readUtf8()
            Log.d("parseDishes", dishesStr)
            MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishesStr)!!
        }
    } else return MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishSource)!!
}