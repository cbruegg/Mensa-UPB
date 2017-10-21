package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.DishesServiceResult
import com.cbruegg.mensaupbservice.api.Restaurant
import com.cbruegg.mensaupbservice.api.RestaurantsServiceResult
import okio.BufferedSource

/**
 * Parse restaurants from the API response.
 */
fun parseRestaurantsFromApi(restaurantListSource: BufferedSource): List<Restaurant> {
    return RestaurantsServiceResult.deserialize(restaurantListSource.readUtf8()).restaurants
}

/**
 * Parse dishes from the API response.
 */
fun parseDishes(dishSource: BufferedSource): List<Dish> {
    return DishesServiceResult.deserialize(dishSource.readUtf8()).dishes
}