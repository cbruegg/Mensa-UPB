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
 *
 * @param [delegateFor]  A selector for the delegate to use for the specific instance.
 *                       For the same instance, this should always return the same delegate.
 */
class DataBindingAdapter<DATA : Any>(private val delegateFor: (DATA) -> DataBindingViewTypeDelegate<DATA>)
    : ObservableListAdapter<DATA, BindingHolder<ViewDataBinding>>() {

    private val delegateByViewType = mutableMapOf<Int, DataBindingViewTypeDelegate<DATA>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): BindingHolder<ViewDataBinding> {
        val delegate = delegateByViewType[viewType]!!
        return BindingHolder(inflater.inflate(delegate.layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: BindingHolder<ViewDataBinding>, item: DATA, onClickListener: View.OnClickListener, viewType: Int) {
        val delegate = delegateByViewType[viewType]!!

        holder.binding.setVariable(delegate.modelVar, item)
        holder.binding.setVariable(delegate.onClickListenerVar, onClickListener)
        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int {
        val delegate = delegateFor(list[position])
        val hashCode = delegate.hashCode()
        delegateByViewType[hashCode] = delegate
        return hashCode
    }

}

/**
 * Factory method for [DataBindingAdapter]s supporting only
 * one viewType.
 */
inline fun <reified DATA : Any> DataBindingAdapter(
        /**
         * Layout to use for rows
         */
        @LayoutRes layoutId: Int,
        /**
         * Variable id from [BR] to assign the model [DATA] to.
         */
        modelVar: Int,
        /**
         * Variable id to set the onClickListener to.
         */
        onClickListenerVar: Int = BR.onClickListener
): DataBindingAdapter<DATA> {
    val delegate = DataBindingViewTypeDelegate<DATA>(layoutId, modelVar, onClickListenerVar)
    return DataBindingAdapter<DATA> { delegate }
}

data class DataBindingViewTypeDelegate<DATA>(
        /**
         * Layout to use for rows
         */
        @LayoutRes val layoutId: Int,
        /**
         * Variable id from [BR] to assign the model [DATA] to.
         */
        val modelVar: Int,
        /**
         * Variable id to set the onClickListener to.
         */
        val onClickListenerVar: Int = BR.onClickListener
)