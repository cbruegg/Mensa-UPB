package com.cbruegg.mensaupb.util.delegates

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias PersistentPropertyDelegate<T> = ReadWriteProperty<Any, T>

abstract class SharedPreferencesPropertyDelegate<T>(
        protected val sharedPreferences: SharedPreferences,
        protected val key: String
): PersistentPropertyDelegate<T>

class StringSharedPreferencesPropertyDelegate<S: String?>(
        sharedPreferences: SharedPreferences,
        key: String,
        private val defaultValue: S
) : SharedPreferencesPropertyDelegate<S>(sharedPreferences, key) {
    override fun getValue(thisRef: Any, property: KProperty<*>): S = sharedPreferences.getString(key, defaultValue) as S

    override fun setValue(thisRef: Any, property: KProperty<*>, value: S) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}