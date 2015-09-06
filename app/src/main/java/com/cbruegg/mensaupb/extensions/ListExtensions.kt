package com.cbruegg.mensaupb.extensions

fun <T, G : T> MutableList<T>.setAll(collection: Collection<G>) {
    clear()
    addAll(collection)
}