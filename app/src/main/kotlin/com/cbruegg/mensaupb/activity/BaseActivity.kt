package com.cbruegg.mensaupb.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.cbruegg.mensaupb.mvp.*

abstract class BaseActivity<V : MvpView, P : MvpPresenter<V>> @JvmOverloads constructor(
        private val jobHandler: JobHandler = JobHandlerDelegate()
) : AppCompatActivity(), JobHandler by jobHandler {

    protected lateinit var presenter: P
        private set
    protected abstract val mvpViewType: Class<V>

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        presenter = createPresenter()
        presenter.attachView(mvpViewType.cast(this))
    }

    override fun onPause() {
        presenter.detachView()
        jobHandler.onPause()
        super.onPause()
    }

    protected abstract fun createPresenter(): P
}

abstract class NoMvpBaseActivity: BaseActivity<NoMvpView, NoMvpPresenter>(), NoMvpView {
    override val mvpViewType: Class<NoMvpView>
        get() = NoMvpView::class.java

    override fun createPresenter() = NoMvpPresenter
}