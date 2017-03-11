package com.cbruegg.mensaupb

import android.os.Handler
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

    private val handler: Handler

    init {
        val looperReadyLock = Semaphore(0)
        var looper: Looper? = null
        thread {
            Looper.prepare()
            looper = Looper.myLooper()
            looperReadyLock.release()
            Looper.loop()
        }
        looperReadyLock.acquire()
        Thread.sleep(10) // Just to be sure the looper loops
        handler = Handler(looper)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }

}