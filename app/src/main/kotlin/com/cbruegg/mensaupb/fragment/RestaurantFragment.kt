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
import com.cbruegg.mensaupb.dishes.DishesFragment
import com.cbruegg.mensaupb.extensions.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method newInstance needs to be used.
 */
class RestaurantFragment : NoMvpBaseFragment() {
    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_PAGER_POSITION = "pager_position"
        private val ARG_GERMAN_DISH_NAME = "german_dish_name"
        private val DAY_COUNT = 7L

        /**
         * Construct a new instance of the RestaurantFragment.
         * @param pagerPosition The position the pager should be initially set to
         * @param germanDishName If set, the fragment looks for a matching
         * dish on the first page only and shows its image
         */
        fun newInstance(restaurant: DbRestaurant,
                        pagerPosition: Date = Date().atMidnight(), // TODO Replace project-wide with midnight / now
                        germanDishName: String? = null): RestaurantFragment {
            val fragment = RestaurantFragment()
            val restrictedPagerPosition = pagerPosition.inRangeOrElse(
                    Date().atMidnight(),
                    Date().atMidnight() + TimeUnit.DAYS.toMillis(DAY_COUNT - 1),
                    Date().atMidnight()
            )
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_RESTAURANT, restaurant)
                putDate(ARG_PAGER_POSITION, restrictedPagerPosition)
                putString(ARG_GERMAN_DISH_NAME, germanDishName)
            }
            return fragment
        }
    }

    // TODO Don't save current position but selected day and restore it

    private val dayPager: ViewPager by bindView(R.id.day_pager)
    private val dayPagerTabs: TabLayout by bindView(R.id.day_pager_tabs)
    private lateinit var adapter: DishesPagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Set up the view pager
         */
        val restaurant = arguments.getParcelable<DbRestaurant>(ARG_RESTAURANT)
        val pagerPosition = arguments.getDate(ARG_PAGER_POSITION)
        val dates = computePagerDates()
        adapter = DishesPagerAdapter(activity, childFragmentManager, restaurant, dates,
                arguments.getString(ARG_GERMAN_DISH_NAME))
        dayPager.adapter = adapter
        dayPagerTabs.setupWithViewPager(dayPager)
        dayPager.currentItem = dates.indexOf(pagerPosition)
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
        return (0..DAY_COUNT - 1).map { Date(today + it * dayInMs).atMidnight() }
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
                                     private val germanDishName: String?) : FragmentStatePagerAdapter(fm) {

        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        override fun getItem(position: Int) = DishesFragment.newInstance(restaurant, dates[position],
                if (position == 0) germanDishName else null)

        override fun getCount() = dates.size

        override fun getPageTitle(position: Int): String = dateFormatter.format(dates[position])
    }

}
