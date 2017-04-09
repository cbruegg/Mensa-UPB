package com.cbruegg.mensaupb.adapter

import com.cbruegg.mensaupb.BR
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.HeaderViewModel

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun RestaurantAdapter() = DataBindingAdapter<DbRestaurant>(R.layout.row_restaurant, BR.restaurant)
typealias RestaurantAdapter = DataBindingAdapter<DbRestaurant>


private val headerDelegate = DataBindingViewTypeDelegate<DishListViewModel>(R.layout.row_header, BR.headerViewModel)
private val dishViewModelDelegate = DataBindingViewTypeDelegate<DishListViewModel>(R.layout.row_dish, BR.dishViewModel)

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun DishListViewModelAdapter() = DataBindingAdapter<DishListViewModel> { item ->
    when (item) {
        is HeaderViewModel -> headerDelegate
        is DishViewModel -> dishViewModelDelegate
    }
}
typealias DishListViewModelAdapter = DataBindingAdapter<DishListViewModel>

