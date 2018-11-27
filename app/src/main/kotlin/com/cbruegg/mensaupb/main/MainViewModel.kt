package com.cbruegg.mensaupb.main

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.viewmodel.BaseViewModel
import java.util.Date

data class RestaurantLoadSpec(
    val restaurant: DbRestaurant,
    var requestedDay: Date? = null,
    var requestedDishName: String? = null
)

data class MainViewModel(
    val restaurants: MutableLiveData<List<DbRestaurant>>,
    val networkError: MutableLiveData<Boolean>,
    val drawerShown: MutableLiveData<Boolean>,
    val showAppWidgetAd: MutableLiveData<Boolean>,
    val isLoading: MutableLiveData<Boolean>,
    val restaurantLoadSpec: MutableLiveData<RestaurantLoadSpec?>,
    var lastLoadMeta: Date = Date(0)
) : BaseViewModel()

fun initialMainViewModel() = MainViewModel(
    restaurants = MutableLiveData(emptyList()),
    networkError = MutableLiveData(false),
    drawerShown = MutableLiveData(false),
    showAppWidgetAd = MutableLiveData(false),
    isLoading = MutableLiveData(false),
    restaurantLoadSpec = MutableLiveData(null)
)