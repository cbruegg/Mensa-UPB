package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.provideGson
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.annotations.SerializedName
import java.util.*

public data class Dish(@SerializedName("date") public val date: Date,
                       @SerializedName("name_de") public val germanName: String,
                       @SerializedName("description_de") public val germanDescription: String?,
                       @SerializedName("category") public val category: String,
                       @SerializedName("category_de") public val germanCategory: String,
                       @SerializedName("subcategory_de") public val germanSubcategory: String,
                       @SerializedName("priceStudents") public val studentPrice: Double,
                       @SerializedName("priceWorkers") public val workerPrice: Double,
                       @SerializedName("priceGuests") public val guestPrice: Double,
                       @SerializedName("allergens") public val allergens: List<String>,
                       @SerializedName("order_info") public val orderInfo: Int,
                       @SerializedName("badges") public val badges: List<String>,
                       @SerializedName("restaurant") public val restaurantId: String,
                       @SerializedName("pricetype") public val priceType: PriceType,
                       @SerializedName("image") public val imageUrl: String?,
                       @SerializedName("thumbnail") public val thumbnailImageUrl: String?) {
    companion object {
        fun deserialize(serialized: String) = provideGson().fromJson<Dish>(serialized)
    }

    fun serialize(): String = provideGson().toJson(this)
}