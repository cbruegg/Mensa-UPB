package com.cbruegg.mensaupb.util

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private class ThreadLocalDelegate<out T>(init: () -> T) : ReadOnlyProperty<Any, T> {

    private val local = object : ThreadLocal<T>() {
        override fun initialValue() = init()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T = local.get()!!

}

fun <T> threadLocal(init: () -> T): ReadOnlyProperty<Any, T> = ThreadLocalDelegate(init)