package com.cbruegg.mensaupb.adapter

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import com.cbruegg.mensaupb.model.Restaurant

class RestaurantSpinnerAdapter(context: Context, val restaurants: List<Restaurant>) : ArrayAdapter<String>(context, R.layout.simple_spinner_item) {
    init {
        setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
    }

    override fun getCount() = restaurants.size

    override fun getItem(position: Int) = restaurants[position].name
}