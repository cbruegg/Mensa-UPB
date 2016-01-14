package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.MoshiProvider
import com.squareup.moshi.Json
import java.util.*

/**
 * Model representing a dish object returned by the API.
 */
public data class Dish(@Json(name = "date") public val date: Date,
                       @Json(name = "name_de") public val germanName: String,
                       @Json(name = "description_de") public val germanDescription: String?,
                       @Json(name = "category") public val category: String,
                       @Json(name = "category_de") public val germanCategory: String,
                       @Json(name = "subcategory_de") public val germanSubcategory: String,
                       @Json(name = "priceStudents") public val studentPrice: Double,
                       @Json(name = "priceWorkers") public val workerPrice: Double,
                       @Json(name = "priceGuests") public val guestPrice: Double,
                       @Json(name = "allergens") public val allergens: List<String>,
                       @Json(name = "order_info") public val orderInfo: Int,
                       @Json(name = "badges") public val badges: List<Badge?>?,
                       @Json(name = "restaurant") public val restaurantId: String,
                       @Json(name = "pricetype") public val priceType: PriceType,
                       @Json(name = "image") public val imageUrl: String?,
                       @Json(name = "thumbnail") public val thumbnailImageUrl: String?) {
    companion object {
        /**
         * Deserialize a Dish serialized with [serialize]
         */
        fun deserialize(serialized: String) = MoshiProvider.provideJsonAdapter<Dish>().fromJson(serialized)
    }

    /**
     * Serialize the object for deserialization using the companion object's method.
     */
    fun serialize(): String = MoshiProvider.provideJsonAdapter<Dish>().toJson(this)

}