package com.cbruegg.mensaupb.cache

import android.os.Parcelable
import io.requery.*
import java.util.*

const val TABLE_DB_RESTAURANT_CACHE_ENTRY = "restaurant_cache_entries"
private const val UNIQUE_IDX = "unique_idx"

/**
 * If such an entry exists, then the [DbDish]es
 * for [restaurant] on day [dishesForDate] were last updated on
 * [lastUpdate].
 */
@Entity @Table(name = TABLE_DB_RESTAURANT_CACHE_ENTRY, uniqueIndexes = arrayOf(UNIQUE_IDX))
interface DbRestaurantCacheEntry : Persistable, Parcelable {

    @get:Column(name = "id") @get:Key @get:Generated
    val id: Int

    @get:Column(name = "restaurant") @get:ManyToOne(cascade = arrayOf(CascadeAction.DELETE)) @get:Index(UNIQUE_IDX)
    val restaurant: DbRestaurant

    @get:Column(name = "dishesForDate") @get:Index(UNIQUE_IDX)
    val dishesForDate: Date

    @get:Column(name = "lastUpdate") @get:Index(UNIQUE_IDX)
    val lastUpdate: Date
}

/**
 * Singleton DB entry. If it exists, it specifies the last time
 * the list of restaurants was updated.
 */
@Entity @Table(name = "restaurant_list_cache_meta")
interface DbRestaurantListCacheMeta : Persistable, Parcelable {
    @get:Column(name = "id") @get:Key @get:Generated
    @Deprecated(message = "Only used for sqlite internally.", replaceWith = ReplaceWith(""))
    val id: Int
        get() = 0

    @get:Column(name = "lastUpdate")
    val lastUpdate: Date
}