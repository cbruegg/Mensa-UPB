package com.cbruegg.mensaupb.adapter

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.RowRestaurantBinding
import com.cbruegg.mensaupb.model.Restaurant

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
class RestaurantAdapter : ObservableListAdapter<Restaurant, RestaurantAdapter.RestaurantBindingHolder>() {

    override fun onBindViewHolder(holder: RestaurantBindingHolder, item: Restaurant, onClickListener: View.OnClickListener) {
        holder.binding.restaurant = item
        holder.binding.onClickListener = onClickListener
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int, inflater: LayoutInflater): RestaurantBindingHolder {
        return RestaurantBindingHolder(inflater.inflate(R.layout.row_restaurant, parent, false))
    }

    class RestaurantBindingHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowRestaurantBinding = DataBindingUtil.bind(itemView)
    }
}
