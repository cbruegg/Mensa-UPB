package com.cbruegg.mensaupb.dishes

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.util.OnlyScaleDownCenterInside
import com.cbruegg.mensaupb.viewmodel.DishViewModel

/**
 * @return the size of the display in pixels. The first element of the pair is the width,
 * the second part is the height.
 */
private fun getDisplaySize(context: Context): Pair<Int, Int> {
    val display = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val size = Point()
    display.defaultDisplay.getSize(size)
    return Pair(size.x, size.y)
}

/**
 * Show a dialog that displays the full size image of the dish.
 * @param dishViewModel DishViewModel with an imageUrl
 */
@SuppressLint("InflateParams")
fun showDishDetailsDialog(context: Context, dishViewModel: DishViewModel, onDismiss: () -> Unit = {}) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_dish_details, null, false)
    val imageView = dialogView.findViewById<ImageView>(R.id.dish_image)
    val descriptionView = dialogView.findViewById<TextView>(R.id.dish_description)
    val progressBar = dialogView.findViewById<ProgressBar>(R.id.dish_image_progressbar)

    val alertDialog = AlertDialog.Builder(context)
        .setView(dialogView)
        .setCancelable(true)
        .setOnDismissListener { onDismiss() }
        .create()
    alertDialog.show()

    val displaySize = getDisplaySize(context)

    // fit() doesn't work here, as AlertDialogs don't provide
    // a container for inflation, so some LayoutParams don't work
    GlideApp.with(context)
        .load(dishViewModel.dish.imageUrl)
        .override(displaySize.first, displaySize.second)
        .transform(OnlyScaleDownCenterInside)
        .listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                Toast.makeText(context, R.string.could_not_load_image, Toast.LENGTH_SHORT).show()
                alertDialog.dismiss()
                return true
            }

            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                progressBar.visibility = View.GONE
                imageView.visibility = View.VISIBLE
                descriptionView.visibility = View.VISIBLE
                return false
            }
        })
        .into(imageView)
    val fullTextBuilder = StringBuilder()
    if (dishViewModel.containsAllergens) {
        fullTextBuilder.append(dishViewModel.allergensText).append("\n")
    }
    if (dishViewModel.hasBadges) {
        fullTextBuilder.append(dishViewModel.badgesText).append("\n")
    }
    fullTextBuilder.append(dishViewModel.priceText)
    descriptionView.text = fullTextBuilder
}