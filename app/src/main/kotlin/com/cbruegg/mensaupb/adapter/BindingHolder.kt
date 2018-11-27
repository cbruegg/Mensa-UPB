package com.cbruegg.mensaupb.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import android.view.View

/**
 * A [RecyclerView.ViewHolder] for Android data binding.
 *
 * @see [binding]
 */
class BindingHolder<out T : ViewDataBinding>(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    val binding: T = DataBindingUtil.bind(itemView)!!
}