package com.cbruegg.mensaupb.util

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

inline fun <reified V : ViewModel> Fragment.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]

inline fun <reified V : ViewModel> FragmentActivity.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]