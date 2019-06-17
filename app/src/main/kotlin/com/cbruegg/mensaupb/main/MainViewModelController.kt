package com.cbruegg.mensaupb.main

import androidx.lifecycle.viewModelScope
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.minus
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.util.OneOff
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Date

private const val MAX_RESTAURANTS_AGE_MS = 24L * 60 * 60 * 1000
private const val DEFAULT_RESTAURANT_NAME = "Mensa Academica"

class MainViewModelController(
    private val repository: Repository,
    private val oneOff: OneOff,
    private val viewModel: MainViewModel,
    private var requestedRestaurantId: String?,
    private var requestedDishName: String?,
    private var requestedSelectedDay: Date?
) : CoroutineScope by viewModel.viewModelScope {

    fun newRequest(requestedRestaurantId: String, requestedDishName: String?, requestedSelectedDay: Date?) {
        this.requestedRestaurantId = requestedRestaurantId
        this.requestedDishName = requestedDishName
        this.requestedSelectedDay = requestedSelectedDay
        viewModel.lastLoadMeta = Date(0)
        reloadIfNeeded()
    }

    fun onRestaurantClick(restaurant: DbRestaurant, currentlyDisplayedDate: Date?) {
        viewModel.drawerShown.data = false
        viewModel.restaurantLoadSpec.data = RestaurantLoadSpec(restaurant, requestedDay = currentlyDisplayedDate)
    }

    private fun showDishesForRestaurant(restaurant: DbRestaurant) {
        viewModel.restaurantLoadSpec.data = RestaurantLoadSpec(restaurant, requestedSelectedDay, requestedDishName)
        requestedSelectedDay = null
        requestedDishName = null
    }

    fun start() {
        reloadIfNeeded()
        runOneOffs()
    }

    private fun runOneOffs() {
        oneOff.launch("appwidget_ad") {
            viewModel.showAppWidgetAd.data = true
        }
    }

    /**
     * If this is the first time the user opens the app, show the drawer so
     * the user knows about its existence.
     */
    private fun checkShowFirstTimeDrawer() {
        oneOff.launch("showFirstTimeDrawer") {
            viewModel.drawerShown.data = true
        }
    }

    /**
     * Load a default restaurant fragment. If found in the list of restaurants,
     * the last used restaurant or else if found the [DEFAULT_RESTAURANT_NAME] is used,
     * else the first item in the list.
     */
    private fun loadDefaultRestaurant(preparedList: List<DbRestaurant>) {
        val restaurant = preparedList.firstOrNull { it.id == requestedRestaurantId }
            ?: preparedList.firstOrNull { it.id == viewModel.restaurantLoadSpec.data?.restaurant?.id }
            ?: preparedList.firstOrNull { it.name.toLowerCase() == DEFAULT_RESTAURANT_NAME.toLowerCase() }
            ?: preparedList.firstOrNull()
        restaurant?.let { showDishesForRestaurant(it) }
        // Clear fulfilled request
        requestedRestaurantId = null
    }

    fun onCameBackFromPreferences() {
        loadDefaultRestaurant(viewModel.restaurants.data)
    }

    /**
     * Refetch the list of restaurants from the cache
     * or the network, reloading the fragments afterwards.
     * This is useful for reloading after receiving a new intent.
     */
    private fun reloadIfNeeded() = launch {
        synchronized(viewModel) {
            val shouldReload = viewModel.lastLoadMeta < now - MAX_RESTAURANTS_AGE_MS ||
                    requestedDishName != null ||
                    requestedRestaurantId != null ||
                    requestedSelectedDay != null
            if (shouldReload) {
                viewModel.lastLoadMeta = now
            } else {
                return@launch
            }
        }

        viewModel.isLoading.data = true
        repository.restaurants(acceptStale = true)
            .fold({
                viewModel.restaurants.data = emptyList()
                viewModel.networkError.data = true
                it.printStackTrace()
            }) { (restaurants, _) ->
                // Since restaurants change rarely, don't show isStale
                val preparedList = restaurants.uiSorted()

                viewModel.restaurants.data = preparedList
                viewModel.networkError.data = false
                checkShowFirstTimeDrawer()
                loadDefaultRestaurant(preparedList)
            }
        viewModel.isLoading.data = false
    }
}