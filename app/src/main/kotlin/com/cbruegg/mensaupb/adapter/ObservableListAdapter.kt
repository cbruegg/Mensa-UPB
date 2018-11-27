package com.cbruegg.mensaupb.adapter

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * An abstract RecyclerView-Adapter that listens for changes in the provided observable list.
 * It also features support for a lambda onClickListener.
 */
abstract class ObservableListAdapter<DATA, VH : androidx.recyclerview.widget.RecyclerView.ViewHolder>(
    val list: ObservableArrayList<DATA> = ObservableArrayList(),
    var onClickListener: ((data: DATA, position: Int) -> Unit)? = null
) : androidx.recyclerview.widget.RecyclerView.Adapter<VH>() {

    init {
        list.addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableList<DATA>>() {
            override fun onChanged(sender: ObservableList<DATA>) {
                notifyDataSetChanged()
            }

            override fun onItemRangeRemoved(sender: ObservableList<DATA>, positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }

            override fun onItemRangeInserted(sender: ObservableList<DATA>, positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeMoved(sender: ObservableList<DATA>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onItemRangeChanged(sender: ObservableList<DATA>, positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }
        })
    }

    override final fun getItemCount(): Int = list.size

    override final fun onBindViewHolder(holder: VH, position: Int) {
        onBindViewHolder(holder, list[position], onClickListener.toInternalOnClickListener(list[position], position), getItemViewType(position))
    }

    override final fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = onCreateViewHolder(parent, viewType, LayoutInflater.from(parent.context))

    abstract fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): VH

    abstract fun onBindViewHolder(holder: VH, item: DATA, onClickListener: View.OnClickListener, viewType: Int)

    /**
     * Since we can specify a lambda OnClickListener, we need to convert that
     * to a normal View.OnClickListener. This method is responsible for that.
     */
    private fun ((data: DATA, position: Int) -> Unit)?.toInternalOnClickListener(data: DATA, position: Int): View.OnClickListener {
        return View.OnClickListener { this@toInternalOnClickListener?.invoke(data, position) }
    }
}
