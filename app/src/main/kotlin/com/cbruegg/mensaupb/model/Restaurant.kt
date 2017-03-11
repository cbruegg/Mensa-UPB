package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.JsonConstructor
import com.squareup.moshi.Json

/**
 * Model representing a restaurant object returned by the API.
 */
data class Restaurant @JsonConstructor constructor(
        @Json(name = "id") val id: String,
        @Json(name = "name") val name: String,
        @Json(name = "location") val location: String,
        @Json(name = "isActive") val isActive: Boolean
)