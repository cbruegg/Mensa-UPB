package com.cbruegg.mensaupb.appwidget

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.viewmodel.BaseViewModel
import kotlinx.coroutines.sync.Mutex

data class DishesAppWidgetViewModel(
    val networkError: MutableLiveData<Boolean>,
    val restaurants: MutableLiveData<List<DbRestaurant>>,
    val confirmButtonStatus: MutableLiveData<Boolean>,
    val showProgress: MutableLiveData<Boolean>,
    val closed: MutableLiveData<Boolean>,
    val loadingMutex: Mutex = Mutex()
) : BaseViewModel()

fun initialDishesAppWidgetViewModel() = DishesAppWidgetViewModel(
    networkError = MutableLiveData(false),
    restaurants = MutableLiveData(emptyList()),
    confirmButtonStatus = MutableLiveData(false),
    showProgress = MutableLiveData(false),
    closed = MutableLiveData(false)
)