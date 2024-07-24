package com.cbruegg.mensaupb.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

open class LiveData<T> protected constructor(initialValue: T, private val tCaster: (T?) -> T) : LiveData<T>() {

    companion object {
        @JvmName("LiveData")
        operator fun <T : Any> invoke(initialValue: T) = LiveData(initialValue, tCaster = { it!! })

        @JvmName("NullableLiveData")
        operator fun <T : Any?> invoke(initialValue: T) = LiveData<T?>(initialValue, tCaster = { it })
    }

    open val data: T
        get() = value

    init {
        value = initialValue
    }

    override fun getValue(): T = tCaster(super.getValue())
}

class MutableLiveData<T> private constructor(initialValue: T, tCaster: (T?) -> T) : com.cbruegg.mensaupb.util.LiveData<T>(initialValue, tCaster) {

    companion object {
        @JvmName("MutableLiveData")
        operator fun <T : Any> invoke(initialValue: T) = MutableLiveData(initialValue, tCaster = { it!! })

        @JvmName("NullableMutableLiveData")
        operator fun <T : Any?> invoke(initialValue: T) = MutableLiveData<T?>(initialValue, tCaster = { it })
    }

    override var data: T
        get() = value
        set(x) {
            value = x
        }
}

inline fun <reified T> LiveData<T>.observeNullSafe(lifecycleOwner: LifecycleOwner, crossinline f: (T) -> Unit) = observe(lifecycleOwner) {
    f(it as T)
}