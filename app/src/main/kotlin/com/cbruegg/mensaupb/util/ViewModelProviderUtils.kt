package com.cbruegg.mensaupb.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

inline fun <reified V : ViewModel> androidx.fragment.app.Fragment.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]

inline fun <reified V : ViewModel> androidx.fragment.app.FragmentActivity.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]