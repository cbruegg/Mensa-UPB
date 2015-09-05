package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.PriceType
import com.google.gson.*
import java.lang.reflect.Type

fun provideGson(): Gson {
    return GsonBuilder()
            .registerTypeAdapter(javaClass<PriceType>(), PriceTypeDeserializer())
            .create()
}

internal class PriceTypeDeserializer : JsonDeserializer<PriceType> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PriceType? =
            when (json?.getAsString()) {
                "weighted" -> PriceType.WEIGHTED
                "fixed" -> PriceType.FIXED
                null -> null
                else -> throw IllegalArgumentException("Unsupported PriceType")
            }

}
