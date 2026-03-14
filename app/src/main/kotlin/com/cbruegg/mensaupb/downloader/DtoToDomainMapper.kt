package com.cbruegg.mensaupb.downloader

import com.cbruegg.mensaupb.api.Restaurant

fun Map<String, Map<String, *>>.mapToRestaurants(): List<Restaurant> {
    return map {
        Restaurant(
            it.key,
            it.value["name"] as String,
            it.value["location"] as String,
            it.value["active"] as Boolean
        )
    }.filter { it.id != "one-way-snack" } // Doesn't exist anymore
}
