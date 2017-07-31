package com.cbruegg.mensaupb.restaurant

import android.arch.lifecycle.ViewModel
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.util.MutableLiveData
import java.util.Date

data class RestaurantViewModel(
        val pagerInfo: MutableLiveData<PagerInfo>,
        val restaurant: DbRestaurant,
        var requestedDishName: String?,
        var lastLoadMeta: LastLoadMeta? = null
) : ViewModel()

data class PagerInfo(var position: Date, val dates: List<Date>)
data class LastLoadMeta(val pagerDates: List<Date>, val whenLoaded: Date)