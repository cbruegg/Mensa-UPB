package com.cbruegg.mensaupb.dishes

import android.content.Context
import android.graphics.Point
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

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
fun showDishDetailsDialog(context: Context, dishViewModel: DishViewModel) {
    val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_dish_details, null, false)
    val imageView = dialogView.findViewById(R.id.dish_image) as ImageView
    val descriptionView = dialogView.findViewById(R.id.dish_description) as TextView
    val dishInfoContainer = dialogView.findViewById(R.id.dish_info_container)
    val progressBar = dialogView.findViewById(R.id.dish_image_progressbar) as ProgressBar

    val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()
    alertDialog.show()

    val displaySize = getDisplaySize(context)

    // fit() doesn't work here, as AlertDialogs don't provide
    // a container for inflation, so some LayoutParams don't work
    Picasso.with(context)
            .load(dishViewModel.dish.imageUrl)
            .resize(displaySize.first, displaySize.second)
            .onlyScaleDown()
            .centerInside()
            .into(imageView, object : Callback {
                override fun onSuccess() {
                    progressBar.visibility = View.GONE
                    dishInfoContainer.visibility = View.VISIBLE
                }

                override fun onError() {
                    Toast.makeText(context, R.string.could_not_load_image, Toast.LENGTH_SHORT).show()
                    alertDialog.dismiss()
                }

            })
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