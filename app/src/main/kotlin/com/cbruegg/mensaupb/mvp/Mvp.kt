package com.cbruegg.mensaupb.mvp

import com.cbruegg.mensaupb.util.weakReference

interface MvpView

abstract class MvpPresenter<V : MvpView>(
        private val jobHandler: JobHandler = JobHandlerDelegate()
) : JobHandler by jobHandler {

    protected var view: V? by weakReference(null)
        private set

    fun attachView(view: V) {
        this.view = view
        onViewAttached()
    }

    fun detachView() {
        this.view = null
        jobHandler.onPause()
        onViewDetached()
    }

    protected open fun onViewAttached() {}
    protected open fun onViewDetached() {}
}

interface NoMvpView: MvpView
object NoMvpPresenter: MvpPresenter<NoMvpView>()