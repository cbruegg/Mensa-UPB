package com.cbruegg.mensaupb

import kotlinx.coroutines.experimental.asCoroutineDispatcher
import kotlinx.coroutines.experimental.newSingleThreadContext
import java.util.concurrent.Executors

val DbThread = newSingleThreadContext("DbThread")
val IOPool = Executors.newCachedThreadPool().asCoroutineDispatcher()