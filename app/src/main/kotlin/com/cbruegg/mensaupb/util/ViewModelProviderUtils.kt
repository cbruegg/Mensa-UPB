package com.cbruegg.mensaupb.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders

inline fun <reified V : ViewModel> Fragment.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]

inline fun <reified V : ViewModel> FragmentActivity.viewModel(crossinline factory: () -> V): V = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(p0: Class<T>): T = factory() as T

})[V::class.java]