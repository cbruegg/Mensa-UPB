package com.cbruegg.mensaupb.extensions

import java.util.*

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Array<out T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return sortedWith(Comparator<T> { lhs, rhs -> comparator(lhs, rhs) })
}

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Iterable<T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return sortedWith(Comparator<T> { lhs, rhs -> comparator(lhs, rhs) })
}