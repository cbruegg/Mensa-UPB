package com.cbruegg.mensaupb.mvp

import android.os.Bundle
import android.util.Log
import com.cbruegg.mensaupb.extensions.TAG
import com.cbruegg.mensaupb.util.weakReference

interface MvpView
interface MvpModel

/**
 * The presenter will handle state management for you,
 * but you need to implement this interface for your model
 * to save and restore state to/from a [Bundle]. Android may discard
 * the saved state after process death, so critical state that
 * should survive app relaunches should be saved in other ways,
 * for example by using a [com.cbruegg.mensaupb.util.delegates.PersistentPropertyDelegate]
 * in your [MvpModel].
 */
interface MvpModelSaver<in M : MvpModel> {
    /**
     * @see [MvpModelSaver]
     */
    fun save(model: M, savedInstanceState: Bundle)

    /**
     * @see [MvpModelSaver]
     */
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
        Log.i(TAG, "Attaching view $view to $this")

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
        Log.i(TAG, "Detaching view $view to $this")

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

    /**
     * Called when the view is attached to the presenter and the
     * model state has been restored.
     * You should not initialize any content of the view here.
     *
     * @see [initView]
     */
    protected open fun onViewAttached() {}

    /**
     * Called after the view has been attached and the model state has
     * been restored.
     * You should load content into the View here. This method is usually
     * called after methods like [android.app.Activity.onCreate] and
     * [android.support.v4.app.Fragment.onCreateView], but not after methods
     * like [android.app.Activity.onResume].
     */
    protected open fun initView() {}

    /**
     * Called when the view is detached. This can mean
     * that the UI is paused, but it may as well be destroyed.
     */
    protected open fun onViewDetached() {}
}

interface NoMvpView : MvpView
object NoMvpPresenter : MvpPresenter<NoMvpView>()

object EmptyModel : MvpModel
object EmptyModelSaver : MvpModelSaver<EmptyModel> {
    override fun save(model: EmptyModel, savedInstanceState: Bundle) {}
    override fun restore(savedInstanceState: Bundle, intoModel: EmptyModel) {}
}