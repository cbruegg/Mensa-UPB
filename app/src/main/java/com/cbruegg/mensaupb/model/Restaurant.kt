package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.parser.provideGson
import com.github.salomonbrys.kotson.fromJson

public data class Restaurant(public val id: String,
                             public val name: String,
                             public val location: String,
                             public val isActive: Boolean) {

    companion object {
        fun deserialize(serialized: String) = provideGson().fromJson<Restaurant>(serialized)
    }

    fun serialize(): String = provideGson().toJson(this)
}