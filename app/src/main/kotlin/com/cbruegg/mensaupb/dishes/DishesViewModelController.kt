package com.cbruegg.mensaupb.dishes

import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.minus
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.Date

private const val MAX_DISH_AGE_MS = 3L * 60 * 60 * 1000

class DishesViewModelController(
        private val repository: Repository,
        private val restaurant: DbRestaurant,
        private val date: Date,
        private val userType: UserType,
        private val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishListViewModel>,
        private val dishNameToShowOnLoad: String?,
        private val viewModel: DishesViewModel
) {

    fun onDishClicked(dishListViewModel: DishListViewModel) {
        if (dishListViewModel is DishViewModel && dishListViewModel.hasBigImage) {
            viewModel.showDialogFor.data = dishListViewModel
        }
    }

    fun onDetailsDialogDismissed() {
        viewModel.showDialogFor.data = null
    }

    fun reloadIfNeeded() = launch(UI) {
        synchronized(viewModel) {
            val shouldReload = viewModel.lastLoadMeta < now - MAX_DISH_AGE_MS
            if (shouldReload) {
                viewModel.lastLoadMeta = now
            } else {
                return@launch
            }
        }

        viewModel.isLoading.data = true
        repository.dishesAsync(restaurant, date, acceptStale = true)
                .await()
                .fold({
                    viewModel.dishViewModels.data = emptyList()
                    viewModel.networkError.data = true
                    viewModel.isStale.data = false
                    it.printStackTrace()
                }) { (dishes, isStale) ->
                    val dishListViewModels = dishes.dishViewModelCreator(userType)
                    val argDish = if (dishNameToShowOnLoad == null) null else dishListViewModels.asSequence()
                            .filterIsInstance<DishViewModel>()
                            .firstOrNull {
                                it.dish.name == dishNameToShowOnLoad && it.hasThumbnail
                            }

                    viewModel.dishViewModels.data = dishListViewModels
                    viewModel.networkError.data = false
                    viewModel.isStale.data = isStale
                    viewModel.showDialogFor.data = argDish
                }
        viewModel.isLoading.data = false
    }
}