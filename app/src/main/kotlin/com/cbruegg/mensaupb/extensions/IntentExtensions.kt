package com.cbruegg.mensaupb.extensions

import android.content.Intent
import java.util.Date

fun Intent.putDateExtra(key: String, date: Date?) {
    putExtra(key, date?.time ?: -1)
}

fun Intent.getDateExtra(key: String): Date? =
    getLongExtra(key, -1).let { if (it == -1L) null else it }?.let(::Date)