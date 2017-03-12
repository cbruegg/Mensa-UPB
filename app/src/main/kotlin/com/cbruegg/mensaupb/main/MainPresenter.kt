package com.cbruegg.mensaupb.main

import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.mvp.ModelMvpPresenter
import com.cbruegg.mensaupb.util.OneOff
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.experimental.launch

class MainPresenter(
        private val downloader: Downloader,
        private val oneOff: OneOff,
        model: MainModel
) : ModelMvpPresenter<MainView, MainModel>(model, MainModelSaver) {

    private val DEFAULT_RESTAURANT_NAME = "Mensa Academica"

    fun onRestaurantsReloadRequested() {
        reload()
    }

    fun onRestaurantClick(restaurant: DbRestaurant) {
        view?.setDrawerStatus(false)
        showDishesForRestaurant(restaurant)
    }

    private fun showDishesForRestaurant(restaurant: DbRestaurant) {
        model.lastRestaurantId = restaurant.id
        view?.showDishesForRestaurant(restaurant, view?.currentlyDisplayedDay, model.showDishWithGermanName)
    }

    override fun initView() {
        super.initView()

        reload()
        runOneOffs()
        view?.requestWidgetUpdate()
    }

    private fun runOneOffs() {
        oneOff.launch("appwidget_ad") {
            view?.showAppWidgetAd()
        }
    }

    /**
     * If this is the first time the user opens the app, show the drawer so
     * the user knows about its existence.
     */
    private fun checkShowFirstTimeDrawer() {
        oneOff.launch("showFirstTimeDrawer") {
            view?.setDrawerStatus(true)
        }
    }

    /**
     * Load a default restaurant fragment. If found in the list of restaurants,
     * the last used restaurant or else if found the [DEFAULT_RESTAURANT_NAME] is used,
     * else the first item in the list.
     */
    private fun loadDefaultRestaurant(preparedList: List<DbRestaurant>) {
        val restaurant = preparedList.firstOrNull { it.id == model.requestedRestaurantId }
                ?: preparedList.firstOrNull { it.id == model.lastRestaurantId }
                ?: preparedList.firstOrNull { it.name.toLowerCase() == DEFAULT_RESTAURANT_NAME.toLowerCase() }
                ?: preparedList.firstOrNull()
        restaurant?.let { showDishesForRestaurant(it) }
    }

    fun onCameBackFromPreferences() {
        model.lastRestaurant()?.let {
            showDishesForRestaurant(it)
        }
    }

    /**
     * Refetch the list of restaurants from the cache
     * or the network, reloading the fragments afterwards.
     * This is useful for reloading after receiving a new intent.
     */
    private fun reload() {
        launch(MainThread) {
            downloader.downloadOrRetrieveRestaurantsAsync()
                    .await()
                    .fold({
                        view?.setRestaurants(emptyList())
                        view?.showNetworkError(it)
                    }) {
                        val preparedList = it.uiSorted()
                        model.restaurants = preparedList
                        view?.setRestaurants(preparedList)
                        checkShowFirstTimeDrawer()
                        loadDefaultRestaurant(preparedList)
                    }
        }.register()
    }
}