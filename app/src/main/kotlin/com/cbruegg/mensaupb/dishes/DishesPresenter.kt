package com.cbruegg.mensaupb.dishes

import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.sikoanmvp.MvpPresenter
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*

class DishesPresenter(
        private val downloader: Downloader,
        private val restaurant: DbRestaurant,
        private val date: Date,
        private val userType: UserType,
        private val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishListViewModel>,
        private val dishNameToShowOnLoad: String?
) : MvpPresenter<DishesView>() {

    fun onDishClicked(distListViewModel: DishListViewModel) {
        if (distListViewModel is DishViewModel && distListViewModel.hasBigImage) {
            view?.showDishDetailsDialog(distListViewModel)
        }
    }

    /**
     * If the dishName parameter is non-null,
     * try to find a matching dish and display its image.
     */
    private fun tryShowArgDish(dishListViewModels: List<DishListViewModel>) {
        if (dishNameToShowOnLoad == null) {
            return
        }

        dishListViewModels.asSequence()
                .filterIsInstance<DishViewModel>()
                .firstOrNull {
                    it.dish.name == dishNameToShowOnLoad && it.hasThumbnail
                }?.let {
            view?.showDishDetailsDialog(it)
        }
    }

    override fun initView() {
        super.initView()

        launch(UI) {
            view?.isLoading = true
            downloader.downloadOrRetrieveDishesAsync(restaurant, date, acceptStale = true)
                    .await()
                    .fold({
                        view?.showDishes(emptyList())
                        view?.showNetworkError(it)
                    }) { (dishes, isStale) ->
                        view?.run {
                            val dishViewModels = dishes.dishViewModelCreator(userType)
                            showStale(isStale)
                            setShowNoDishesMessage(dishes.isEmpty())
                            tryShowArgDish(dishViewModels)
                            showDishes(dishViewModels)
                        }
                    }
            view?.isLoading = false
        }.register()
    }
}