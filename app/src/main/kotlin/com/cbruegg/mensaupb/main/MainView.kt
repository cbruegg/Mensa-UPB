package com.cbruegg.mensaupb.main

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.sikoanmvp.MvpView
import java.io.IOException
import java.util.*

interface MainView : MvpView {
    fun setDrawerStatus(visible: Boolean)
    fun showDishesForRestaurant(restaurant: DbRestaurant, day: Date?, showDishWithGermanName: String?)
    fun setRestaurants(restaurants: List<DbRestaurant>)
    fun showNetworkError(ioException: IOException)
    fun showAppWidgetAd()
    val currentlyDisplayedDay: Date?
    fun requestWidgetUpdate()
    var isLoading: Boolean
}