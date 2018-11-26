package com.cbruegg.mensaupb.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * A [RecyclerView.ViewHolder] for Android data binding.
 *
 * @see [binding]
 */
class BindingHolder<out T : ViewDataBinding>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val binding: T = DataBindingUtil.bind(itemView)!!
}