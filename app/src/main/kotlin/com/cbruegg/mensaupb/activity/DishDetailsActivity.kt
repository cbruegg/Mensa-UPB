package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
        private const val ARG_TEXT = "text"

        fun createStartIntent(context: Context, imageUrl: String?, text: String) =
            Intent(context, DishDetailsActivity::class.java).apply {
                replaceExtras(bundleOf(ARG_IMAGE_URL to imageUrl, ARG_TEXT to text))
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
        val text = extras.getString(ARG_TEXT) ?: error("Use createStartIntent")

        binding.dishText.text = text
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

    private suspend fun <T> FutureTarget<T>.await(): T = withContext(Dispatchers.IO) { get() }

}

private sealed class ImageSpec {
    data class File(val file: java.io.File) : ImageSpec()
    data class Drawable(@DrawableRes val res: Int) : ImageSpec()
}

private fun File.toImageSpec() = ImageSpec.File(this)
private fun @receiver:DrawableRes Int.toImageSpec() = ImageSpec.Drawable(this)
