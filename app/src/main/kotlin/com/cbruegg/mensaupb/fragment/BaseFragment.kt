package com.cbruegg.mensaupb.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.cbruegg.mensaupb.mvp.*

abstract class BaseFragment<V : MvpView, P : MvpPresenter<V>> @JvmOverloads constructor(
        private val jobHandler: JobHandler = JobHandlerDelegate()
) : Fragment(), JobHandler by jobHandler {

    protected lateinit var presenter: P
        private set
    protected abstract val mvpViewType: Class<V>

    /**
     * Sets up the [presenter].
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = createPresenter()
        presenter.attachView(mvpViewType.cast(this), savedInstanceState, runInit = true)
    }

    override fun onResume() {
        super.onResume()
        presenter.attachView(mvpViewType.cast(this), savedInstanceState = null, runInit = false)
    }

    override fun onPause() {
        presenter.detachView()
        jobHandler.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.saveState(outState)
    }

    protected abstract fun createPresenter(): P
}

abstract class NoMvpBaseFragment : BaseFragment<NoMvpView, NoMvpPresenter>(), NoMvpView {
    override val mvpViewType: Class<NoMvpView>
        get() = NoMvpView::class.java

    override fun createPresenter() = NoMvpPresenter
}