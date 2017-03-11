package com.cbruegg.mensaupb.appwidget

import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfiguration
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.mvp.MvpPresenter
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.experimental.launch

class DishesAppWidgetConfigPresenter(
        private val downloader: Downloader,
        private val dishesWidgetConfigurationManager: DishesWidgetConfigurationManager,
        private val appWidgetId: Int
) : MvpPresenter<DishesAppWidgetConfigView>() {

    private var restaurantList = emptyList<DbRestaurant>()

    fun onConfirmClicked(restaurantItemIndex: Int) {
        val selectedRestaurant = restaurantList[restaurantItemIndex]
        dishesWidgetConfigurationManager.putConfiguration(appWidgetId, DishesWidgetConfiguration(selectedRestaurant.id))
        view?.updateWidget()
        view?.close(true)
    }

    override fun onViewAttached() {
        super.onViewAttached()

        view?.setConfirmButtonStatus(false)
        view?.setProgressBarVisible(true)

        launch(MainThread) {
            view?.setProgressBarVisible(true)
            downloader
                    .downloadOrRetrieveRestaurantsAsync()
                    .await()
                    .fold({ view?.showNetworkError(it) }) {
                        restaurantList = it.uiSorted()
                        view?.setRestaurantSpinnerList(restaurantList)
                        view?.setConfirmButtonStatus(true)
                    }
            view?.setProgressBarVisible(false)
        }.register()
    }

    fun onCancel() {
        view?.close(false)
    }

}