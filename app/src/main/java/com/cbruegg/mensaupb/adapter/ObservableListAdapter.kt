package com.cbruegg.mensaupb.adapter

import android.databinding.ObservableArrayList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.cbruegg.mensaupb.extensions.addOnListChangedCallback

abstract class ObservableListAdapter<DATA, VH : RecyclerView.ViewHolder>(val list: ObservableArrayList<DATA> = ObservableArrayList()) : RecyclerView.Adapter<VH>() {
    init {
        list.addOnListChangedCallback({ notifyDataSetChanged() })
    }

    override final fun getItemCount(): Int = list.size()

    override final fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, list[position])
    }

    override final fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = onCreateViewHolder(parent, viewType, LayoutInflater.from(parent.getContext()))

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): VH

    abstract fun onBindViewHolder(holder: VH, item: DATA)
}
