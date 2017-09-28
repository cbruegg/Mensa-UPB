package com.cbruegg.mensaupb.adapter

import com.cbruegg.mensaupb.BR
import com.cbruegg.mensaupb.GlideRequests
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.HeaderViewModel

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun RestaurantAdapter(glide: GlideRequests) = DataBindingAdapter<DbRestaurant>(R.layout.row_restaurant, BR.restaurant,
        imageId = null, glide = glide) { null }


private val headerDelegate = DataBindingViewTypeDelegate<DishListViewModel>(R.layout.row_header, BR.headerViewModel)
private val dishViewModelDelegate = DataBindingViewTypeDelegate<DishListViewModel>(R.layout.row_dish, BR.dishViewModel, imageId = R.id.row_dish_image)

/**
 * Adapter responsible for displaying Restaurants in a RecyclerView.
 */
fun DishListViewModelAdapter(glide: GlideRequests) = DataBindingAdapter<DishListViewModel>(
        glide,
        imageUrlGetter = { item -> (item as? DishViewModel)?.dish?.thumbnailImageUrl },
        delegateFor = { item ->
            when (item) {
                is HeaderViewModel -> headerDelegate
                is DishViewModel -> dishViewModelDelegate
            }
        }
)
