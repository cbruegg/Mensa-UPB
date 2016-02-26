package com.cbruegg.mensaupb.extensions

import java.util.concurrent.locks.Lock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock

/**
 * Run the specified function on a new thread,
 * obtaining a lock in the thread first.
 */
inline fun Lock.withLockAsync(crossinline f: () -> Unit) {
    thread {
        withLock {
            f()
        }
    }
}