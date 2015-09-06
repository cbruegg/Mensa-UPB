package com.cbruegg.mensaupb.adapter

import android.databinding.ObservableArrayList
import android.support.v7.widget.RecyclerView
import com.cbruegg.mensaupb.extensions.addOnListChangedCallback

abstract class ObservableListAdapter<DATA, VH : RecyclerView.ViewHolder>(val list: ObservableArrayList<DATA> = ObservableArrayList()) : RecyclerView.Adapter<VH>() {
    init {
        list.addOnListChangedCallback({ notifyDataSetChanged() })
    }

    override final fun getItemCount(): Int = list.size()

    override final fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, list[position])
    }

    abstract fun onBindViewHolder(holder: VH, item: DATA)
}
