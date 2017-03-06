package com.cbruegg.mensaupb

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.experimental.CoroutineDispatcher
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