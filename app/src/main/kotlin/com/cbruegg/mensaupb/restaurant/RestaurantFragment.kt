package com.cbruegg.mensaupb.restaurant

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.databinding.FragmentRestaurantBinding
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.midnight
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import com.google.android.material.tabs.TabLayoutMediator
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

private const val ARG_RESTAURANT = "restaurant"
private const val ARG_REQUESTED_PAGER_POSITION = "pager_position"
private const val ARG_DISH_NAME = "dish_name"
private const val ARG_BOTTOM_PADDING = "bottom_padding"

/**
 * Construct a new instance of the RestaurantFragment.
 * @param pagerPosition The position the pager should be initially set to
 * @param dishName If set, the fragment looks for a matching
 * dish on the first page only and shows its image
 */
@Suppress("FunctionName")
fun RestaurantFragment(
    restaurant: DbRestaurant,
    bottomPadding: Int,
    pagerPosition: Date = midnight,
    dishName: String? = null
): RestaurantFragment {
    @Suppress("DEPRECATION") val fragment = RestaurantFragment()
    fragment.arguments = Bundle().apply {
        putString(ARG_RESTAURANT, restaurant.id)
        putDate(ARG_REQUESTED_PAGER_POSITION, pagerPosition)
        putString(ARG_DISH_NAME, dishName)
        putInt(ARG_BOTTOM_PADDING, bottomPadding)
    }
    return fragment
}

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method needs to be used.
 */
@SuppressLint("SimpleDateFormat")
class RestaurantFragment
@Deprecated(message = "Use method with arguments.", level = DeprecationLevel.WARNING) constructor() : Fragment() {

    private lateinit var viewModel: RestaurantViewModel
    private lateinit var viewModelController: RestaurantViewModelController

    private var adapter: DishesPagerAdapter? = null

    private val dateFormatter by lazy { SimpleDateFormat(getString(R.string.dateTabFormat)) }

    @Inject
    lateinit var data: BlockingEntityStore<Persistable>

    @Inject
    lateinit var repository: Repository

    private lateinit var binding: FragmentRestaurantBinding

    override fun onResume() {
        super.onResume()
        viewModelController.reloadIfNeeded()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentRestaurantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val arguments = arguments ?: error("No arguments supplied!")
        val requestedPagerPosition = arguments.getDate(ARG_REQUESTED_PAGER_POSITION)
        val restaurantId = arguments.getString(ARG_RESTAURANT)
        val requestedDishName = arguments.getString(ARG_DISH_NAME)
        val restaurant = data.findByKey(DbRestaurant::class, restaurantId)!!
        val bottomPadding = arguments.getInt(ARG_BOTTOM_PADDING)

        viewModel = viewModel { initialRestaurantViewModel(requestedPagerPosition, restaurant, requestedDishName) }
        viewModelController = RestaurantViewModelController(viewModel)
        viewModel.pagerInfo.observe(this) {
            display(it, viewModel.requestedDishName, bottomPadding)
            viewModel.requestedDishName = null // Request was fulfilled
        }
    }

    private fun display(pagerInfo: PagerInfo, requestedDishName: String?, bottomPadding: Int) {
        val pagerIndex = pagerInfo.dates.indexOf(pagerInfo.position) // May be -1

        val adapter = DishesPagerAdapter(
            context!!, viewModel.restaurant,
            pagerInfo.dates, requestedDishName, pagerIndex, repository,
            bottomPadding
        )
        this.adapter = adapter
        binding.dayPager.adapter = adapter
        binding.dayPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                viewModel.pagerInfo.value.position = pagerInfo.dates[position]
            }
        })
        TabLayoutMediator(binding.dayPagerTabs, binding.dayPager) { tab, position ->
            binding.dayPager.currentItem = tab.position
            tab.text = dateFormatter.format(adapter.dates[position])
        }.attach()
        binding.dayPager.currentItem = pagerIndex

        viewModel.lastLoadMeta = LastLoadMeta(pagerInfo.dates, whenLoaded = now)
    }

    /**
     * Get the date of the fragment
     * currently selected in the ViewPager.
     */
    val pagerSelectedDate: Date?
        get() = adapter?.dates?.get(binding.dayPager.currentItem)

}
