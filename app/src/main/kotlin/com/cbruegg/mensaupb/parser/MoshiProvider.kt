package com.cbruegg.mensaupb.parser

import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import com.squareup.moshi.Types
import java.util.*

object MoshiProvider {
    /**
     * The global Moshi instance. You will most likely want ot use
     * [provideJsonAdapter] instead.
     */
    val moshi: Moshi = Moshi.Builder()
            .add(Date::class.java, Rfc3339DateJsonAdapter())
            .addUserClassFactory(Dish::class.java, KotlinDataClassFactory())
            .addUserClassFactory(Restaurant::class.java, KotlinDataClassFactory())
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