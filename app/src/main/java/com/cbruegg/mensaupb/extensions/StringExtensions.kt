package com.cbruegg.mensaupb.extensions

fun String.capitalizeFirstChar(): String = if (isNotEmpty()) substring(0, 1).toUpperCase() + substring(1) else ""