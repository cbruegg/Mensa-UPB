package com.cbruegg.mensaupb.extensions

import java.io.PrintWriter
import java.io.StringWriter

val Throwable.stackTraceString: String
    get() = StringWriter().apply {
        PrintWriter(this).use {
            printStackTrace(it)
        }
    }.toString()