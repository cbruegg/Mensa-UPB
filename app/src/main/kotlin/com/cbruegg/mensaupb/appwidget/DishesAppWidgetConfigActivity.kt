package com.cbruegg.mensaupb.appwidget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantSpinnerAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import kotlinx.android.synthetic.main.activity_app_widget_config.*
import javax.inject.Inject

/**
 * Activity used for configuring an app widget. It must
 * be supplied an an AppWidgetId using [AppWidgetManager.EXTRA_APPWIDGET_ID].
 */
class DishesAppWidgetConfigActivity : AppCompatActivity() {

    private val appWidgetId by lazy {
        intent.extras.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    @Inject
    lateinit var repository: Repository
    private lateinit var viewModel: DishesAppWidgetViewModel
    private lateinit var viewModelController: DishesAppWidgetViewModelController

    private fun createController(viewModel: DishesAppWidgetViewModel) = DishesAppWidgetViewModelController(
        repository,
        DishesWidgetConfigurationManager(this),
        appWidgetId,
        viewModel
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
        setContentView(R.layout.activity_app_widget_config)
        setResult(RESULT_CANCELED)

        viewModel = viewModel(::initialDishesAppWidgetViewModel)
        viewModelController = createController(viewModel)

        viewModel.networkError.observe(this) {
            if (it) {
                showNetworkError()
            }
        }
        viewModel.restaurants.observe(this) {
            widgetConfigSpinner.adapter = RestaurantSpinnerAdapter(this, it)
        }
        viewModel.confirmButtonStatus.observe(this) {
            widgetConfigConfirm.isEnabled = it
        }
        viewModel.showProgress.observe(this) {
            widgetConfigProgressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
        viewModel.closed.observe(this) {
            if (it) {
                if (!viewModel.networkError.data && viewModel.restaurants.data.isNotEmpty()) {
                    setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
                }
                finish()
            }
        }

        widgetConfigConfirm.setOnClickListener {
            viewModelController.onConfirmClicked(widgetConfigSpinner.selectedItemPosition)
            DishesWidgetUpdateService.scheduleUpdate(this, 15, appWidgetId)
        }
        widgetConfigCancel.setOnClickListener { viewModelController.onCancel() }

        viewModelController.load()
    }

    /**
     * Notify the user about a network error.
     */
    private fun showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
    }

}

