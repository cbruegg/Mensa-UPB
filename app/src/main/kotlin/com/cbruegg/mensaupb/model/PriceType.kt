package com.cbruegg.mensaupb.model

import com.squareup.moshi.Json

/**
 * Enum of price types used by the API
 */
enum class PriceType {
    @Json(name = "weighted") WEIGHTED,
    @Json(name = "fixed") FIXED
}