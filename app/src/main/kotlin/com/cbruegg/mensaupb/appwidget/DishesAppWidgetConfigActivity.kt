package com.cbruegg.mensaupb.appwidget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantSpinnerAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.databinding.ActivityAppWidgetConfigBinding
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.service.DishesWidgetUpdateService
import com.cbruegg.mensaupb.util.exhaustive
import com.cbruegg.mensaupb.util.observeNullSafe
import com.cbruegg.mensaupb.util.viewModel
import javax.inject.Inject

/**
 * Activity used for configuring an app widget. It must
 * be supplied an an AppWidgetId using [AppWidgetManager.EXTRA_APPWIDGET_ID].
 */
class DishesAppWidgetConfigActivity : AppCompatActivity() {

    private val appWidgetId by lazy {
        intent!!.extras!!.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    @Inject
    lateinit var repository: Repository
    private lateinit var viewModel: DishesAppWidgetViewModel
    private lateinit var viewModelController: DishesAppWidgetViewModelController
    private lateinit var binding: ActivityAppWidgetConfigBinding

    private fun createController(viewModel: DishesAppWidgetViewModel) = DishesAppWidgetViewModelController(
        repository,
        DishesWidgetConfigurationManager(this),
        appWidgetId,
        viewModel
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppWidgetConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app.appComponent.inject(this)
        setResult(RESULT_CANCELED)

        viewModel = viewModel(::initialDishesAppWidgetViewModel)
        viewModelController = createController(viewModel)

        viewModel.networkError.observeNullSafe(this) {
            if (it) {
                showNetworkError()
            }
        }
        viewModel.restaurants.observeNullSafe(this) {
            binding.widgetConfigSpinner.adapter = RestaurantSpinnerAdapter(this, it)
        }
        viewModel.confirmButtonStatus.observeNullSafe(this) {
            binding.widgetConfigConfirm.isEnabled = it
        }
        viewModel.showProgress.observeNullSafe(this) {
            binding.widgetConfigProgressBar.visibility = if (it) View.VISIBLE else View.INVISIBLE
        }
        viewModel.status.observeNullSafe(this) {
            when (it) {
                Status.Confirmed -> {
                    if (!viewModel.networkError.data && viewModel.restaurants.data.isNotEmpty()) {
                        setResult(RESULT_OK, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
                    } else {
                        setResult(Activity.RESULT_CANCELED, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
                    }
                    finish()
                }
                Status.Open -> {
                }
                Status.Canceled -> {
                    setResult(Activity.RESULT_CANCELED, Intent().apply { putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId) })
                    finish()
                }
            }.exhaustive
        }

        binding.widgetConfigConfirm.setOnClickListener {
            viewModelController.onConfirmClicked(binding.widgetConfigSpinner.selectedItemPosition)
            DishesWidgetUpdateService.scheduleUpdate(15, this, appWidgetId)
        }
        binding.widgetConfigCancel.setOnClickListener { viewModelController.onCancel() }

        viewModelController.load()
    }

    /**
     * Notify the user about a network error.
     */
    private fun showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
    }

}

