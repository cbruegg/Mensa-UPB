package com.cbruegg.mensaupb

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.newSingleThreadContext
import java.util.concurrent.Executors

val DbThread = newSingleThreadContext("DbThread")
val IOPool = Executors.newCachedThreadPool().asCoroutineDispatcher()