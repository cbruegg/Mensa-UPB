package com.cbruegg.mensaupb.util.lifecycle

import androidx.recyclerview.widget.RecyclerView

abstract class LifecycleRecyclerAdapter<T : LifecycleViewHolder> : RecyclerView.Adapter<T>() {

    final override fun onBindViewHolder(holder: T, position: Int) {
        holder.clear()
        onBindViewHolderImpl(holder, position)
    }

    abstract fun onBindViewHolderImpl(holder: T, position: Int)

    override fun onViewAttachedToWindow(holder: T) {
        super.onViewAttachedToWindow(holder)
        holder.onAppear()
    }

    override fun onViewDetachedFromWindow(holder: T) {
        super.onViewDetachedFromWindow(holder)
        holder.onDisappear()
    }

}