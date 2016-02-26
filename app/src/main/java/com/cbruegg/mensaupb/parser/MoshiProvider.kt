package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.PriceType
import com.squareup.moshi.*
import java.util.*

object MoshiProvider {
    /**
     * The global Moshi instance. You will most likely want ot use
     * [provideJsonAdapter] instead.
     */
    val moshi = Moshi.Builder()
            .add(PriceTypeJsonAdapter())
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .build()

    /**
     * Provide a Moshi JsonAdapter for the specified type
     */
    inline fun <reified T : Any> provideJsonAdapter(): JsonAdapter<T> {
        return moshi.adapter<T>(T::class.java)
    }

    /**
     * Provide a Moshi JsonAdapter for the specified type in a list.
     */
    inline fun <reified T : Any> provideListJsonAdapter(): JsonAdapter<List<T>> {
        return moshi.adapter<List<T>>(Types.newParameterizedType(List::class.java, T::class.java))
    }

}

private class PriceTypeJsonAdapter {
    @FromJson fun priceTypeFromJson(priceType: String?): PriceType? =
            when (priceType) {
                "weighted" -> PriceType.WEIGHTED
                "fixed" -> PriceType.FIXED
                null -> null
                else -> throw IllegalArgumentException("Unsupported PriceType")
            }

    @ToJson fun priceTypeToJson(priceType: PriceType?): String? =
            when (priceType) {
                PriceType.FIXED -> "fixed"
                PriceType.WEIGHTED -> "weighted"
                null -> null
            }
}