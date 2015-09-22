package com.cbruegg.mensaupb.extensions

import java.util.Comparator

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Array<out T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return this.sortedWith(object : Comparator<T> {
        override fun compare(lhs: T, rhs: T) = comparator(lhs, rhs)
    })
}

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Iterable<T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return this.sortedWith(object : Comparator<T> {
        override fun compare(lhs: T, rhs: T) = comparator(lhs, rhs)
    })
}