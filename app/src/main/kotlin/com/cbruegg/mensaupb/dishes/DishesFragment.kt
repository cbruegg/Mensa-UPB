package com.cbruegg.mensaupb.dishes

import android.arch.lifecycle.LifecycleFragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import kotterknife.bindView
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.adapter.DishListViewModelAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import java.util.Date
import javax.inject.Inject

private const val ARG_RESTAURANT = "restaurant"
private const val ARG_DATE = "date"
private const val ARG_DISH_NAME = "dish_name"

/**
 * Construct a new instance of this fragment.
 * @param dishName If non-null, look for a matching dish and
 * shows its image.
 * @see DishesFragment
 */
fun DishesFragment(restaurant: DbRestaurant, date: Date, dishName: String? = null): DishesFragment {
    @Suppress("DEPRECATION") val fragment = DishesFragment()
    fragment.arguments = Bundle().apply {
        putString(ARG_RESTAURANT, restaurant.id)
        putDate(ARG_DATE, date)
        putString(ARG_DISH_NAME, dishName)
    }
    return fragment
}

/**
 * Fragment responsible for displaying the dishes of a restaurant at a specified date.
 * The factory method newInstance needs to be used.
 */
class DishesFragment
@Deprecated(message = "Use method with arguments.", level = DeprecationLevel.WARNING) constructor()
    : LifecycleFragment() {

    private val dishList: RecyclerView by bindView(R.id.dish_list)
    private val noDishesMessage: TextView by bindView(R.id.no_dishes_message)
    private val networkErrorMessage: TextView by bindView(R.id.network_error_message)
    private val progressBar: ProgressBar by bindView(R.id.dish_progress_bar)
    @Inject lateinit var repository: Repository
    @Inject lateinit var data: BlockingEntityStore<Persistable>

    private val adapter by lazy { DishListViewModelAdapter(GlideApp.with(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
    }

    private lateinit var viewModel: DishesViewModel
    private lateinit var viewModelController: DishesViewModelController

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = viewModel {
            initialDishesViewModel()
        }
        val appContext = app
        val restaurant = data.findByKey(DbRestaurant::class, arguments.getString(ARG_RESTAURANT))
                ?: throw IllegalArgumentException("Supplied restaurant ID is not in the database!")

        viewModelController = DishesViewModelController(
                repository,
                restaurant,
                arguments.getDate(ARG_DATE)!!,
                context.userType,
                { toDishViewModels(appContext, it) },
                arguments.getString(ARG_DISH_NAME),
                viewModel
        )

        viewModel.showDialogFor.observe(this) { dishViewModel ->
            if (dishViewModel != null) {
                showDishDetailsDialog(context, dishViewModel) {
                    viewModelController.onDetailsDialogDismissed()
                }
            }
        }
        viewModel.dishViewModels.observe(this) { dishListViewModels ->
            showDishes(dishListViewModels)
            noDishesMessage.visibility = noDishesMessageVisibility(dishListViewModels, viewModel.isLoading.data)
        }
        viewModel.isLoading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
            noDishesMessage.visibility = noDishesMessageVisibility(viewModel.dishViewModels.data, it)
        }
        viewModel.isStale.observe(this) { isStale ->
            if (isStale) {
                delayUntilVisible {
                    Snackbar.make(view!!, R.string.showing_stale_data, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.networkError.observe(this) {
            networkErrorMessage.visibility = if (it) View.VISIBLE else View.GONE
        }
    }

    private fun noDishesMessageVisibility(dishListViewModels: List<DishListViewModel>, isLoading: Boolean) =
            if (!isLoading && dishListViewModels.isEmpty()) View.VISIBLE else View.GONE

    override fun onResume() {
        super.onResume()
        viewModelController.reloadIfNeeded()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_dishes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.onClickListener = { dishViewModel, _ -> viewModelController.onDishClicked(dishViewModel) }
        dishList.adapter = adapter
        dishList.layoutManager = LinearLayoutManager(activity)
        dishList.addOnScrollListener(RecyclerViewPreloader(this, adapter, adapter, 5))

        super.onViewCreated(view, savedInstanceState)
    }

    private fun showDishes(dishes: List<DishListViewModel>) {
        adapter.list.setAll(dishes)
    }

    private val delayedActions = mutableListOf<() -> Unit>()

    private fun delayUntilVisible(f: () -> Unit) {
        if (userVisibleHint) {
            f()
        } else {
            delayedActions += f
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        delayedActions.forEach { it() }
        delayedActions.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        delayedActions.clear()
    }
}