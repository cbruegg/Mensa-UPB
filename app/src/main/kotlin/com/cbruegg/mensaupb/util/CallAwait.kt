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
    val exceptionWithCapturedStack = SuspendingCallException()

    enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            exceptionWithCapturedStack.initCause(Exception())
            continuation.tryAndCompleteResumeWithException(exceptionWithCapturedStack)

//            continuation.tryAndCompleteResume(response)
        }

        override fun onFailure(call: Call, e: IOException) {
            if (continuation.isCancelled) return

            exceptionWithCapturedStack.initCause(e)
            continuation.tryAndCompleteResumeWithException(exceptionWithCapturedStack)
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

private class SuspendingCallException : RuntimeException() {

    override var message: String = "This ${SuspendingCallException::class.java.simpleName} " +
            "has not been initialized yet"
        private set

    override fun initCause(cause: Throwable): Throwable {
        message = "Received ${cause.javaClass.name} while executing suspending function here, stacktrace is found below."
        return super.initCause(cause)
    }
}