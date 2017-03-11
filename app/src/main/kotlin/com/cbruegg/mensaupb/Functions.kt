package com.cbruegg.mensaupb

import android.util.Base64

/**
 * Useful for creating breakpoints in empty functions.
 */
@Suppress("unused")
@Deprecated(message = "Remove in production.") fun nop() {
}

/**
 * Since SQL databases cannot store lists of objects a columns,
 * this method can be used to serialize such elements safely.
 * Only use this for lists of non-db objects!
 *
 * [toString] must not return an empty string.
 */
inline fun <T> Iterable<T>.serializeForSql(toString: (T) -> String): String =
        map { toString(it) }
                .onEach { require(it.isNotEmpty()) { "Serialized form must not be empty. " } }
                .map { Base64.encodeToString(it.toByteArray(), Base64.DEFAULT) }
                .joinToString(separator = ",")

/**
 * Analogue to [serializeForSql]
 */
inline fun <T> String.deserializeFromSql(toT: (String) -> T): List<T> =
        if (isEmpty()) emptyList()
        else split(',')
                .map { toT(Base64.decode(it, Base64.DEFAULT).toString(Charsets.UTF_8)) }