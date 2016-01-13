package com.cbruegg.mensaupb.viewmodel

import android.content.Context
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.extensions.capitalizeFirstChar
import com.cbruegg.mensaupb.extensions.replace
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.model.UserType
import java.text.DecimalFormat
import java.util.*

/**
 * Wrapper for [Dish] objects providing easy access to various attributes for data binding.
 */
data class DishViewModel(val dish: Dish,
                         val headerText: String?,
                         val userPrice: Double,
                         val priceText: String,
                         val allergensText: String,
                         val badgesText: String) {

    companion object {
        private val NUMBER_FORMAT = DecimalFormat("0.00")

        fun create(dish: Dish, headerText: String?, userType: UserType, context: Context): DishViewModel {
            val userPrice = when (userType) {
                UserType.STUDENT -> dish.studentPrice
                UserType.WORKER -> dish.workerPrice
                UserType.GUEST -> dish.guestPrice
            }
            val priceText = "${context.getString(R.string.price)} ${NUMBER_FORMAT.format(userPrice)} € ${if (dish.priceType == PriceType.WEIGHTED) context.getString(R.string.per_100_gramm) else ""}"
            val allergensText = "${context.getString(R.string.allergens)} ${dish.allergens.replace("A1", "A1 (Gluten)").joinToString()}"
            val badgesText = dish.badges.joinTo(buffer = StringBuilder(), transform = { context.getString(it.descriptionId) }).toString().capitalizeFirstChar()
            return DishViewModel(dish, headerText, userPrice, priceText, allergensText, badgesText)
        }
    }

    val hasBadges = dish.badges.isNotEmpty()
    val localizedCategory: String = if (Locale.getDefault().language == Locale.GERMAN.language) dish.germanCategory else dish.category
    val containsAllergens = dish.allergens.isNotEmpty()
    val hasThumbnail = !dish.thumbnailImageUrl.isNullOrEmpty()
    val hasBigImage = !dish.imageUrl.isNullOrEmpty()
    val hasHeader = headerText != null
}

/**
 * Compute the DishViewModels for a list of Dishes.
 */
fun List<Dish>.toDishViewModels(context: Context, userType: UserType): List<DishViewModel> {
    val sortedList = sortedBy { it.germanCategory }
            .reversed()
            .asSequence()
            .groupBy { it.germanCategory }
            .values
            .flatMap { it.sortedBy { userType.selectPrice(it) } }
            .toList()
    return sortedList.mapIndexed { position, dish -> DishViewModel.create(dish, headerTextForIndex(position, sortedList), userType, context) }
}

/**
 * Check if the dish is the first element of a category in the list.
 * Useful for determining whether a header should be displayed.
 */
private fun isFirstInCategory(index: Int, dishes: List<Dish>): Boolean {
    val indexDish = dishes[index]
    val previousDish = if (index - 1 >= 0) dishes[index - 1] else null
    return indexDish.germanCategory != previousDish?.germanCategory
}

private fun headerTextForIndex(index: Int, dishes: List<Dish>): String?
        = if (isFirstInCategory(index, dishes)) dishes[index].germanCategory else null