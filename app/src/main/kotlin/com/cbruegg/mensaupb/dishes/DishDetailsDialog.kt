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
    val imageUrl = dishViewModel.dish.imageUrl
    startActivity(
        DishDetailsActivity.createStartIntent(
            context = this,
            imageUrl = imageUrl,
            priceText = dishViewModel.priceText,
            badgesText = dishViewModel.badgesText?.takeIf { dishViewModel.hasBadges },
            allergensText = dishViewModel.allergensText
                .takeIf { dishViewModel.containsAllergens }
                ?.substringAfter(": ")
                ?.trim(),
            nutritionalValuesText = dishViewModel.nutritionalValuesText
                ?.takeIf { dishViewModel.hasNutritionalValues }
                ?.substringAfter('\n', dishViewModel.nutritionalValuesText.orEmpty())
                ?.replace(", ", "\n")
        )
    )
}
