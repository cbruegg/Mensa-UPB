package com.cbruegg.mensaupb.restaurant

import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.cache.oldestAllowedCacheDate
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.extensions.inRangeOrNull
import com.cbruegg.mensaupb.util.MutableLiveData
import java.util.*
import java.util.concurrent.TimeUnit

fun initialRestaurantViewModel(requestedPagerPosition: Date?, restaurant: DbRestaurant, requestedDishName: String?): RestaurantViewModel {
    val dates = computePagerDates()
    val restrictedPagerPosition = requestedPagerPosition?.inRangeOrNull(
            dates.first(),
            dates.last()
    ) ?: dates.first()
    val viewModel = RestaurantViewModel(
            pagerInfo = MutableLiveData(PagerInfo(restrictedPagerPosition, dates)),
            restaurant = restaurant,
            requestedDishName = requestedDishName
    )
    return viewModel
}

private const val DAY_COUNT = 7L

/**
 * Return a list of dates to be used for fetching dishes.
 */
private fun computePagerDates(): List<Date> {
    val today = System.currentTimeMillis()
    val dayInMs = TimeUnit.DAYS.toMillis(1)
    return (0..DAY_COUNT - 1).map { Date(today + it * dayInMs).atMidnight }
}

class RestaurantViewModelController(private val restaurantViewModel: RestaurantViewModel) {

    fun reloadIfNeeded() {
        val shouldReload = restaurantViewModel.lastLoadMeta?.let { (pagerDates, whenLoaded) ->
            whenLoaded < oldestAllowedCacheDate || pagerDates != computePagerDates()
        } ?: true

        if (shouldReload) {
            reload()
        }
    }

    private fun reload() {
        val dates = computePagerDates()
        val requestedPagerPosition = restaurantViewModel.pagerInfo.data.position
        val restrictedPagerPosition = requestedPagerPosition.inRangeOrNull(
                dates.first(),
                dates.last()
        ) ?: dates.first()

        restaurantViewModel.apply {
            pagerInfo.data = PagerInfo(restrictedPagerPosition, dates)
        }
    }

}