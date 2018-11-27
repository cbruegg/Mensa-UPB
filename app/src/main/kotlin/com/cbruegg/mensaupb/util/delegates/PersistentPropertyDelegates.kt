package com.cbruegg.mensaupb.util.delegates

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

typealias PersistentPropertyDelegate<T> = ReadWriteProperty<Any, T>

abstract class SharedPreferencesPropertyDelegate<T>(
    private val sharedPreferencesProvider: () -> SharedPreferences,
    protected val key: String
) : PersistentPropertyDelegate<T> {
    protected val sharedPreferences by lazy { sharedPreferencesProvider() }
}

class StringSharedPreferencesPropertyDelegate<S : String?>
private constructor(
    sharedPreferences: () -> SharedPreferences,
    key: String,
    private val defaultValue: S,
    private val sCaster: (String?) -> S
) : SharedPreferencesPropertyDelegate<S>(sharedPreferences, key) {

    companion object {
        @JvmName("StringSharedPreferencesPropertyDelegate")
        operator fun invoke(sharedPreferences: () -> SharedPreferences, key: String, defaultValue: String) =
            StringSharedPreferencesPropertyDelegate(sharedPreferences, key, defaultValue, sCaster = { it!! })

        @JvmName("NullableStringSharedPreferencesPropertyDelegate")
        operator fun invoke(sharedPreferences: () -> SharedPreferences, key: String, defaultValue: String?) =
            StringSharedPreferencesPropertyDelegate(sharedPreferences, key, defaultValue, sCaster = { it })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): S = sCaster(sharedPreferences.getString(key, defaultValue))

    override fun setValue(thisRef: Any, property: KProperty<*>, value: S) {
        sharedPreferences.edit().putString(key, value).apply()
    }
}