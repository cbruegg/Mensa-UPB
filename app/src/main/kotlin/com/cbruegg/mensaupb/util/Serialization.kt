package com.cbruegg.mensaupb.util

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.dumps
import kotlinx.serialization.loads

fun BinaryFormat.asStringFormat(): StringFormat = object : StringFormat {
    override val context get() = this@asStringFormat.context

    override fun <T> parse(deserializer: DeserializationStrategy<T>, string: String) =
        this@asStringFormat.loads(deserializer, string)

    override fun <T> stringify(serializer: SerializationStrategy<T>, obj: T) =
        this@asStringFormat.dumps(serializer, obj)
}