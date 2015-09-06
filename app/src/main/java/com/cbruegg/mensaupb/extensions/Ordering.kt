package com.cbruegg.mensaupb.extensions

import java.util.Comparator
import kotlin.reflect.KProperty

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Array<out T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return this.sortBy(object : Comparator<T> {
        override fun compare(lhs: T, rhs: T) = comparator(lhs, rhs)
    })
}

/**
 * Returns a list of all elements, sorted by the specified [comparator].
 */
public fun <T> Iterable<T>.sortBy(comparator: (first: T, second: T) -> Int): List<T> {
    return this.sortBy(object : Comparator<T> {
        override fun compare(lhs: T, rhs: T) = comparator(lhs, rhs)
    })
}