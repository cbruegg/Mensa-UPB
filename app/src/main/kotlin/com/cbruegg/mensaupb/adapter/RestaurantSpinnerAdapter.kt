package com.cbruegg.mensaupb.adapter

import android.content.Context
import android.widget.ArrayAdapter
import com.cbruegg.mensaupb.cache.DbRestaurant

/**
 * A convenience implementation of an ArrayAdapter
 * specific for [Restaurant]s.
 */
class RestaurantSpinnerAdapter(context: Context, val restaurants: List<DbRestaurant>) :
    ArrayAdapter<String>(context, android.R.layout.simple_spinner_item) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    override fun getCount() = restaurants.size

    override fun getItem(position: Int) = restaurants[position].name
}