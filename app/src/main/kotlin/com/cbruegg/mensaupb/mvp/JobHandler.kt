package com.cbruegg.mensaupb.mvp

import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.disposeOnCompletion
import java.util.*

interface JobHandler {
    /**
     * Register a [Job] to be canceled during
     * UI pauses.
     */
    fun Job.register()

    /**
     * Call this on UI pauses.
     */
    fun onPause()
}

class JobHandlerDelegate: JobHandler {

    private val jobs: MutableList<Job> = Collections.synchronizedList(mutableListOf())

    /**
     * Register a job to be cancelled in [onPause].
     */
    override fun Job.register() {
        jobs += this
        invokeOnCompletion { jobs -= this }
    }

    override fun onPause() {
        jobs.forEach { it.cancel(CancellationException("onPause() called")) }
        jobs.clear()
    }

}

