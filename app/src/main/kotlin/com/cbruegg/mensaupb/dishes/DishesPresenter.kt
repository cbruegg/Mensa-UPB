package com.cbruegg.mensaupb.dishes

import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupb.mvp.MvpPresenter
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import kotlinx.coroutines.experimental.launch
import java.util.*

class DishesPresenter(
        private val downloader: Downloader,
        private val restaurant: DbRestaurant,
        private val date: Date,
        private val userType: UserType,
        private val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishViewModel>,
        private val dishNameToShowOnLoad: String?
) : MvpPresenter<DishesView>() {

    fun onDishClicked(dishViewModel: DishViewModel) {
        if (dishViewModel.hasBigImage) {
            view?.showDishDetailsDialog(dishViewModel)
        }
    }

    /**
     * If the dishName parameter is non-null,
     * try to find a matching dish and display its image.
     */
    private fun tryShowArgDish(dishViewModels: List<DishViewModel>) {
        if (dishNameToShowOnLoad == null) {
            return
        }

        dishViewModels.firstOrNull {
            it.dish.name == dishNameToShowOnLoad && it.hasThumbnail
        }?.let {
            view?.showDishDetailsDialog(it)
        }
    }

    override fun initView() {
        super.initView()

        launch(MainThread) {
            downloader.downloadOrRetrieveDishesAsync(restaurant, date)
                    .await()
                    .fold({
                        view?.showDishes(emptyList())
                        view?.showNetworkError(it)
                    }) {
                        view?.setShowNoDishesMessage(it.isEmpty())
                        val dishViewModels = it.dishViewModelCreator(userType)
                        tryShowArgDish(dishViewModels)
                        view?.showDishes(dishViewModels)
                    }
        }.register()
    }
}