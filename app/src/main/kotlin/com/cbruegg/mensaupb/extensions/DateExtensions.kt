package com.cbruegg.mensaupb.extensions

import java.util.Calendar
import java.util.Date

/**
 * Return a clone of this Date set to 0:00:00:0000
 */
val Date.atMidnight: Date
    get() = Calendar.getInstance().apply {
        time = this@atMidnight
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

/**
 * Returns a date +timeMs from this date's time.
 */
operator fun Date.plus(timeMs: Long) = Date(time + timeMs)

/**
 * Returns a date -timeMs from this date's time.
 */
operator fun Date.minus(timeMs: Long) = Date(time - timeMs)

fun Date.inRangeOrNull(min: Date, max: Date) = if (min <= this && this < max) this else null

/**
 * Current date.
 */
val now: Date
    get() = Date()

/**
 * Current date at midnight.
 */
val midnight: Date
    get() = now.atMidnight