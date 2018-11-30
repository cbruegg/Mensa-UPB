package com.cbruegg.mensaupb.dishes

import android.annotation.SuppressLint
import android.content.Context
import com.cbruegg.mensaupb.activity.DishDetailsActivity
import com.cbruegg.mensaupb.viewmodel.DishViewModel

/**
 * Show a dialog that displays the full size image of the dish.
 * @param dishViewModel DishViewModel with an imageUrl
 */
@SuppressLint("InflateParams")
fun Context.showDishDetailsDialog(dishViewModel: DishViewModel) {
    val fullText = buildString {
        if (dishViewModel.containsAllergens) {
            append(dishViewModel.allergensText).append("\n")
        }
        if (dishViewModel.hasBadges) {
            append(dishViewModel.badgesText).append("\n")
        }
        append(dishViewModel.priceText)
    }

    val imageUrl = dishViewModel.dish.imageUrl
    startActivity(DishDetailsActivity.createStartIntent(this, imageUrl, fullText))
}