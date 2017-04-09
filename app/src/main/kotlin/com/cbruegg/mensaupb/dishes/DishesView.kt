package com.cbruegg.mensaupb.dishes

import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.sikoanmvp.MvpView
import java.io.IOException

interface DishesView : MvpView {
    fun showDishDetailsDialog(dishViewModel: DishViewModel)

    /**
     * Set whether or not to show a message saying that
     * no dishes were found.
     */
    fun setShowNoDishesMessage(showMessage: Boolean)

    fun showNetworkError(e: IOException)

    fun showDishes(dishes: List<DishListViewModel>)
}