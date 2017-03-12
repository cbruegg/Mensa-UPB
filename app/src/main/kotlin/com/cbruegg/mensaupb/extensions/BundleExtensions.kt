package com.cbruegg.mensaupb.extensions

import android.os.Bundle
import java.util.*

fun Bundle.putDate(key: String, date: Date?) {
    putLong(key, date?.time ?: -1)
}

fun Bundle.getDate(key: String): Date? =
        getLong(key, -1).let { if (it == -1L) null else it }?.let(::Date)