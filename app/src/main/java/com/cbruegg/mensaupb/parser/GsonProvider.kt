package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.PriceType
import com.google.gson.*
import java.lang.reflect.Type

private val gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd")
        .registerTypeAdapter(PriceType::class.java, PriceTypeDeserializer())
        .registerTypeAdapter(PriceType::class.java, PriceTypeSerializer())
        .create()

/**
 * Provide a new Gson instance that is designed to be used for handling the API responses.
 */
fun provideGson(): Gson = gson

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
            when (json?.asString) {
                "weighted" -> PriceType.WEIGHTED
                "fixed" -> PriceType.FIXED
                null -> null
                else -> throw IllegalArgumentException("Unsupported PriceType")
            }

}
