package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.squareup.moshi.Json
import okio.BufferedSource

/**
 * Parse restaurants from the API response.
 */
public fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> =
        MoshiProvider.provideJsonAdapter<Map<String, RestaurantInternal>>()
                .fromJson(restaurantListSource)
                .map { Restaurant(it.key, it.value.name, it.value.location, it.value.isActive) }

/**
 * Parse dishes from the API response.
 */
public fun parseDishes(dishSource: BufferedSource): List<Dish> = MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishSource)

private class RestaurantInternal(@Json(name = "name") val name: String,
                                 @Json(name = "location") val location: String,
                                 @Json(name = "active") val isActive: Boolean)