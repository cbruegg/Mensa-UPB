package com.cbruegg.mensaupb.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class LiveData<T>(initialValue: T) : LiveData<T>() {

    open val data: T
        get() = value

    init {
        value = initialValue
    }

    override fun getValue(): T = super.getValue() as T
}

open class MutableLiveData<T>(initialValue: T) : com.cbruegg.mensaupb.util.LiveData<T>(initialValue) {
    override var data: T
        get() = value
        set(x) {
            value = x
        }
}

inline fun <reified T> observer(crossinline f: ((T) -> Unit)): Observer<T> = Observer<T> {
    f(it as T)
}

inline fun <reified T> LiveData<T>.observe(lifecycleOwner: LifecycleOwner, crossinline f: (T) -> Unit) = observe(lifecycleOwner, observer {
    f(it)
})