package com.cbruegg.mensaupb.extensions

/**
 * Return a copy of the string that has the first char capitalized with the rest
 * of the String remaining the same.
 */
fun String.capitalizeFirstChar(): String = if (isNotEmpty()) substring(0, 1).toUpperCase() + substring(1) else ""