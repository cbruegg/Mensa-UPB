package com.cbruegg.mensaupb

import android.os.Handler
import android.os.HandlerThread
import kotlinx.coroutines.experimental.android.HandlerContext

val DbThread = HandlerContext(Handler(HandlerThread("DbThread").apply { start() }.looper), "DbThread")