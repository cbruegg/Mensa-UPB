package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.provideGson
import com.github.salomonbrys.kotson.fromJson

/**
 * Model representing a restaurant object returned by the API.
 */
public data class Restaurant(public val id: String,
                             public val name: String,
                             public val location: String,
                             public val isActive: Boolean) {

    companion object {
        /**
         * Deserialize a Restaurant serialized with [serialize]
         */
        fun deserialize(serialized: String) = provideGson().fromJson<Restaurant>(serialized)
    }

    /**
     * Serialize the object for deserialization using the companion object's method.
     */
    fun serialize(): String = provideGson().toJson(this)
}