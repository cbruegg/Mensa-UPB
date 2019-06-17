package com.cbruegg.mensaupb.restaurant

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.adapter.DishListViewModelAdapter
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.dishes.DishesViewModelController
import com.cbruegg.mensaupb.dishes.initialDishesViewModel
import com.cbruegg.mensaupb.dishes.showDishDetailsDialog
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.util.lifecycle.LifecycleRecyclerAdapter
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import com.google.android.material.snackbar.Snackbar
import java.util.Date

/**
 * ViewPager adapter
 */
class DishesPagerAdapter(
    private val context: Context,
    private val restaurant: DbRestaurant,
    val dates: List<Date>,
    /**
     * If set, look for a matching dish on the page
     * specified by [dishNamePositionInPager] and display its image
     */
    private val dishName: String?,
    private val dishNamePositionInPager: Int?,
    private val repository: Repository
) : LifecycleRecyclerAdapter<DishesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DishesViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_dishes, parent, false))

    override fun getItemCount() = dates.size

    private val glide = GlideApp.with(context)

    override fun onBindViewHolderImpl(holder: DishesViewHolder, position: Int) {
        val date = dates[position]
        val dishName = if (position == dishNamePositionInPager) dishName else null

        val viewModel = initialDishesViewModel()
        val viewModelController = DishesViewModelController(
            repository,
            restaurant,
            date,
            context.userType,
            { toDishViewModels(context, it) },
            dishName,
            viewModel
        )

        val adapter = DishListViewModelAdapter(glide)
        adapter.onClickListener = { dishViewModel, _ -> viewModelController.onDishClicked(dishViewModel) }
        holder.dishList.adapter = adapter
        holder.dishList.layoutManager = LinearLayoutManager(context)
        holder.dishList.addOnScrollListener(RecyclerViewPreloader(context as Activity, adapter, adapter, 5))

        viewModel.showDialogFor.observe(holder) { dishViewModel ->
            if (dishViewModel != null) {
                context.showDishDetailsDialog(dishViewModel)
            }
        }
        viewModel.dishViewModels.observe(holder) { dishListViewModels ->
            adapter.list.setAll(dishListViewModels)
            holder.noDishesMessage.visibility = noDishesMessageVisibility(dishListViewModels, viewModel.isLoading.data)
        }
        viewModel.isLoading.observe(holder) {
            holder.dishProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            holder.noDishesMessage.visibility = noDishesMessageVisibility(viewModel.dishViewModels.data, it)
        }
        viewModel.isStale.observe(holder) { isStale ->
            if (isStale) {
                Snackbar.make(holder.itemView, R.string.showing_stale_data, Snackbar.LENGTH_SHORT).show()
            }
        }
        viewModel.networkError.observe(holder) {
            holder.networkErrorMessage.visibility = if (it) View.VISIBLE else View.GONE
        }

        viewModelController.reloadIfNeeded()
    }

    private fun noDishesMessageVisibility(dishListViewModels: List<DishListViewModel>, isLoading: Boolean) =
        if (!isLoading && dishListViewModels.isEmpty()) View.VISIBLE else View.GONE

}