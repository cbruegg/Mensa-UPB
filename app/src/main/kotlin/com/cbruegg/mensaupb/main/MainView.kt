package com.cbruegg.mensaupb.main

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.mvp.MvpView
import java.io.IOException

interface MainView : MvpView {
    fun setDrawerStatus(visible: Boolean)
    fun showDishesForRestaurant(restaurant: DbRestaurant, day: Int?, showDishWithGermanName: String?)
    fun setRestaurants(restaurants: List<DbRestaurant>)
    fun showNetworkError(ioException: IOException)
    fun showAppWidgetAd()
    val currentlyDisplayedDay: Int?
    fun requestWidgetUpdate()
}