package com.cbruegg.mensaupb.downloader

import com.cbruegg.mensaupbservice.api.Badge
import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.PriceType
import com.cbruegg.mensaupbservice.api.Restaurant
import com.squareup.moshi.Json
import java.util.Date

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

/**
 * The Studierendenwerk API is broken. Dishes with "" as the category in Mensa Academica are from the day before
 * and must be filtered out.
 */
fun List<JsonDish>.mapToDishes(): List<Dish> {
    return filterNot { it.restaurantId == "mensa-academica-paderborn" && it.category.isEmpty() }.map { it.toDish() }
}

/**
 * Model representing a dish object returned by the API.
 */
data class JsonDish(
    @Json(name = "date") val date: Date,
    @Json(name = "name_de") val nameDE: String,
    @Json(name = "name_en") val nameEN: String,
    @Json(name = "description_de") val descriptionDE: String?,
    @Json(name = "description_en") val descriptionEN: String?,
    @Json(name = "category") val category: String,
    @Json(name = "category_de") val categoryDE: String,
    @Json(name = "category_en") val categoryEN: String,
    @Json(name = "subcategory_de") val subcategoryDE: String,
    @Json(name = "subcategory_en") val subcategoryEN: String,
    @Json(name = "priceStudents") val studentPrice: Double,
    @Json(name = "priceWorkers") val workerPrice: Double,
    @Json(name = "priceGuests") val guestPrice: Double,
    @Json(name = "allergens") val allergens: List<String>,
    @Json(name = "order_info") val orderInfo: Int,
    @Json(name = "badges") val badgesStrings: List<String>?,
    @Json(name = "restaurant") val restaurantId: String,
    @Json(name = "pricetype") val priceType: JsonPriceType,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "thumbnail") val thumbnailImageUrl: String?
) {
    @delegate:Transient
    val badges by lazy { badgesStrings?.mapNotNull { Badge.findById(it) } ?: emptyList() }

    fun toDish() = Dish(
        date, nameDE, nameEN,
        descriptionDE, descriptionEN, category, categoryDE, categoryEN,
        subcategoryDE, subcategoryEN, studentPrice, workerPrice, guestPrice, allergens, orderInfo,
        badges, restaurantId, priceType.toApiPriceType(), imageUrl, thumbnailImageUrl
    )
}

enum class JsonPriceType {
    @Json(name = "weighted")
    WEIGHTED,
    @Json(name = "fixed")
    FIXED;

    fun toApiPriceType() = when (this) {
        JsonPriceType.WEIGHTED -> PriceType.WEIGHTED
        JsonPriceType.FIXED -> PriceType.FIXED
    }
}