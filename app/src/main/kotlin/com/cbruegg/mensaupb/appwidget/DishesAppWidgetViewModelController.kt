package com.cbruegg.mensaupb.appwidget

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

class DishesAppWidgetViewModelController(
    private val repository: Repository,
    private val dishesWidgetConfigurationManager: DishesWidgetConfigurationManager,
    private val appWidgetId: Int,
    private val viewModel: DishesAppWidgetViewModel
) {

    private var restaurantList = emptyList<DbRestaurant>()

    fun onConfirmClicked(restaurantItemIndex: Int) {
        val selectedRestaurant = restaurantList[restaurantItemIndex]
        dishesWidgetConfigurationManager.putConfiguration(appWidgetId, DishesWidgetConfiguration(selectedRestaurant.id))
        viewModel.closed.data = true
    }

    fun load() = GlobalScope.launch(Dispatchers.Main) {
        viewModel.loadingMutex.withLock {
            if (viewModel.restaurants.data.isNotEmpty() || viewModel.networkError.data) {
                return@withLock
            }

            viewModel.showProgress.data = true
            repository
                .restaurantsAsync()
                .await()
                .fold({
                    viewModel.networkError.data = true
                    it.printStackTrace()
                }) { (restaurants, _) ->
                    restaurantList = restaurants.uiSorted()
                    viewModel.restaurants.data = restaurantList
                    viewModel.networkError.data = false
                    viewModel.confirmButtonStatus.data = true
                }
            viewModel.showProgress.data = false
        }
    }

    fun onCancel() {
        viewModel.closed.data = true
    }

}