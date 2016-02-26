package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.MoshiProvider
import com.squareup.moshi.Json

/**
 * Model representing a restaurant object returned by the API.
 */
data class Restaurant(@Json(name = "id") val id: String,
                      @Json(name = "name") val name: String,
                      @Json(name = "location") val location: String,
                      @Json(name = "isActive") val isActive: Boolean) {

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