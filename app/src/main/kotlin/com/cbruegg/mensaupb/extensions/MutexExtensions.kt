package com.cbruegg.mensaupb.extensions

import kotlinx.coroutines.experimental.sync.Mutex

suspend fun Mutex.use(f: suspend () -> Unit) {
    lock()
    try {
        f()
    } finally {
        unlock()
    }
}