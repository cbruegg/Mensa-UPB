package com.cbruegg.mensaupb.main

import android.arch.lifecycle.ViewModel
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.util.MutableLiveData
import java.util.*

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
) : ViewModel()

fun initialMainViewModel() = MainViewModel(
        restaurants = MutableLiveData(emptyList()),
        networkError = MutableLiveData(false),
        drawerShown = MutableLiveData(false),
        showAppWidgetAd = MutableLiveData(false),
        isLoading = MutableLiveData(false),
        restaurantLoadSpec = MutableLiveData(null)
)