package com.cbruegg.mensaupb.restaurant

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.dishes.DishesFragment
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.midnight
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import io.requery.Persistable
import io.requery.kotlin.BlockingEntityStore
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
@Deprecated(message = "Use method with arguments.", level = DeprecationLevel.WARNING) constructor() : Fragment() {

    private lateinit var viewModel: RestaurantViewModel
    private lateinit var viewModelController: RestaurantViewModelController

    private lateinit var adapter: DishesPagerAdapter

    @Inject
    lateinit var data: BlockingEntityStore<Persistable>

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
            context!!, childFragmentManager, viewModel.restaurant,
            pagerInfo.dates, requestedDishName, pagerIndex
        )
        dayPager.adapter = adapter
        dayPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                viewModel.pagerInfo.value.position = pagerInfo.dates[position]
            }
        })
        dayPagerTabs.setupWithViewPager(dayPager)
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
        context: Context,
        fm: FragmentManager,
        private val restaurant: DbRestaurant,
        val dates: List<Date>,
        /**
         * If set, look for a matching dish on the page
         * specified by [dishNamePositionInPager] and display its image
         */
        private val dishName: String?,
        private val dishNamePositionInPager: Int?
    ) : FragmentStatePagerAdapter(fm) {

        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        override fun getItem(position: Int) = DishesFragment(
            restaurant, dates[position],
            if (position == dishNamePositionInPager) dishName else null
        )

        override fun getCount() = dates.size

        override fun getPageTitle(position: Int): String = dateFormatter.format(dates[position])
    }

}
