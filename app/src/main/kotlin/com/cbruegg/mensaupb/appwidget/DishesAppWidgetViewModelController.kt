package com.cbruegg.mensaupb.appwidget

import androidx.lifecycle.viewModelScope
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock

class DishesAppWidgetViewModelController(
    private val repository: Repository,
    private val dishesWidgetConfigurationManager: DishesWidgetConfigurationManager,
    private val appWidgetId: Int,
    private val viewModel: DishesAppWidgetViewModel
) : CoroutineScope by viewModel.viewModelScope {

    private var restaurantList = emptyList<DbRestaurant>()

    fun onConfirmClicked(restaurantItemIndex: Int) {
        val selectedRestaurant = restaurantList[restaurantItemIndex]
        dishesWidgetConfigurationManager.putConfiguration(appWidgetId, DishesWidgetConfiguration(selectedRestaurant.id))
        viewModel.status.data = Status.Confirmed
    }

    fun load() = launch {
        viewModel.loadingMutex.withLock {
            if (viewModel.restaurants.data.isNotEmpty() || viewModel.networkError.data) {
                return@withLock
            }

            viewModel.showProgress.data = true
            repository
                .restaurants()
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
        viewModel.status.data = Status.Canceled
    }

}