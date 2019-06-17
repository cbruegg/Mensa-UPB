package com.cbruegg.mensaupb.viewmodel

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.extensions.capitalizeFirstChar
import com.cbruegg.mensaupb.extensions.replace
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupbservice.api.Badge
import com.cbruegg.mensaupbservice.api.PriceType
import java.text.DecimalFormat

sealed class DishListViewModel

/**
 * Wrapper for [DbDish] objects providing easy access to various attributes for data binding.
 */
data class DishViewModel(
    val dish: DbDish,
    val priceText: String,
    val allergensText: String,
    val badgesText: String?,
    val name: String,
    val description: CharSequence
) : DishListViewModel() {
    val hasBadges = dish.badges.isNotEmpty()
    val containsAllergens = dish.allergens.isNotEmpty()
    val hasThumbnail = !dish.thumbnailImageUrl.isNullOrEmpty()
    val hasBigImage = !dish.imageUrl.isNullOrEmpty()

    // Overridden equals and hashCode as description may be a
    // SpannableStringBuilder that doesn't implement equals correctly
    // (as it checks referential equality of spans)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as DishViewModel

        if (dish != other.dish) return false
        if (priceText != other.priceText) return false
        if (allergensText != other.allergensText) return false
        if (badgesText != other.badgesText) return false
        if (name != other.name) return false
        if (description.toString() != other.description.toString()) return false
        if (hasBadges != other.hasBadges) return false
        if (containsAllergens != other.containsAllergens) return false
        if (hasThumbnail != other.hasThumbnail) return false
        if (hasBigImage != other.hasBigImage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dish.hashCode()
        result = 31 * result + priceText.hashCode()
        result = 31 * result + allergensText.hashCode()
        result = 31 * result + (badgesText?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + description.toString().hashCode()
        result = 31 * result + hasBadges.hashCode()
        result = 31 * result + containsAllergens.hashCode()
        result = 31 * result + hasThumbnail.hashCode()
        result = 31 * result + hasBigImage.hashCode()
        return result
    }

}

data class HeaderViewModel(val text: CharSequence, val showDivider: Boolean) : DishListViewModel()

private val NUMBER_FORMAT = DecimalFormat("0.00")

private fun DbDish.toDishViewModel(userType: UserType, context: Context): DishViewModel {
    val userPrice = when (userType) {
        UserType.STUDENT -> studentPrice
        UserType.WORKER -> workerPrice
        UserType.GUEST -> guestPrice
    }
    val priceText = "${NUMBER_FORMAT.format(userPrice)} € ${if (priceType == PriceType.WEIGHTED) context.getString(R.string.per_100_gramm) else ""}"
    val badgesText = badges
        .joinTo(buffer = StringBuilder(), transform = { context.getString(it.descriptionStringId) })
        .toString()
        .capitalizeFirstChar()

    val allergensText = "${context.getString(R.string.allergens)} ${allergens.replace("A1", context.getString(R.string.allergen_gluten_description)).joinToString()}"

    val description = buildRowDishDescription(context, priceText, badgesText)
    return DishViewModel(this, priceText, allergensText, badgesText, displayName(), description.trim())
}

private fun DbDish.buildRowDishDescription(context: Context, priceText: String, badgesText: String): CharSequence {
    val sp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16f, context.resources.displayMetrics).toInt()
    val sp15 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15f, context.resources.displayMetrics).toInt()
    val textColor = TypedValue().also { context.theme.resolveAttribute(R.attr.colorOnBackground, it, true) }.data
    return SpannableStringBuilder()
        .appendln(displayName(), ForegroundColorSpan(textColor), AbsoluteSizeSpan(sp16))
        .appendln(priceText, AbsoluteSizeSpan(sp15))
        .append(badgesText, AbsoluteSizeSpan(sp15))
}

private fun SpannableStringBuilder.append(line: String, vararg spans: Any): SpannableStringBuilder {
    val oldLen = length
    val newLen = oldLen + line.length
    append(line)
    for (span in spans) {
        setSpan(span, oldLen, newLen, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
    }
    return this
}

private fun SpannableStringBuilder.appendln(line: String, vararg spans: Any): SpannableStringBuilder {
    append(line, *spans)
    append('\n')
    return this
}

private val Badge.descriptionStringId
    get() = when (this) {
        Badge.VEGAN -> R.string.vegan
        Badge.VEGETARIAN -> R.string.vegetarian
        Badge.NONFAT -> R.string.nonfat
        Badge.LACTOSE_FREE -> R.string.lactose_free
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
fun List<DbDish>.toDishViewModels(context: Context, userType: UserType): List<DishListViewModel> {
    val result = mutableListOf<DishListViewModel>()
    val sortedList = sortedWith(userType.dishComparator)
    for ((position, dish) in sortedList.withIndex()) {
        val headerText = headerTextForIndex(position, sortedList)
        if (headerText != null) {
            result += HeaderViewModel(headerText, showDivider = position != 0)
        }
        result += dish.toDishViewModel(userType, context)
    }

    return result
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

private fun headerTextForIndex(index: Int, dishes: List<DbDish>): String? = if (isFirstInCategory(index, dishes)) dishes[index].displayCategory() else null