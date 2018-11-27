package com.cbruegg.mensaupb.util

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 * Suspend extension that allows suspend [Call] inside coroutine.
 *
 * @return Result of request or throw exception
 */
suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
    val exceptionWithCapturedStack = CoroutineCallException()

    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            continuation.tryAndCompleteResume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isCancelled) return

            (e.findRootCause() ?: e).initCause(exceptionWithCapturedStack)
            continuation.tryAndCompleteResumeWithException(e)
        }
    })

    continuation.invokeOnCancellation {
        try {
            cancel()
        } catch (ignored: Throwable) {
        }
    }
}

@UseExperimental(InternalCoroutinesApi::class)
private fun <T> CancellableContinuation<T>.tryAndCompleteResume(value: T) = tryResume(value)?.let { completeResume(it) }

@UseExperimental(InternalCoroutinesApi::class)
private fun <T> CancellableContinuation<T>.tryAndCompleteResumeWithException(e: Exception) = tryResumeWithException(e)?.let { completeResume(it) }

private fun Throwable.findRootCause(): Throwable? {
    var cause = cause ?: return null
    while (true) {
        val nextCause = cause.cause
        when (nextCause) {
            null, cause -> return cause
            else -> cause = nextCause
        }
    }
}

private class CoroutineCallException : RuntimeException("Originally called here:")