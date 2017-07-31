package com.cbruegg.mensaupb.dishes

import android.arch.lifecycle.ViewModel
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import java.util.Date

data class DishesViewModel(
        val isLoading: MutableLiveData<Boolean>,
        val networkError: MutableLiveData<Boolean>,
        val dishViewModels: MutableLiveData<List<DishListViewModel>>,
        val isStale: MutableLiveData<Boolean>,
        val showDialogFor: MutableLiveData<DishViewModel?>,
        var lastLoadMeta: Date = Date(0)
) : ViewModel()

fun initialDishesViewModel() = DishesViewModel(
        isLoading = MutableLiveData(true),
        networkError = MutableLiveData(false),
        dishViewModels = MutableLiveData(emptyList()),
        isStale = MutableLiveData(false),
        showDialogFor = MutableLiveData(null)
)