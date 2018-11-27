package com.cbruegg.mensaupb

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

val DbThread = Executors.newSingleThreadExecutor().asCoroutineDispatcher()