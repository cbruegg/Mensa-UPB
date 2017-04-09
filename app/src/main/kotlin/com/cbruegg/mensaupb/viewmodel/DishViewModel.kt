package com.cbruegg.mensaupb.viewmodel

import android.content.Context
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.compat.Html
import com.cbruegg.mensaupb.extensions.capitalizeFirstChar
import com.cbruegg.mensaupb.extensions.replace
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.model.UserType
import java.text.DecimalFormat
import java.util.*

/**
 * Wrapper for [DbDish] objects providing easy access to various attributes for data binding.
 */
data class DishViewModel(
        val dish: DbDish,
        val headerText: String?,
        val userPrice: Double,
        val priceText: String,
        val allergensText: String,
        val badgesText: String?,
        val position: Int,
        val name: String,
        val description: CharSequence
) {
    val hasBadges = dish.badges.filterNotNull().isNotEmpty()
    val containsAllergens = dish.allergens.isNotEmpty()
    val hasThumbnail = !dish.thumbnailImageUrl.isNullOrEmpty()
    val hasBigImage = !dish.imageUrl.isNullOrEmpty()
    val hasHeader = headerText != null
    val showDivider = hasHeader && position > 0
}

private val NUMBER_FORMAT = DecimalFormat("0.00")

private fun DbDish.toDishViewModel(headerText: String?, userType: UserType, context: Context, position: Int): DishViewModel {
    val userPrice = when (userType) {
        UserType.STUDENT -> studentPrice
        UserType.WORKER -> workerPrice
        UserType.GUEST -> guestPrice
    }
    val priceText = "${NUMBER_FORMAT.format(userPrice)} € ${if (priceType == PriceType.WEIGHTED) context.getString(R.string.per_100_gramm) else ""}"
    val allergensText = "${context.getString(R.string.allergens)} ${allergens.replace("A1", context.getString(R.string.allergen_gluten_description)).joinToString()}"
    val badgesText = badges
            .filterNotNull()
            .joinTo(buffer = StringBuilder(), transform = { context.getString(it.descriptionId) })
            .toString()
            .capitalizeFirstChar()
    val description = Html.fromHtml(context.getString(R.string.row_dish_description, name, priceText, badgesText)).trim()
    return DishViewModel(this, headerText, userPrice, priceText, allergensText, badgesText, position, this.displayName(), description)
}


/**
 * A comparator that sorts by the dish category and the price
 * for this user type.
 */
@Suppress("Destructure")
val UserType.dishComparator: Comparator<DbDish>
    get() = compareByDescending<DbDish> { it.displayCategory() } // Sort by category
            .thenComparator { d1, d2 ->
                if (d1.priceType == d2.priceType) 0 else if (d1.priceType == PriceType.FIXED) -1 else 1
            } // Weighted is worse
            .thenBy { selectPrice(it) } // then by actual price

/**
 * Compute the DishViewModels for a list of Dishes.
 */
fun List<DbDish>.toDishViewModels(context: Context, userType: UserType): List<DishViewModel> {
    val sortedList = sortedWith(userType.dishComparator)
    return sortedList.mapIndexed { position, dish ->
        dish.toDishViewModel(headerTextForIndex(position, sortedList), userType, context, position)
    }
}

/**
 * Check if the dish is the first element of a category in the list.
 * Useful for determining whether a header should be displayed.
 */
private fun isFirstInCategory(index: Int, dishes: List<DbDish>): Boolean {
    val indexDish = dishes[index]
    val previousDish = if (index - 1 >= 0) dishes[index - 1] else null
    return indexDish.displayCategory() != previousDish?.displayCategory?.invoke()
}

private fun headerTextForIndex(index: Int, dishes: List<DbDish>): String?
        = if (isFirstInCategory(index, dishes)) dishes[index].displayCategory() else null