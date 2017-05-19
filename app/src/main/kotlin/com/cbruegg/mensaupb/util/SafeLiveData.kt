package com.cbruegg.mensaupb.util

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer

open class LiveData<T>(initialValue: T) : LiveData<T>() {

    open val data: T
        get() = value

    init {
        value = initialValue
    }

    override fun getValue(): T = super.getValue() as T

    override fun observeForever(observer: Observer<T>) = super.observeForever(observer)

    override fun removeObserver(observer: Observer<T>) = super.removeObserver(observer)

    override fun observe(owner: LifecycleOwner, observer: Observer<T>) = super.observe(owner, observer)
}

open class MutableLiveData<T>(initialValue: T) : com.cbruegg.mensaupb.util.LiveData<T>(initialValue) {
    override var data: T
        get() = value
        set(x) {
            value = x
        }

    override fun postValue(value: T) = super.postValue(value)
}

inline fun <reified T> observer(crossinline f: ((T) -> Unit)): Observer<T> = Observer<T> {
    f(it as T)
}

inline fun <reified T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline f: (T) -> Unit) = observe(lifecycleOwner, observer {
    f(it)
})