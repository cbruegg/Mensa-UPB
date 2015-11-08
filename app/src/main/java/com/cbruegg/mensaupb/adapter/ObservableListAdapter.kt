package com.cbruegg.mensaupb.adapter

import android.databinding.ObservableArrayList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbruegg.mensaupb.extensions.addOnListChangedCallback

/**
 * An abstract RecyclerView-Adapter that listents for changes in the provided observable list.
 * It also features support for a lambda onClickListener.
 */
abstract class ObservableListAdapter<DATA, VH : RecyclerView.ViewHolder>(val list: ObservableArrayList<DATA> = ObservableArrayList(),
                                                                         var onClickListener: ((data: DATA, position: Int) -> Unit)? = null) : RecyclerView.Adapter<VH>() {

    init {
        list.addOnListChangedCallback({ notifyDataSetChanged() })
    }

    override final fun getItemCount(): Int = list.size

    override final fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, list[position], onClickListener.toInternalOnClickListener(list[position], position))
    }

    override final fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = onCreateViewHolder(parent, viewType, LayoutInflater.from(parent.context))

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): VH

    abstract fun onBindViewHolder(holder: VH, item: DATA, onClickListener: View.OnClickListener)

    /**
     * Since we can specify a lambda OnClickListener, we need to convert that
     * to a normal View.OnClickListener. This method is responsible for that.
     */
    private fun ((data: DATA, position: Int) -> Unit)?.toInternalOnClickListener(data: DATA, position: Int): View.OnClickListener {
        return object : View.OnClickListener {
            override fun onClick(v: View) {
                this@toInternalOnClickListener?.invoke(data, position)
            }
        }
    }
}
