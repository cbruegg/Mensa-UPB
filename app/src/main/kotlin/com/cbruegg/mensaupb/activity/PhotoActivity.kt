package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.bumptech.glide.request.FutureTarget
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.util.LiveData
import com.cbruegg.mensaupb.util.MutableLiveData
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.cbruegg.mensaupb.viewmodel.BaseViewModel
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.android.synthetic.main.activity_photo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MIN_LOAD_TIME_MS = 500 // Prevents jank

class PhotoActivity : BaseActivity() {

    private class ViewModel(private val context: Context, private val imageUrl: String) : BaseViewModel() {
        private val _image: MutableLiveData<File?> = MutableLiveData(null)
        val image: LiveData<File?> = _image

        private var firstLoad = true

        fun load() {
            if (!firstLoad) return
            firstLoad = false

            launch {
                val startTime = System.currentTimeMillis()
                val file = GlideApp.with(context)
                    .asFile()
                    .load(imageUrl)
                    .submit()
                    .await()
                delay(MIN_LOAD_TIME_MS - (System.currentTimeMillis() - startTime))
                _image.data = file
            }
        }

        private suspend fun <T> FutureTarget<T>.await(): T = withContext(Dispatchers.IO) { get() }
    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun createStartIntent(context: Context, imageUrl: String) = Intent(context, PhotoActivity::class.java).apply {
            replaceExtras(bundleOf(ARG_IMAGE_URL to imageUrl))
        }
    }

    private val imageUrl by lazy { intent!!.extras!!.getString(ARG_IMAGE_URL) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

        viewModel { ViewModel(applicationContext, imageUrl) }.apply {
            image.observe(this@PhotoActivity) { imageFile ->
                imageFile ?: return@observe

                photoViewLoading.isVisible = false
                photoView.setImage(ImageSource.uri(imageFile.toUri()))
                fadeInPhotoView()
            }

            photoViewLoading.isVisible = true
            load()
        }

        activityPhotoRoot.setOnClickListener { finish() }
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