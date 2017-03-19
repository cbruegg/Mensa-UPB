package com.cbruegg.mensaupb.extensions

import android.net.Uri
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Return a copy of the string that has the first char capitalized with the rest
 * of the String remaining the same.
 */
fun String.capitalizeFirstChar(): String = if (isNotEmpty()) substring(0, 1).toUpperCase() + substring(1) else ""

fun String.md5(): String = MessageDigest.getInstance("MD5").run {
    update(toByteArray())
    BigInteger(1, digest()).toString(16)
}

fun String.toUri(): Uri = Uri.parse(this)