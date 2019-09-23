package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.request.FutureTarget
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.databinding.ActivityDishDetailsBinding
import com.cbruegg.mensaupb.util.LiveData
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.cbruegg.mensaupb.util.xmlDrawableToBitmap
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutionException
import kotlin.math.max

class DishDetailsActivity : AppCompatActivity() {

    private class ViewModel(private val context: Context, private val imageUrl: String?, text: String) : androidx.lifecycle.ViewModel() {
        private val _image: MutableLiveData<ImageSpec?> = MutableLiveData(null)
        val image: LiveData<ImageSpec?> = _image

        val text: LiveData<String> = LiveData(text)

        private var loadingJob: Job? = null

        @UiThread
        fun load() {
            if (imageUrl == null || loadingJob != null) return

            loadingJob = viewModelScope.launch {
                val file = try {
                    GlideApp.with(context)
                        .asFile()
                        .load(imageUrl)
                        .submit()
                        .await()
                } catch (e: ExecutionException) {
                    null
                }

                _image.data = file?.toImageSpec() ?: R.drawable.ic_error_outline_black_24dp.toImageSpec()

                loadingJob = null
            }
        }

        private suspend fun <T> FutureTarget<T>.await(): T = withContext(Dispatchers.IO) { get() }
    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"
        private const val ARG_TEXT = "text"

        fun createStartIntent(context: Context, imageUrl: String?, text: String) = Intent(context, DishDetailsActivity::class.java).apply {
            replaceExtras(bundleOf(ARG_IMAGE_URL to imageUrl, ARG_TEXT to text))
        }
    }

    private lateinit var viewModel: ViewModel
    private lateinit var binding: ActivityDishDetailsBinding

    private fun initialViewModel() = (intent?.extras ?: error("Use createStartIntent")).run {
        ViewModel(
            applicationContext,
            getString(ARG_IMAGE_URL),
            getString(ARG_TEXT) ?: error("Use createStartIntent")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDishDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= 29) {
            binding.activityPhotoRoot.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            binding.activityPhotoRoot.setOnApplyWindowInsetsListener { _, windowInsets ->
                binding.dishText.also {
                    it.setPadding(it.paddingLeft, it.paddingTop, it.paddingRight, max(windowInsets.systemWindowInsetBottom, it.paddingBottom))
                }
                // We deliberately ignore all but the bottom padding, as the photo may expand under the system windows
                windowInsets.consumeSystemWindowInsets()
            }
        }

        viewModel = viewModel(::initialViewModel).apply {
            image.observe(this@DishDetailsActivity) { imageSpec ->
                imageSpec ?: return@observe

                val imageSource = when (imageSpec) {
                    is ImageSpec.File -> ImageSource.uri(imageSpec.file.toUri())
                    is ImageSpec.Drawable -> ImageSource.bitmap(xmlDrawableToBitmap(imageSpec.res))
                }
                binding.photoView.setImage(imageSource)
                binding.photoViewLoading.isVisible = false
                fadeInPhotoView()
            }
            text.observe(this@DishDetailsActivity) { text ->
                binding.dishText.text = text
            }

            binding.photoViewLoading.isVisible = true
            load()
        }

        binding.activityPhotoRoot.setOnClickListener { finish() }
        binding.photoView.setOnClickListener { finish() }

        binding.dishText.visibility = View.INVISIBLE
        binding.dishText.doOnLayout {
            binding.dishText.visibility = View.VISIBLE
            binding.dishText.translationY = binding.dishText.height.toFloat()
            binding.dishText.animate().setDuration(150).translationY(0f)
        }
    }

    private fun fadeInPhotoView() {
        binding.photoView.alpha = 0f
        viewModel.viewModelScope.launch {
            while (!binding.photoView.isImageLoaded) {
                awaitFrame()
            }
            binding.photoView.animate().setDuration(300).alpha(1f)
        }
    }

}

private sealed class ImageSpec {
    data class File(val file: java.io.File) : ImageSpec()
    data class Drawable(@DrawableRes val res: Int) : ImageSpec()
}

private fun File.toImageSpec() = ImageSpec.File(this)
private fun @receiver:DrawableRes Int.toImageSpec() = ImageSpec.Drawable(this)