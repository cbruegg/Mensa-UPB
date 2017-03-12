package com.cbruegg.mensaupb.main

import android.os.Bundle
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.mvp.MvpModel
import com.cbruegg.mensaupb.mvp.MvpModelSaver
import com.cbruegg.mensaupb.util.delegates.PersistentPropertyDelegate

data class MainModel(
        var requestedRestaurantId: String?,
        var requestedDishWithGermanName: String?,
        var requestedSelectedDay: Int?,
        private val lastRestaurantIdDelegate: PersistentPropertyDelegate<String?>,
        var restaurants: List<DbRestaurant>? = null
) : MvpModel {
    var lastRestaurantId: String? by lastRestaurantIdDelegate
    fun lastRestaurant() = restaurants?.firstOrNull { it.id == lastRestaurantId }
}

object MainModelSaver : MvpModelSaver<MainModel> {

    override fun save(model: MainModel, savedInstanceState: Bundle) {
        savedInstanceState.apply {
            putString("requestedRestaurantId", model.requestedRestaurantId)
            putString("requestedDishWithGermanName", model.requestedDishWithGermanName)
            putInt("requestedSelectedDay", model.requestedSelectedDay ?: -1)
        }
    }

    override fun restore(savedInstanceState: Bundle, intoModel: MainModel) {
        intoModel.apply {
            requestedRestaurantId = savedInstanceState.getString("requestedRestaurantId")
            requestedDishWithGermanName = savedInstanceState.getString("requestedDishWithGermanName")
            requestedSelectedDay = savedInstanceState.getInt("requestedSelectedDay", -1).let { if (it == -1) null else it }
        }
    }

}