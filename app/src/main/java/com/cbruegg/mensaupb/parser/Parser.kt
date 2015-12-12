package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import org.json.JSONObject
import com.github.salomonbrys.kotson.*
import java.util.*

/**
 * Parse restaurants from the API response.
 */
public fun parseRestaurants(restaurantList: String): List<Restaurant> {
    val jsonRestaurantList = JSONObject(restaurantList)
    val parsedRestaurantList = ArrayList<Restaurant>()
    jsonRestaurantList.keys().asSequence().forEach {
        val restaurantJson = jsonRestaurantList.getJSONObject(it)
        val restaurant = Restaurant(id = it, name = restaurantJson.getString("name"),
                                    location = restaurantJson.getString("location"),
                                    isActive = restaurantJson.getBoolean("active"))
        parsedRestaurantList.add(restaurant)
    }
    return parsedRestaurantList
}

/**
 * Parse dishes from the API response.
 */
public fun parseDishes(dishes: String): List<Dish> = provideGson().fromJson<List<Dish>>(dishes)