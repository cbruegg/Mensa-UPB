package com.cbruegg.mensaupb.extensions

fun <T, G : T> MutableList<T>.setAll(collection: Collection<G>) {
    clear()
    addAll(collection)
}

fun <T, G : T> List<T>.replace(toReplace: G, by: G): List<T> {
    val copy = toArrayList()
    for (i in copy.indices) {
        if (copy[i] == toReplace) {
            copy[i] = by
        }
    }
    return copy
}