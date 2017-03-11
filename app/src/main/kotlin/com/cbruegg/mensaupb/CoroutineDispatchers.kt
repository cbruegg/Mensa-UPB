package com.cbruegg.mensaupb

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import kotlinx.coroutines.experimental.CoroutineDispatcher
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.CoroutineContext

/**
 * A [CoroutineDispatcher] dispatching to the main thread.
 */
object MainThread : CoroutineDispatcher() {

    private val handler = Handler(Looper.getMainLooper())

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }

}

object DbThread : CoroutineDispatcher() {

    private val handler = Handler(HandlerThread("DbThread").apply { start() }.looper)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }

}