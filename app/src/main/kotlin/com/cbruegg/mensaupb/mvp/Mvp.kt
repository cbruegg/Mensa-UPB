package com.cbruegg.mensaupb.mvp

import android.os.Bundle
import com.cbruegg.mensaupb.util.weakReference

interface MvpView
interface MvpModel
interface MvpModelSaver<in M : MvpModel> {
    fun save(model: M, savedInstanceState: Bundle)
    fun restore(savedInstanceState: Bundle, intoModel: M)
}

abstract class MvpPresenter<V : MvpView> : ModelMvpPresenter<V, EmptyModel>(EmptyModel, EmptyModelSaver)

abstract class ModelMvpPresenter<V : MvpView, out M : MvpModel>(
        val model: M,
        private val modelSaver: MvpModelSaver<M>,
        private val jobHandler: JobHandler = JobHandlerDelegate()
) : JobHandler by jobHandler {

    protected var view: V? by weakReference(null)
        private set

    /**
     * Call this when the presenter should be allowed to perform
     * changes to the [view].
     *
     * @param [savedInstanceState] If non-null, the model state is restored.
     * @param [runInit] Iff true, [initView] will be called to let the presenter
     *                  perform view initialization, for example downloading data
     *                  and calling the view to show it.
     */
    fun attachView(view: V, savedInstanceState: Bundle?, runInit: Boolean) {
        this.view = view
        savedInstanceState
                ?.getBundle(javaClass.name)
                ?.let { modelSaver.restore(it, model) }
        onViewAttached()
        if (runInit) {
            initView()
        }
    }

    /**
     * Call this when the presenter should no longer be allowed to perform
     * changes to the [view].
     */
    fun detachView() {
        this.view = null
        jobHandler.onPause()
        onViewDetached()
    }

    fun saveState(savedInstanceState: Bundle) {
        savedInstanceState.putBundle(
                javaClass.name,
                Bundle().also { modelSaver.save(model, it) }
        )
    }

    protected open fun onViewAttached() {}
    protected open fun initView() {}
    protected open fun onViewDetached() {}
}

interface NoMvpView : MvpView
object NoMvpPresenter : MvpPresenter<NoMvpView>()

object EmptyModel : MvpModel
object EmptyModelSaver : MvpModelSaver<EmptyModel> {
    override fun save(model: EmptyModel, savedInstanceState: Bundle) {}
    override fun restore(savedInstanceState: Bundle, intoModel: EmptyModel) {}
}