package com.cbruegg.mensaupb.restaurant

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.adapter.DishListViewModelAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.dishes.DishesViewModelController
import com.cbruegg.mensaupb.dishes.initialDishesViewModel
import com.cbruegg.mensaupb.dishes.showDishDetailsDialog
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.midnight
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_restaurant.*
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

private const val ARG_RESTAURANT = "restaurant"
private const val ARG_REQUESTED_PAGER_POSITION = "pager_position"
private const val ARG_DISH_NAME = "dish_name"

/**
 * Construct a new instance of the RestaurantFragment.
 * @param pagerPosition The position the pager should be initially set to
 * @param dishName If set, the fragment looks for a matching
 * dish on the first page only and shows its image
 */
@Suppress("FunctionName")
fun RestaurantFragment(
    restaurant: DbRestaurant,
    pagerPosition: Date = midnight,
    dishName: String? = null
): RestaurantFragment {
    @Suppress("DEPRECATION") val fragment = RestaurantFragment()
    fragment.arguments = Bundle().apply {
        putString(ARG_RESTAURANT, restaurant.id)
        putDate(ARG_REQUESTED_PAGER_POSITION, pagerPosition)
        putString(ARG_DISH_NAME, dishName)
    }
    return fragment
}

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method needs to be used.
 */
class RestaurantFragment
@Deprecated(message = "Use method with arguments.", level = DeprecationLevel.WARNING) constructor() : androidx.fragment.app.Fragment() {

    private lateinit var viewModel: RestaurantViewModel
    private lateinit var viewModelController: RestaurantViewModelController

    private lateinit var adapter: DishesPagerAdapter

    @Inject
    lateinit var data: BlockingEntityStore<Persistable>

    @Inject
    lateinit var repository: Repository

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onResume() {
        super.onResume()
        viewModelController.reloadIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val arguments = arguments ?: error("No arguments supplied!")
        val requestedPagerPosition = arguments.getDate(ARG_REQUESTED_PAGER_POSITION)
        val restaurantId = arguments.getString(ARG_RESTAURANT)
        val requestedDishName = arguments.getString(ARG_DISH_NAME)
        val restaurant = data.findByKey(DbRestaurant::class, restaurantId)!!

        viewModel = viewModel { initialRestaurantViewModel(requestedPagerPosition, restaurant, requestedDishName) }
        viewModelController = RestaurantViewModelController(viewModel)
        viewModel.pagerInfo.observe(this) {
            display(it, viewModel.requestedDishName)
            viewModel.requestedDishName = null // Request was fulfilled
        }
    }

    private fun display(pagerInfo: PagerInfo, requestedDishName: String?) {
        val pagerIndex = pagerInfo.dates.indexOf(pagerInfo.position) // May be -1

        adapter = DishesPagerAdapter(
            context!!, viewModel.restaurant,
            pagerInfo.dates, requestedDishName, pagerIndex, repository
        )
        dayPager.adapter = adapter
        dayPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.pagerInfo.value.position = pagerInfo.dates[position]
            }
        })
        TabLayoutMediator(dayPagerTabs, dayPager) { tab, position ->
            dayPager.currentItem = tab.position
            tab.text = adapter.getPageTitle(position)
        }.attach()
        dayPager.currentItem = pagerIndex

        viewModel.lastLoadMeta = LastLoadMeta(pagerInfo.dates, whenLoaded = now)
    }

    /**
     * Get the date of the fragment
     * currently selected in the ViewPager.
     */
    val pagerSelectedDate: Date
        get() = adapter.dates[dayPager.currentItem]

    /**
     * ViewPager adapter
     */
    private class DishesPagerAdapter(
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
    ) : RecyclerView.Adapter<DishesViewHolder>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DishesViewHolder(LayoutInflater.from(context).inflate(R.layout.fragment_dishes, parent, false))

        override fun getItemCount() = dates.size

        override fun onBindViewHolder(holder: DishesViewHolder, position: Int) {
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

            val adapter = DishListViewModelAdapter(GlideApp.with(context))
            adapter.onClickListener = { dishViewModel, _ -> viewModelController.onDishClicked(dishViewModel) }
            holder.dishList.adapter = adapter
            holder.dishList.layoutManager = LinearLayoutManager(context)
            holder.dishList.addOnScrollListener(RecyclerViewPreloader(context as Activity, adapter, adapter, 5))

            // TODO Observe with lifecycle
            viewModel.showDialogFor.observeForever { dishViewModel ->
                if (dishViewModel != null) {
                    context.showDishDetailsDialog(dishViewModel)
                }
            }
            viewModel.dishViewModels.observeForever { dishListViewModels ->
                adapter.list.setAll(dishListViewModels)
                holder.noDishesMessage.visibility = noDishesMessageVisibility(dishListViewModels, viewModel.isLoading.data)
            }
            viewModel.isLoading.observeForever {
                holder.dishProgressBar.visibility = if (it) View.VISIBLE else View.GONE
                holder.noDishesMessage.visibility = noDishesMessageVisibility(viewModel.dishViewModels.data, it)
            }
            viewModel.isStale.observeForever { isStale ->
                if (isStale) {
                    Snackbar.make(holder.itemView, R.string.showing_stale_data, Snackbar.LENGTH_SHORT).show()
                }
            }
            viewModel.networkError.observeForever {
                holder.networkErrorMessage.visibility = if (it) View.VISIBLE else View.GONE
            }

            viewModelController.reloadIfNeeded()
        }

        private fun noDishesMessageVisibility(dishListViewModels: List<DishListViewModel>, isLoading: Boolean) =
            if (!isLoading && dishListViewModels.isEmpty()) View.VISIBLE else View.GONE

        @SuppressLint("SimpleDateFormat")
        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        fun getPageTitle(position: Int): String = dateFormatter.format(dates[position])
    }

    private class DishesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LayoutContainer {
        override val containerView: View?
            get() = itemView


        val dishList: RecyclerView = itemView.findViewById(R.id.dishList)
        val noDishesMessage: View = itemView.findViewById(R.id.noDishesMessage)
        val dishProgressBar: ProgressBar = itemView.findViewById(R.id.dishProgressBar)
        val networkErrorMessage: View = itemView.findViewById(R.id.networkErrorMessage)
    }

}
