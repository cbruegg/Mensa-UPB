package com.cbruegg.mensaupb.util

import okhttp3.MediaType

class MediaTypeConverter : io.requery.Converter<okhttp3.MediaType, String> {
    override fun getMappedType() = MediaType::class.java

    override fun getPersistedType() = String::class.java

    override fun convertToPersisted(value: okhttp3.MediaType?) = value?.toString()

    override fun getPersistedSize() = null

    override fun convertToMapped(type: Class<out okhttp3.MediaType>?, value: String?) =
            value?.let(MediaType::parse)

}