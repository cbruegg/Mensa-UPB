package com.cbruegg.mensaupb.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * A [RecyclerView.ViewHolder] for Android data binding.
 *
 * @see [binding]
 */
class BindingHolder<out T : ViewDataBinding>(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    val binding: T = DataBindingUtil.bind(itemView)!!
}