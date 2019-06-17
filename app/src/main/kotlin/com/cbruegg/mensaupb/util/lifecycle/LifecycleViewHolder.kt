package com.cbruegg.mensaupb.util.lifecycle

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

abstract class LifecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LifecycleOwner {

    private var lifecycleRegistry = createLifecycleRegistry()

    private fun createLifecycleRegistry() = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.INITIALIZED
    }

    fun onAppear() {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    fun onDisappear() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    fun clear() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        lifecycleRegistry = createLifecycleRegistry()
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

}