package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.MoshiProvider
import com.squareup.moshi.Json

/**
 * Model representing a restaurant object returned by the API.
 */
public data class Restaurant(@Json(name = "id") public val id: String,
                             @Json(name = "name") public val name: String,
                             @Json(name = "location") public val location: String,
                             @Json(name = "isActive") public val isActive: Boolean) {

    companion object {
        /**
         * Deserialize a Restaurant serialized with [serialize]
         */
        fun deserialize(serialized: String) = MoshiProvider.provideJsonAdapter<Restaurant>().fromJson(serialized)
    }

    /**
     * Serialize the object for deserialization using the companion object's method.
     */
    fun serialize(): String = MoshiProvider.provideJsonAdapter<Restaurant>().toJson(this)
}