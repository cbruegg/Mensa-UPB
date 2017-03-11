package com.cbruegg.mensaupb.adapter

import com.cbruegg.mensaupb.BR
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.RowDishBinding
import com.cbruegg.mensaupb.databinding.RowRestaurantBinding
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.viewmodel.DishViewModel

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun RestaurantAdapter() = DataBindingAdapter<RowRestaurantBinding, Restaurant>(R.layout.row_restaurant, BR.restaurant)
typealias RestaurantAdapter = DataBindingAdapter<RowRestaurantBinding, Restaurant>

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun DishViewModelAdapter() = DataBindingAdapter<RowDishBinding, DishViewModel>(R.layout.row_dish, BR.dishViewModel)
typealias DishViewModelAdapter = DataBindingAdapter<RowDishBinding, DishViewModel>

