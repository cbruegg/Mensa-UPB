package com.cbruegg.mensaupb.util

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class WeakReferenceDelegate<T>(initialValue: T) : ReadWriteProperty<Any, T?> {

    private var weakRef: WeakReference<T?> = WeakReference(initialValue)

    override fun getValue(thisRef: Any, property: KProperty<*>): T? = weakRef.get()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        weakRef = WeakReference(value)
    }

}

fun <T> weakReference(initialValue: T) = WeakReferenceDelegate(initialValue)