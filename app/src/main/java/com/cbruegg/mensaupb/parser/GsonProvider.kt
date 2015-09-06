package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.PriceType
import com.google.gson.*
import java.lang.reflect.Type

fun provideGson(): Gson {
    return GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .registerTypeAdapter(javaClass<PriceType>(), PriceTypeDeserializer())
            .registerTypeAdapter(javaClass<PriceType>(), PriceTypeSerializer())
            .create()
}

private class PriceTypeSerializer : JsonSerializer<PriceType> {
    override fun serialize(src: PriceType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement? =
            when (src) {
                PriceType.FIXED -> JsonPrimitive("fixed")
                PriceType.WEIGHTED -> JsonPrimitive("weighted")
                null -> null
            }
}

private class PriceTypeDeserializer : JsonDeserializer<PriceType> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): PriceType? =
            when (json?.getAsString()) {
                "weighted" -> PriceType.WEIGHTED
                "fixed" -> PriceType.FIXED
                null -> null
                else -> throw IllegalArgumentException("Unsupported PriceType")
            }

}
