package com.cbruegg.mensaupb.extensions

import arrow.core.Either
import retrofit2.HttpException
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter

val Throwable.stackTraceString
    get() = StringWriter().apply {
        PrintWriter(this).use {
            printStackTrace(it)
        }
    }.toString()

/**
 * Return either the desired result on the [Either.Right] side
 * or a caught [IOException] on the [Either.Left] side.
 */
inline fun <T : Any> eitherTryIo(f: () -> T): Either<IOException, T> =
    try {
        Either.Right(f())
    } catch (e: IOException) {
        Either.Left(e)
    } catch(e: HttpException) {
        Either.Left(IOException(e))
    }