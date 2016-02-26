package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import okio.BufferedSource

/**
 * Parse restaurants from the API response.
 */
fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> =
        MoshiProvider.provideJsonAdapter<Map<String, Map<String, *>>>()
                .fromJson(restaurantListSource)
                .map {
                    Restaurant(it.key, it.value["name"] as String, it.value["location"] as String, it.value["active"] as Boolean)
                }

/**
 * Parse dishes from the API response.
 */
fun parseDishes(dishSource: BufferedSource): List<Dish> = MoshiProvider.provideListJsonAdapter<Dish>().fromJson(dishSource)