package com.cbruegg.mensaupb.api

import com.squareup.moshi.Json
import java.util.Date

data class Restaurant(
    val id: String,
    val name: String,
    val location: String,
    val isActive: Boolean
)

enum class Badge(private val id: String) {
    VEGAN("vegan"),
    VEGETARIAN("vegetarian"),
    NONFAT("nonfat"),
    LACTOSE_FREE("lactose-free");

    companion object {
        fun findById(id: String): Badge? = values().firstOrNull { it.id == id }
    }
}

enum class PriceType {
    @Json(name = "weighted")
    WEIGHTED,

    @Json(name = "fixed")
    FIXED
}

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
    @Json(name = "pricetype") val priceType: PriceType,
    @Json(name = "image") val imageUrl: String?,
    @Json(name = "thumbnail") val thumbnailImageUrl: String?,
    @Json(name = "nutritional_values") val nutritionalValues: JsonNutritionalValues?
) {
    @delegate:Transient
    val badges by lazy { badgesStrings?.mapNotNull { Badge.findById(it) } ?: emptyList() }
}

data class JsonNutritionalValues(
    @Json(name = "all") val all: String?
)
