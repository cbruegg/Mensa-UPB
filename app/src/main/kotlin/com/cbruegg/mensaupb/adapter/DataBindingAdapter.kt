package com.cbruegg.mensaupb.adapter

import android.databinding.ViewDataBinding
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbruegg.mensaupb.BR

/**
 * A RecyclerView adapter that auto-updates the RecyclerView on changes
 * of the [list]. Row views are created automatically. Data binding is used
 * to load data into the rows.
 */
class DataBindingAdapter<T : ViewDataBinding, DATA>(
        /**
         * Layout to use for rows
         */
        @LayoutRes private val layoutId: Int,
        /**
         * Variable id from [BR] to assign the model [DATA] to.
         */
        private val modelVar: Int,
        /**
         * Variable id to set the onClickListener to.
         */
        private val onClickListenerVar: Int = BR.onClickListener
) : ObservableListAdapter<DATA, BindingHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BindingHolder<T> =
            BindingHolder(inflater.inflate(layoutId, parent, false))

    override fun onBindViewHolder(holder: BindingHolder<T>, item: DATA, onClickListener: View.OnClickListener) {
        holder.binding.setVariable(modelVar, item)
        holder.binding.setVariable(onClickListenerVar, onClickListener)
        holder.binding.executePendingBindings()
    }

}