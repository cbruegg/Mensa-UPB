package com.cbruegg.mensaupb.util

import kotlinx.serialization.*
import kotlinx.serialization.modules.SerializersModule

@ExperimentalSerializationApi
fun BinaryFormat.asStringFormat(): StringFormat = object : StringFormat {

    override val serializersModule: SerializersModule
        get() = this@asStringFormat.serializersModule

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String) =
            this@asStringFormat.decodeFromHexString(deserializer, string)

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T) =
            this@asStringFormat.encodeToHexString(serializer, value)
}