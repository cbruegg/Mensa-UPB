package com.cbruegg.mensaupb.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.RowDishBinding
import com.cbruegg.mensaupb.viewmodel.DishViewModel

/**
 * Adapter responsible for displaying DishViewModels in a RecyclerView.
 */
class DishViewModelAdapter : ObservableListAdapter<DishViewModel, DishViewModelAdapter.DishBindingViewHolder>() {
    override fun onBindViewHolder(holder: DishBindingViewHolder, item: DishViewModel, onClickListener: View.OnClickListener) {
        holder.binding.dishViewModel = item
        holder.binding.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): DishBindingViewHolder {
        return DishBindingViewHolder(inflater.inflate(R.layout.row_dish, parent, false))
    }

    class DishBindingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowDishBinding = DataBindingUtil.bind(itemView)
    }
}
