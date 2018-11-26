package com.cbruegg.mensaupb.extensions

/**
 * Clear the list and add all elements of the collection to it.
 */
fun <T, G : T> MutableList<T>.setAll(collection: Collection<G>) {
    clear()
    addAll(collection)
}

/**
 * Replace all elements that equal toReplace with "by".
 * This method doesn't modify the original list
 */
fun <T, G : T> List<T>.replace(toReplace: G, by: G): List<T> =
    List(size) { i ->
        if (this[i] == toReplace) by else this[i]
    }