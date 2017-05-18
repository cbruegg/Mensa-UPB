package com.cbruegg.mensaupb.fragment

import android.arch.lifecycle.LifecycleFragment
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.cache.oldestAllowedCacheDate
import com.cbruegg.mensaupb.dishes.DishesFragment
import com.cbruegg.mensaupb.extensions.*
import com.cbruegg.mensaupb.util.viewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method newInstance needs to be used.
 */
class RestaurantFragment : LifecycleFragment() {
    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_REQUESTED_PAGER_POSITION = "pager_position"
        private val ARG_DISH_NAME = "dish_name"
        private val DAY_COUNT = 7L

        /**
         * Construct a new instance of the RestaurantFragment.
         * @param pagerPosition The position the pager should be initially set to
         * @param dishName If set, the fragment looks for a matching
         * dish on the first page only and shows its image
         */
        fun newInstance(restaurant: DbRestaurant,
                        pagerPosition: Date = midnight,
                        dishName: String? = null): RestaurantFragment {
            val fragment = RestaurantFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_RESTAURANT, restaurant)
                putDate(ARG_REQUESTED_PAGER_POSITION, pagerPosition)
                putString(ARG_DISH_NAME, dishName)
            }
            return fragment
        }
    }

    data class LastLoadMeta(val pagerDates: List<Date>, val whenLoaded: Date)
    data class PagerInfo(var position: Date, val dates: List<Date>)
    data class ViewModel(
            val pagerInfo: MutableLiveData<PagerInfo>,
            val restaurant: DbRestaurant,
            var requestedDishName: String?,
            var lastLoadMeta: LastLoadMeta? = null
    ) : android.arch.lifecycle.ViewModel()

    private lateinit var viewModel: ViewModel

    private val dayPager: ViewPager by bindView(R.id.day_pager)
    private val dayPagerTabs: TabLayout by bindView(R.id.day_pager_tabs)
    private lateinit var adapter: DishesPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onResume() {
        super.onResume()

        val shouldReload = viewModel.lastLoadMeta?.let {
            it.whenLoaded < oldestAllowedCacheDate || it.pagerDates != computePagerDates()
        } ?: true

        if (shouldReload) {
            updateViewModel()
        }
    }

    private fun updateViewModel() {
        val dates = computePagerDates()
        val requestedPagerPosition = viewModel.pagerInfo.value!!.position
        val restrictedPagerPosition = requestedPagerPosition.inRangeOrNull(
                dates.first(),
                dates.last()
        ) ?: dates.first()

        viewModel.apply {
            pagerInfo.value = PagerInfo(restrictedPagerPosition, dates)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val dates = computePagerDates()
        val requestedPagerPosition = arguments.getDate(ARG_REQUESTED_PAGER_POSITION)
        val restrictedPagerPosition = requestedPagerPosition?.inRangeOrNull(
                dates.first(),
                dates.last()
        ) ?: dates.first()

        viewModel = viewModel<ViewModel> {
            ViewModel(
                    pagerInfo = MutableLiveData<PagerInfo>().apply {
                        value = PagerInfo(restrictedPagerPosition, dates)
                    },
                    restaurant = arguments.getParcelable<DbRestaurant>(ARG_RESTAURANT),
                    requestedDishName = arguments.getString(ARG_DISH_NAME)
            )
        }

        viewModel.pagerInfo.observe(this, Observer<PagerInfo> {
            display(it!!, viewModel.requestedDishName)
            viewModel.requestedDishName = null // Request was fulfilled
        })
    }

    private fun display(pagerInfo: PagerInfo, requestedDishName: String?) {
        adapter = DishesPagerAdapter(activity, childFragmentManager, viewModel.restaurant,
                pagerInfo.dates, requestedDishName)
        dayPager.adapter = adapter
        dayPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                viewModel.pagerInfo.value!!.position = pagerInfo.dates[position]
            }
        })
        dayPagerTabs.setupWithViewPager(dayPager)
        dayPager.currentItem = pagerInfo.dates.indexOf(pagerInfo.position)

        viewModel.lastLoadMeta = LastLoadMeta(pagerInfo.dates, whenLoaded = now)
    }

    /**
     * Get the date of the fragment
     * currently selected in the ViewPager.
     */
    val pagerSelectedDate: Date
        get() = adapter.dates[dayPager.currentItem]

    /**
     * Return a list of dates to be used for fetching dishes.
     */
    private fun computePagerDates(): List<Date> {
        val today = System.currentTimeMillis()
        val dayInMs = TimeUnit.DAYS.toMillis(1)
        return (0..DAY_COUNT - 1).map { Date(today + it * dayInMs).atMidnight }
    }

    /**
     * ViewPager adapter
     */
    private class DishesPagerAdapter(context: Context,
                                     fm: FragmentManager,
                                     private val restaurant: DbRestaurant,
                                     val dates: List<Date>,
                                     /**
                                      * If set, look for a matching dish on the first page
                                      * and display its image
                                      */
                                     private val dishName: String?) : FragmentStatePagerAdapter(fm) {

        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        override fun getItem(position: Int) = DishesFragment.newInstance(restaurant, dates[position],
                if (position == 0) dishName else null)

        override fun getCount() = dates.size

        override fun getPageTitle(position: Int): String = dateFormatter.format(dates[position])
    }

}
