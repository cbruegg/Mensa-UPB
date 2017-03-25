package com.cbruegg.mensaupb.appwidget

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.sikoanmvp.MvpView
import java.io.IOException

interface DishesAppWidgetConfigView : MvpView {
    fun showNetworkError(e: IOException)
    fun setRestaurantSpinnerList(list: List<DbRestaurant>)
    fun setConfirmButtonStatus(enabled: Boolean)
    fun setProgressBarVisible(visible: Boolean)
    fun updateWidget()
    fun close(success: Boolean)
}