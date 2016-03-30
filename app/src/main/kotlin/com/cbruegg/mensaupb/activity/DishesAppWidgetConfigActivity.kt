package com.cbruegg.mensaupb.activity

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantSpinnerAdapter
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfiguration
import com.cbruegg.mensaupb.appwidget.DishesWidgetConfigurationManager
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class DishesAppWidgetConfigActivity : AppCompatActivity() {

    private val appWidgetId by lazy { intent.extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID) }
    private val spinner by bindView<Spinner>(R.id.widget_config_spinner)
    private val cancelButton by bindView<Button>(R.id.widget_config_cancel)
    private val confirmButton by bindView<Button>(R.id.widget_config_confirm)
    private val progressBar by bindView<ProgressBar>(R.id.widget_config_progressbar)

    private var subscription: Subscription? = null
    private var restaurantList: List<Restaurant>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_widget_config)
        setResult(RESULT_CANCELED)

        subscription = Downloader(this).downloadOrRetrieveRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it.fold({ showNetworkError() }) {
                        // TODO Remove duplicated code
                        val preparedList = it
                                .sortBy { first, second -> first.location.compareTo(second.location) }
                                .reversed() // Paderborn should be at the top of the list
                        restaurantList = preparedList
                        spinner.adapter = RestaurantSpinnerAdapter(this, preparedList)
                        confirmButton.isEnabled = true
                        progressBar.visibility = View.INVISIBLE
                    }
                    subscription?.unsubscribe()
                }

        progressBar.visibility = View.VISIBLE
        confirmButton.isEnabled = false
        confirmButton.setOnClickListener {
            // Non-Null assertion is safe, the button is only enabled after receiving
            // the list
            val selectedRestaurant = restaurantList!![spinner.selectedItemPosition]
            DishesWidgetConfigurationManager(this).putConfiguration(appWidgetId, DishesWidgetConfiguration(selectedRestaurant.id))
            updateWidget()
            setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
            finish()
        }
        cancelButton.setOnClickListener { finish() }
    }

    private fun showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

    fun updateWidget() {
        val serviceIntent = DishesWidgetUpdateService.createStartIntent(this, appWidgetId)
        startService(serviceIntent)
    }
}

