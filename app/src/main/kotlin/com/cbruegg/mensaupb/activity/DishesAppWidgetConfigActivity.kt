package com.cbruegg.mensaupb.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.MainThread
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantSpinnerAdapter
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfiguration
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import com.cbruegg.mensaupb.viewmodel.uiSorted
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.launch
import java.io.IOException

/**
 * Activity used for configuring an app widget. It must
 * be supplied an an AppWidgetId using [AppWidgetManager.EXTRA_APPWIDGET_ID].
 */
class DishesAppWidgetConfigActivity : BaseActivity() {

    private val spinner by bindView<Spinner>(R.id.widget_config_spinner)

    private val cancelButton by bindView<Button>(R.id.widget_config_cancel)
    private val confirmButton by bindView<Button>(R.id.widget_config_confirm)
    private val progressBar by bindView<ProgressBar>(R.id.widget_config_progressbar)

    private var restaurantList: List<Restaurant>? = null
    private val appWidgetId by lazy {
        intent.extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_widget_config)
        setResult(RESULT_CANCELED)

        progressBar.visibility = View.VISIBLE
        confirmButton.isEnabled = false
        confirmButton.setOnClickListener {
            // Non-Null assertion is safe, the button is only enabled after receiving
            // the list
            val selectedRestaurant = restaurantList!![spinner.selectedItemPosition]
            DishesWidgetConfigurationManager(this@DishesAppWidgetConfigActivity)
                    .putConfiguration(appWidgetId, DishesWidgetConfiguration(selectedRestaurant.id))
            updateWidget()
            setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
            finish()
        }
        cancelButton.setOnClickListener { finish() }

        job = launch(MainThread) {
            Downloader(this@DishesAppWidgetConfigActivity)
                    .downloadOrRetrieveRestaurantsAsync()
                    .await()
                    .fold({ showNetworkError(it) }) {
                        val preparedList = it.uiSorted()
                        restaurantList = preparedList
                        spinner.adapter = RestaurantSpinnerAdapter(this@DishesAppWidgetConfigActivity, preparedList)
                        confirmButton.isEnabled = true
                    }
            progressBar.visibility = View.INVISIBLE
        }
    }

    /**
     * Notify the user about a network error.
     */
    private fun showNetworkError(e: IOException) {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    /**
     * Start the widget update service.
     */
    fun updateWidget() {
        val serviceIntent = DishesWidgetUpdateService.createStartIntent(this, appWidgetId)
        startService(serviceIntent)
    }
}

