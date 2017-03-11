package com.cbruegg.mensaupb.fragment

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.adapter.DishViewModelAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Fragment responsible for displaying the dishes of a restaurant at a specified date.
 * The factory method newInstance needs to be used.
 */
class DishesFragment : BaseFragment() {

    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_DATE = "date"
        private val ARG_GERMAN_DISH_NAME = "german_dish_name"

        /**
         * Construct a new instance of this fragment.
         * @param germanDishName If non-null, look for a matching dish and
         * shows its image.
         * @see DishesFragment
         */
        fun newInstance(restaurant: Restaurant, date: Date, germanDishName: String? = null): DishesFragment {
            val fragment = DishesFragment()
            fragment.arguments = Bundle().apply {
                putString(ARG_RESTAURANT, restaurant.serialize())
                putLong(ARG_DATE, date.time)
                putString(ARG_GERMAN_DISH_NAME, germanDishName)
            }
            return fragment
        }
    }

    private val dishList: RecyclerView by bindView(R.id.dish_list)
    private val noDishesMessage: TextView by bindView(R.id.no_dishes_message)
    @Inject lateinit var downloader: Downloader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_dishes, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Set up the list of dishes
         */
        val adapter = DishViewModelAdapter()
        adapter.onClickListener = { dishViewModel, _ -> if (dishViewModel.hasBigImage) showDishDetailsDialog(dishViewModel) }
        dishList.adapter = adapter
        dishList.layoutManager = LinearLayoutManager(activity)

        val restaurant = Restaurant.deserialize(arguments.getString(ARG_RESTAURANT))
        val date = Date(arguments.getLong(ARG_DATE))
        val userType = context.userType
        val germanDishName: String? = arguments.getString(ARG_GERMAN_DISH_NAME, null)

        launch(MainThread) {
            downloader.downloadOrRetrieveDishesAsync(restaurant, date)
                    .await()
                    .fold({ showNetworkError(adapter, it) }) {
                        noDishesMessage.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                        val dishViewModels = it.toDishViewModels(activity, userType)
                        tryShowArgDish(dishViewModels, germanDishName)
                        adapter.list.setAll(dishViewModels)
                    }
        }.register()
    }

    /**
     * If the germanDishName parameter is non-null,
     * try to find a matching dish and display its image.
     */
    private fun tryShowArgDish(dishViewModels: List<DishViewModel>, germanDishName: String?) {
        if (germanDishName == null) {
            return
        }

        dishViewModels.firstOrNull { it.dish.germanName == germanDishName }?.let {
            if (it.hasThumbnail) {
                showDishDetailsDialog(it)
            }
        }
    }

    private fun showNetworkError(adapter: DishViewModelAdapter, e: IOException) {
        noDishesMessage.visibility = View.GONE
        adapter.list.setAll(emptyList())
        e.printStackTrace()
    }

    /**
     * @return the size of the display in pixels. The first element of the pair is the width,
     * the second part is the height.
     */
    private fun getDisplaySize(): Pair<Int, Int> {
        val display = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        display.defaultDisplay.getSize(size)
        return Pair(size.x, size.y)
    }

    /**
     * Show a dialog that displays the full size image of the dish.
     * @param dishViewModel DishViewModel with an imageUrl
     */
    private fun showDishDetailsDialog(dishViewModel: DishViewModel) {
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_dish_details, null, false)
        val imageView = dialogView.findViewById(R.id.dish_image) as ImageView
        val descriptionView = dialogView.findViewById(R.id.dish_description) as TextView
        val dishInfoContainer = dialogView.findViewById(R.id.dish_info_container)
        val progressBar = dialogView.findViewById(R.id.dish_image_progressbar) as ProgressBar

        val alertDialog = AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(true)
                .create()
        alertDialog.show()

        val displaySize = getDisplaySize()

        // fit() doesn't work here, as AlertDialogs don't provide
        // a container for inflation, so some LayoutParams don't work
        Picasso.with(activity)
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
                        Toast.makeText(activity, R.string.could_not_load_image, Toast.LENGTH_SHORT).show()
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

}