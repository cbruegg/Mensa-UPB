package com.cbruegg.mensaupb.appwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.BaseActivity
import com.cbruegg.mensaupb.adapter.RestaurantSpinnerAdapter
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import java.io.IOException

/**
 * Activity used for configuring an app widget. It must
 * be supplied an an AppWidgetId using [AppWidgetManager.EXTRA_APPWIDGET_ID].
 */
class DishesAppWidgetConfigActivity : BaseActivity<DishesAppWidgetConfigView, DishesAppWidgetConfigPresenter>(), DishesAppWidgetConfigView {

    private val spinner by bindView<Spinner>(R.id.widget_config_spinner)
    private val cancelButton by bindView<Button>(R.id.widget_config_cancel)
    private val confirmButton by bindView<Button>(R.id.widget_config_confirm)
    private val progressBar by bindView<ProgressBar>(R.id.widget_config_progressbar)

    private val appWidgetId by lazy {
        intent.extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    override val mvpViewType: Class<DishesAppWidgetConfigView>
        get() = DishesAppWidgetConfigView::class.java

    // TODO Inject
    override fun createPresenter() = DishesAppWidgetConfigPresenter(Downloader(this), DishesWidgetConfigurationManager(this), appWidgetId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_widget_config)
        setResult(RESULT_CANCELED)

        confirmButton.setOnClickListener {
            presenter.onConfirmClicked(spinner.selectedItemPosition)
        }
        cancelButton.setOnClickListener { presenter.onCancel() }
    }

    override fun setRestaurantSpinnerList(list: List<DbRestaurant>) {
        spinner.adapter = RestaurantSpinnerAdapter(this, list)
    }

    override fun setConfirmButtonStatus(enabled: Boolean) {
        confirmButton.isEnabled = enabled
    }

    override fun setProgressBarVisible(visible: Boolean) {
        progressBar.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    /**
     * Notify the user about a network error.
     */
    override fun showNetworkError(e: IOException) {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }

    /**
     * Start the widget update service.
     */
    override fun updateWidget() {
        val serviceIntent = DishesWidgetUpdateService.createStartIntent(this, appWidgetId)
        startService(serviceIntent)
    }

    override fun close(success: Boolean) {
        if (success) {
            setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
        }
        finish()
    }
}

