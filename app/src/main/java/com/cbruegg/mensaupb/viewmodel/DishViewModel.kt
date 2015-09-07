package com.cbruegg.mensaupb.viewmodel

import android.content.Context
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.extensions.capitalizeFirstChar
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.model.Badge
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.model.UserType
import java.text.DecimalFormat
import java.util.Locale

data class DishViewModel(val dish: Dish, val headerText: String?, userType: UserType, context: Context) {
    private val NUMBER_FORMAT = DecimalFormat("0.00")

    val localizedCategory: String = if (Locale.getDefault().getLanguage() == Locale.GERMAN.getLanguage()) dish.germanCategory else dish.category

    val userPrice = when (userType) {
        UserType.STUDENT -> dish.studentPrice
        UserType.WORKER -> dish.workerPrice
        UserType.GUEST -> dish.guestPrice
    }

    val priceText = context.getString(R.string.price) + " " +
            NUMBER_FORMAT.format(userPrice) + " € " + (if (dish.priceType == PriceType.WEIGHTED) context.getString(R.string.per_100_gramm) else "")

    val allergensText = context.getString(R.string.allergens) + " " + dish.allergens.join()
    val containsAllergens = dish.allergens.isNotEmpty()

    val badges = dish.badges.map { Badge.findById(it) }.filterNotNull()
    val badgesText = badges.joinTo(buffer = StringBuilder(), transform = { context.getString(it.descriptionId) }).toString().capitalizeFirstChar()
    val hasBadges = badges.isNotEmpty()

    val hasThumbnail = !dish.thumbnailImageUrl.isNullOrEmpty()
    val hasBigImage = !dish.imageUrl.isNullOrEmpty()

    val hasHeader = headerText != null

}

fun List<Dish>.toDishViewModels(context: Context, userType: UserType): List<DishViewModel> {
    val sortedList = sortBy { first, second -> first.germanCategory.compareTo(second.germanCategory) }.reverse()
    return sortedList.mapIndexed { position, dish -> DishViewModel(dish, headerTextForIndex(position, sortedList), userType, context) }
}

private fun isFirstInCategory(index: Int, dishes: List<Dish>): Boolean {
    val indexDish = dishes[index]
    val previousDish = if (index - 1 >= 0) dishes[index - 1] else null
    return indexDish.germanCategory != previousDish?.germanCategory
}

private fun headerTextForIndex(index: Int, dishes: List<Dish>): String?
        = if (isFirstInCategory(index, dishes)) dishes[index].germanCategory else null