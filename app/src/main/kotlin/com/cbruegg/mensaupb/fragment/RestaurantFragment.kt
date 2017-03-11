package com.cbruegg.mensaupb.fragment

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
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method newInstance needs to be used.
 */
class RestaurantFragment : BaseFragment() {
    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_PAGER_POSITION = "pager_position"
        private val ARG_GERMAN_DISH_NAME = "german_dish_name"
        private val DAY_COUNT = 7

        /**
         * Construct a new instance of the RestaurantFragment.
         * @param pagerPosition The position the pager should be initially set to
         * @param germanDishName If set, the fragment looks for a matching
         * dish on the first page only and shows its image
         */
        fun newInstance(restaurant: DbRestaurant,
                        pagerPosition: Int = 0,
                        germanDishName: String? = null): RestaurantFragment {
            val fragment = RestaurantFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_RESTAURANT, restaurant)
                putInt(ARG_PAGER_POSITION, Math.min(pagerPosition, DAY_COUNT))
                putString(ARG_GERMAN_DISH_NAME, germanDishName)
            }
            return fragment
        }
    }

    // TODO Don't save current position but selected day and restore it

    private val dayPager: ViewPager by bindView(R.id.day_pager)
    private val dayPagerTabs: TabLayout by bindView(R.id.day_pager_tabs)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Set up the view pager
         */
        val restaurant = arguments.getParcelable<DbRestaurant>(ARG_RESTAURANT)
        val pagerPosition = arguments.getInt(ARG_PAGER_POSITION)
        val dates = computePagerDates()
        val adapter = DishesPagerAdapter(activity, childFragmentManager, restaurant, dates,
                arguments.getString(ARG_GERMAN_DISH_NAME))
        dayPager.adapter = adapter
        dayPagerTabs.setupWithViewPager(dayPager)
        dayPager.currentItem = pagerPosition
    }

    /**
     * Get the current position of the pager
     */
    val pagerPosition: Int
        get() = dayPager.currentItem

    /**
     * Return a list of dates to be used for fetching dishes.
     */
    private fun computePagerDates(): List<Date> {
        val today = System.currentTimeMillis()
        val dayInMs = TimeUnit.DAYS.toMillis(1)
        return (0..DAY_COUNT - 1).map { Date(today + it * dayInMs) }
    }

    /**
     * ViewPager adapter
     */
    private class DishesPagerAdapter(context: Context,
                                     fm: FragmentManager,
                                     private val restaurant: DbRestaurant,
                                     private val dates: List<Date>,
                                     /**
                                      * If set, look for a matching dish on the first page
                                      * and display its image
                                      */
                                     private val germanDishName: String?) : FragmentStatePagerAdapter(fm) {

        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        override fun getItem(position: Int) = DishesFragment.newInstance(restaurant, dates[position],
                if (position == 0) germanDishName else null)

        override fun getCount() = dates.size

        override fun getPageTitle(position: Int): String = dateFormatter.format(dates[position])
    }

}
