package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.MoshiProvider
import com.squareup.moshi.Json
import java.util.*

/**
 * Model representing a dish object returned by the API.
 * You should always specify all parameters, the parameterless
 * constructor is only used for serialization.
 */
// Default values for serialization, needed so the primary constructor will be called
// to initialize the lazy field
data class Dish(@Json(name = "date") val date: Date = Date(),
                @Json(name = "name_de") val germanName: String = "",
                @Json(name = "description_de") val germanDescription: String? = null,
                @Json(name = "category") val category: String = "",
                @Json(name = "category_de") val germanCategory: String = "",
                @Json(name = "subcategory_de") val germanSubcategory: String = "",
                @Json(name = "priceStudents") val studentPrice: Double = -1.0,
                @Json(name = "priceWorkers") val workerPrice: Double = -1.0,
                @Json(name = "priceGuests") val guestPrice: Double = -1.0,
                @Json(name = "allergens") val allergens: List<String> = emptyList(),
                @Json(name = "order_info") val orderInfo: Int = -1,
                @Json(name = "badges") val badgesStrings: List<String>? = null,
                @Json(name = "restaurant") val restaurantId: String = "",
                @Json(name = "pricetype") val priceType: PriceType = PriceType.FIXED,
                @Json(name = "image") val imageUrl: String? = null,
                @Json(name = "thumbnail") val thumbnailImageUrl: String? = null) {
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

    @delegate:Transient val badges by lazy { badgesStrings?.map { Badge.findById(it) } ?: emptyList<Badge>() }
}