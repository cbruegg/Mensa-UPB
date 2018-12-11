package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import com.bumptech.glide.request.FutureTarget
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.util.LiveData
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.cbruegg.mensaupb.util.xmlDrawableToBitmap
import com.cbruegg.mensaupb.viewmodel.BaseViewModel
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_dish_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ExecutionException

class DishDetailsActivity : BaseActivity() {

    private class ViewModel(private val context: Context, private val imageUrl: String?, text: String) : BaseViewModel() {
        private val _image: MutableLiveData<ImageSpec?> = MutableLiveData(null)
        val image: LiveData<ImageSpec?> = _image

        val text: LiveData<String> = LiveData(text)

        private var loadingJob: Job? = null

        @UiThread
        fun load() {
            if (imageUrl == null || loadingJob != null) return

            loadingJob = launch {
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

    private fun initialViewModel() = (intent?.extras ?: error("Use createStartIntent")).run {
        ViewModel(
            applicationContext,
            getString(ARG_IMAGE_URL),
            getString(ARG_TEXT) ?: error("Use createStartIntent")
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dish_details)

        viewModel(::initialViewModel).apply {
            image.observe(this@DishDetailsActivity) { imageSpec ->
                imageSpec ?: return@observe

                val imageSource = when (imageSpec) {
                    is ImageSpec.File -> ImageSource.uri(imageSpec.file.toUri())
                    is ImageSpec.Drawable -> ImageSource.bitmap(xmlDrawableToBitmap(imageSpec.res))
                }
                photoView.setImage(imageSource)
                photoViewLoading.isVisible = false
                fadeInPhotoView()
            }
            text.observe(this@DishDetailsActivity) { text ->
                dishText.text = text
            }

            photoViewLoading.isVisible = true
            load()
        }

        activityPhotoRoot.setOnClickListener { finish() }
        photoView.setOnClickListener { finish() }

        dishText.visibility = View.INVISIBLE
        dishText.doOnLayout {
            dishText.visibility = View.VISIBLE
            dishText.translationY = dishText.height.toFloat()
            dishText.animate().setDuration(150).translationY(0f)
        }
    }

    private fun fadeInPhotoView() {
        photoView.alpha = 0f
        launch {
            while (!photoView.isImageLoaded) {
                awaitFrame()
            }
            photoView.animate().setDuration(300).alpha(1f)
        }
    }

}

private sealed class ImageSpec {
    data class File(val file: java.io.File) : ImageSpec()
    data class Drawable(@DrawableRes val res: Int) : ImageSpec()
}

private fun File.toImageSpec() = ImageSpec.File(this)
private fun @receiver:DrawableRes Int.toImageSpec() = ImageSpec.Drawable(this)