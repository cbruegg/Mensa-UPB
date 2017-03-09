package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.JsonConstructor
import com.cbruegg.mensaupb.parser.MoshiProvider
import com.squareup.moshi.Json
import java.util.*

/**
 * Model representing a dish object returned by the API.
 */
data class Dish @JsonConstructor constructor(@Json(name = "date") val date: Date,
                                             @Json(name = "name_de") val germanName: String,
                                             @Json(name = "description_de") val germanDescription: String?,
                                             @Json(name = "category") val category: String,
                                             @Json(name = "category_de") val germanCategory: String,
                                             @Json(name = "subcategory_de") val germanSubcategory: String,
                                             @Json(name = "priceStudents") val studentPrice: Double,
                                             @Json(name = "priceWorkers") val workerPrice: Double,
                                             @Json(name = "priceGuests") val guestPrice: Double,
                                             @Json(name = "allergens") val allergens: List<String>,
                                             @Json(name = "order_info") val orderInfo: Int,
                                             @Json(name = "badges") val badgesStrings: List<String>?,
                                             @Json(name = "restaurant") val restaurantId: String,
                                             @Json(name = "pricetype") val priceType: PriceType,
                                             @Json(name = "image") val imageUrl: String?,
                                             @Json(name = "thumbnail") val thumbnailImageUrl: String?) {
    companion object {
        /**
         * Deserialize a Dish serialized with [serialize]
         */
        fun deserialize(serialized: String): Dish = MoshiProvider.provideJsonAdapter<Dish>().fromJson(serialized)
    }

    /**
     * Serialize the object for deserialization using the companion object's method.
     */
    fun serialize(): String = MoshiProvider.provideJsonAdapter<Dish>().toJson(this)

    @delegate:Transient val badges by lazy { badgesStrings?.map { Badge.findById(it) } ?: emptyList<Badge>() }
}