package com.cbruegg.mensaupb.dishes

import com.cbruegg.mensaupb.mvp.MvpView
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import java.io.IOException

interface DishesView : MvpView {
    fun showDishDetailsDialog(dishViewModel: DishViewModel)

    /**
     * Set whether or not to show a message saying that
     * no dishes were found.
     */
    fun setShowNoDishesMessage(showMessage: Boolean)

    fun showNetworkError(e: IOException)

    fun showDishes(dishes: List<DishViewModel>)
}