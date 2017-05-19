package com.cbruegg.mensaupb.appwidget

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.viewmodel.uiSorted
import com.cbruegg.sikoanmvp.MvpPresenter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

class DishesAppWidgetConfigPresenter(
        private val repository: Repository,
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

    override fun initView() {
        super.initView()

        view?.setConfirmButtonStatus(false)
        view?.setProgressBarVisible(true)

        launch(UI) {
            view?.setProgressBarVisible(true)
            repository
                    .restaurantsAsync()
                    .await()
                    .fold({ view?.showNetworkError(it) }) { (restaurants, _) ->
                        restaurantList = restaurants.uiSorted()
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