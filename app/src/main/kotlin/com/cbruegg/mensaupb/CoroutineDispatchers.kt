package com.cbruegg.mensaupb

import kotlinx.coroutines.experimental.newSingleThreadContext

val DbThread = newSingleThreadContext("DbThread")