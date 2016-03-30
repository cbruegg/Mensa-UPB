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
import kotlin.comparisons.compareByDescending
import kotlin.comparisons.thenBy
import kotlin.comparisons.thenComparator

/**
 * Wrapper for [Dish] objects providing easy access to various attributes for data binding.
 */
data class DishViewModel(@DataBindingProperty val dish: Dish,
                         @DataBindingProperty val headerText: String?,
                         @DataBindingProperty val userPrice: Double,
                         @DataBindingProperty val priceText: String,
                         @DataBindingProperty val allergensText: String,
                         @DataBindingProperty val badgesText: String?) {
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
            val badgesText = dish.badges
                    .filterNotNull()
                    .joinTo(buffer = StringBuilder(), transform = { context.getString(it.descriptionId) })
                    .toString()
                    .capitalizeFirstChar()
            return DishViewModel(dish, headerText, userPrice, priceText, allergensText, badgesText)
        }
    }

    @DataBindingProperty val hasBadges = dish.badges.filterNotNull().isNotEmpty()
    @DataBindingProperty val localizedCategory: String = if (Locale.getDefault().language == Locale.GERMAN.language) dish.germanCategory else dish.category
    @DataBindingProperty val containsAllergens = dish.allergens.isNotEmpty()
    @DataBindingProperty val hasThumbnail = !dish.thumbnailImageUrl.isNullOrEmpty()
    @DataBindingProperty val hasBigImage = !dish.imageUrl.isNullOrEmpty()
    @DataBindingProperty val hasHeader = headerText != null
}

val UserType.dishComparator: Comparator<Dish>
    get() = compareByDescending<Dish> { it.germanCategory } // Sort by category
            .thenComparator { d1, d2 ->
                if (d1.priceType == d2.priceType) 0 else if (d1.priceType == PriceType.FIXED) -1 else 1
            } // Weighted is worse
            .thenBy { selectPrice(it) } // then by actual price

/**
 * Compute the DishViewModels for a list of Dishes.
 */
fun List<Dish>.toDishViewModels(context: Context, userType: UserType): List<DishViewModel> {
    val sortedList = sortedWith(userType.dishComparator)
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