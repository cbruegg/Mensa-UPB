package com.cbruegg.mensaupb.extensions

import java.util.*

/**
 * Return a clone of this Date set to 0:00:00:0000
 */
fun Date.atMidnight(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

/**
 * Returns a date +timeMs from this date's time.
 */
operator fun Date.plus(timeMs: Long) = Date(time + timeMs)

/**
 * Returns a date -timeMs from this date's time.
 */
operator fun Date.minus(timeMs: Long) = Date(time - timeMs)