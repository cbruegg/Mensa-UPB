package com.cbruegg.mensaupb.main

import android.os.Bundle
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.util.delegates.PersistentPropertyDelegate
import com.cbruegg.sikoanmvp.MvpModel
import com.cbruegg.sikoanmvp.MvpModelSaver
import java.util.*

data class MainModel(
        var requestedRestaurantId: String?,
        var requestedDishWithName: String?,
        var requestedSelectedDay: Date?,
        private val lastRestaurantIdDelegate: PersistentPropertyDelegate<String?>,
        var restaurants: List<DbRestaurant>? = null
) : MvpModel {
    var lastRestaurantId: String? by lastRestaurantIdDelegate
    fun lastRestaurant() = restaurants?.firstOrNull { it.id == lastRestaurantId }
}

object MainModelSaver : MvpModelSaver<MainModel> {

    private const val requestedRestaurantIdKey = "requestedRestaurantId"
    private const val requestedDishWithNameKey = "requestedDishWithName"
    private const val requestedSelectedDayKey = "requestedSelectedDay"

    override fun save(model: MainModel, savedInstanceState: Bundle) {
        savedInstanceState.apply {
            putString(requestedRestaurantIdKey, model.requestedRestaurantId)
            putString(requestedDishWithNameKey, model.requestedDishWithName)
            putDate(requestedSelectedDayKey, model.requestedSelectedDay)
        }
    }

    override fun restore(savedInstanceState: Bundle, intoModel: MainModel) {
        intoModel.apply {
            requestedRestaurantId = savedInstanceState.getString(requestedRestaurantIdKey)
            requestedDishWithName = savedInstanceState.getString(requestedDishWithNameKey)
            requestedSelectedDay = savedInstanceState.getDate(requestedSelectedDayKey)
        }
    }

}