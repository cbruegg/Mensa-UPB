package com.cbruegg.mensaupb.dishes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import com.cbruegg.mensaupb.activity.DishDetailsActivity
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.viewmodel.DishViewModel

/**
 * Show a dialog that displays the full size image of the dish.
 * @param dishViewModel DishViewModel with an imageUrl
 */
@SuppressLint("InflateParams")
fun Context.showDishDetailsDialog(dishViewModel: DishViewModel) {
    val fullText = buildDishDetailsText(dishViewModel)

    val imageUrl = dishViewModel.dish.imageUrl
    startActivity(DishDetailsActivity.createStartIntent(this, imageUrl, fullText))
}

private fun Context.buildDishDetailsText(dishViewModel: DishViewModel): CharSequence {
    val sectionLabelColor = TypedValue().also {
        theme.resolveAttribute(R.attr.colorPrimary, it, true)
    }.data
    val secondaryTextColor = TypedValue().also {
        theme.resolveAttribute(R.attr.colorOnBackground, it, true)
    }.data

    return SpannableStringBuilder().apply {
        appendSection(getString(R.string.price_section_title), dishViewModel.priceText, sectionLabelColor, secondaryTextColor)

        if (dishViewModel.hasBadges) {
            appendSection(getString(R.string.badges_section_title), dishViewModel.badgesText.orEmpty(), sectionLabelColor, secondaryTextColor)
        }
        if (dishViewModel.containsAllergens) {
            appendSection(getString(R.string.allergens_section_title), dishViewModel.allergensText.substringAfter(": ").trim(), sectionLabelColor, secondaryTextColor)
        }
        if (dishViewModel.hasNutritionalValues) {
            appendSection(
                getString(R.string.nutritional_values),
                dishViewModel.nutritionalValuesText
                    .orEmpty()
                    .substringAfter('\n', dishViewModel.nutritionalValuesText.orEmpty())
                    .replace(", ", "\n"),
                sectionLabelColor,
                secondaryTextColor
            )
        }
    }.trimEnd()
}

private fun SpannableStringBuilder.appendSection(
    label: String,
    value: String,
    labelColor: Int,
    valueColor: Int
): SpannableStringBuilder {
    if (value.isBlank()) return this
    if (isNotEmpty()) append("\n\n")

    appendStyled(label, StyleSpan(Typeface.BOLD), RelativeSizeSpan(1.02f), ForegroundColorSpan(labelColor))
    append('\n')
    appendStyled(value.trim(), RelativeSizeSpan(0.94f), ForegroundColorSpan(valueColor))
    return this
}

private fun SpannableStringBuilder.appendStyled(text: String, vararg spans: Any): SpannableStringBuilder {
    val start = length
    append(text)
    spans.forEach { setSpan(it, start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }
    return this
}
