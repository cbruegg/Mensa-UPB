package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.JsonConstructor
import com.squareup.moshi.Json
import java.util.*

/**
 * Model representing a dish object returned by the API.
 */
data class Dish @JsonConstructor constructor(
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
        @Json(name = "pricetype") val priceType: PriceType,
        @Json(name = "image") val imageUrl: String?,
        @Json(name = "thumbnail") val thumbnailImageUrl: String?
) {
    @delegate:Transient val badges by lazy { badgesStrings?.map { Badge.findById(it) }?.filterNotNull() ?: emptyList<Badge>() }
}