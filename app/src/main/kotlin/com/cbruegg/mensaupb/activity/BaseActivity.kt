package com.cbruegg.mensaupb.activity

import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.Job
import java.util.*

abstract class BaseActivity : AppCompatActivity() {

    private val jobs: MutableList<Job> = Collections.synchronizedList(mutableListOf())

    /**
     * Register a job to be cancelled in [onPause].
     */
    protected fun Job.register() {
        jobs += this
        invokeOnCompletion { jobs -= this }
    }

    override fun onPause() {
        jobs.forEach { it.cancel(CancellationException("onPause() called")) }
        jobs.clear()
        super.onPause()
    }
}