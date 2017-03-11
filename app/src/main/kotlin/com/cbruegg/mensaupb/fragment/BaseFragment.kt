package com.cbruegg.mensaupb.fragment

import android.support.v4.app.Fragment
import com.cbruegg.mensaupb.mvp.JobHandler
import com.cbruegg.mensaupb.mvp.JobHandlerDelegate

abstract class BaseFragment @JvmOverloads constructor(
        private val jobHandler: JobHandler = JobHandlerDelegate()
) : Fragment(), JobHandler by jobHandler {

    override fun onPause() {
        jobHandler.onPause()
        super.onPause()
    }
}