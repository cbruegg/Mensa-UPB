package com.cbruegg.mensaupb.util

import java.io.File

class FileConverter : io.requery.Converter<java.io.File, String> {
    override fun getMappedType() = File::class.java

    override fun getPersistedSize() = null

    override fun convertToMapped(type: Class<out java.io.File>?, value: String?) =
            value?.let(::File)

    override fun getPersistedType() = String::class.java

    override fun convertToPersisted(value: java.io.File?) = value?.toString()

}