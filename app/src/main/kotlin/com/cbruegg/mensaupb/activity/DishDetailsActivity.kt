package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.request.FutureTarget
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.ActivityDishDetailsBinding
import com.cbruegg.mensaupb.util.*
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutionException

class DishDetailsActivity : AppCompatActivity() {

    companion object {
        private const val ARG_IMAGE_URL = "image_url"
        private const val ARG_PRICE_TEXT = "price_text"
        private const val ARG_BADGES_TEXT = "badges_text"
        private const val ARG_ALLERGENS_TEXT = "allergens_text"
        private const val ARG_NUTRITIONAL_VALUES_TEXT = "nutritional_values_text"

        fun createStartIntent(
            context: Context,
            imageUrl: String?,
            priceText: String,
            badgesText: String?,
            allergensText: String?,
            nutritionalValuesText: String?
        ) =
            Intent(context, DishDetailsActivity::class.java).apply {
                replaceExtras(
                    bundleOf(
                        ARG_IMAGE_URL to imageUrl,
                        ARG_PRICE_TEXT to priceText,
                        ARG_BADGES_TEXT to badgesText,
                        ARG_ALLERGENS_TEXT to allergensText,
                        ARG_NUTRITIONAL_VALUES_TEXT to nutritionalValuesText
                    )
                )
            }
    }

    private lateinit var binding: ActivityDishDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val baseDishTextPaddingBottom = binding.dishText.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.activityPhotoRoot) { _, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            binding.dishText.updatePadding(
                bottom = baseDishTextPaddingBottom + bars.bottom
            )
            // We deliberately ignore all but the bottom padding, as the photo may expand under the system windows.
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.activityPhotoRoot)

        val extras = intent?.extras ?: error("Use createStartIntent")
        val imageUrl = extras.getString(ARG_IMAGE_URL)
        val priceText = extras.getString(ARG_PRICE_TEXT) ?: error("Use createStartIntent")
        val badgesText = extras.getString(ARG_BADGES_TEXT)
        val allergensText = extras.getString(ARG_ALLERGENS_TEXT)
        val nutritionalValuesText = extras.getString(ARG_NUTRITIONAL_VALUES_TEXT)

        binding.dishText.text = buildDishDetailsText(priceText, badgesText, allergensText, nutritionalValuesText)
        binding.photoViewLoading.isVisible = true
        loadImage(imageUrl)

        binding.activityPhotoRoot.setOnClickListener { finish() }
        binding.photoView.setOnClickListener { finish() }

        binding.dishText.visibility = View.INVISIBLE
        binding.dishText.doOnLayout {
            binding.dishText.visibility = View.VISIBLE
            binding.dishText.translationY = binding.dishText.height.toFloat()
            binding.dishText.animate().setDuration(150).translationY(0f)
        }
    }

    @UiThread
    private fun loadImage(imageUrl: String?) {
        if (imageUrl == null) {
            showImageSpec(R.drawable.ic_error_outline_black_24dp.toImageSpec())
            return
        }

        binding.photoViewLoading.isVisible = true
        lifecycleScope.launch {
            val file = try {
                GlideApp.with(this@DishDetailsActivity)
                    .asFile()
                    .load(imageUrl)
                    .submit()
                    .await()
            } catch (_: ExecutionException) {
                null
            }

            showImageSpec(
                file?.toImageSpec() ?: R.drawable.ic_error_outline_black_24dp.toImageSpec()
            )
        }
    }

    private fun showImageSpec(imageSpec: ImageSpec) {
        val imageSource = when (imageSpec) {
            is ImageSpec.File -> ImageSource.uri(imageSpec.file.toUri())
            is ImageSpec.Drawable -> ImageSource.bitmap(xmlDrawableToBitmap(imageSpec.res))
        }
        binding.photoView.setImage(imageSource)
        binding.photoViewLoading.isVisible = false
        fadeInPhotoView()
    }

    private fun fadeInPhotoView() {
        binding.photoView.alpha = 0f
        lifecycleScope.launch {
            while (!binding.photoView.isImageLoaded) {
                awaitFrame()
            }
            binding.photoView.animate().setDuration(300).alpha(1f)
        }
    }

    private fun buildDishDetailsText(
        priceText: String,
        badgesText: String?,
        allergensText: String?,
        nutritionalValuesText: String?
    ): CharSequence {
        val sectionLabelColor = TypedValue().also {
            theme.resolveAttribute(R.attr.colorPrimary, it, true)
        }.data
        val secondaryTextColor = TypedValue().also {
            theme.resolveAttribute(R.attr.colorOnBackground, it, true)
        }.data

        return SpannableStringBuilder().apply {
            appendSection(getString(R.string.price_section_title), priceText, sectionLabelColor, secondaryTextColor)
            appendSection(getString(R.string.badges_section_title), badgesText.orEmpty(), sectionLabelColor, secondaryTextColor)
            appendSection(getString(R.string.allergens_section_title), allergensText.orEmpty(), sectionLabelColor, secondaryTextColor)
            appendSection(getString(R.string.nutritional_values), nutritionalValuesText.orEmpty(), sectionLabelColor, secondaryTextColor)
        }.trimEnd()
    }

    private suspend fun <T> FutureTarget<T>.await(): T = withContext(Dispatchers.IO) { get() }

}

private sealed class ImageSpec {
    data class File(val file: java.io.File) : ImageSpec()
    data class Drawable(@DrawableRes val res: Int) : ImageSpec()
}

private fun File.toImageSpec() = ImageSpec.File(this)
private fun @receiver:DrawableRes Int.toImageSpec() = ImageSpec.Drawable(this)

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
