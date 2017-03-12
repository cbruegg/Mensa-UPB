package com.cbruegg.mensaupb.main

import android.os.Bundle
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.mvp.MvpModel
import com.cbruegg.mensaupb.mvp.MvpModelSaver
import com.cbruegg.mensaupb.util.delegates.PersistentPropertyDelegate

data class MainModel(
        var requestedRestaurantId: String?,
        var showDishWithGermanName: String?,
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
            putString("showDishWithGermanName", model.showDishWithGermanName)
        }
    }

    override fun restore(savedInstanceState: Bundle, intoModel: MainModel) {
        intoModel.apply {
            requestedRestaurantId = savedInstanceState.getString("requestedRestaurantId")
            showDishWithGermanName = savedInstanceState.getString("showDishWithGermanName")
        }
    }

}