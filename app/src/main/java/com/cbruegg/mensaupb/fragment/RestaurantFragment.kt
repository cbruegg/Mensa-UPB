package com.cbruegg.mensaupb.fragment

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.model.Restaurant
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Fragment hosting a Pager of DishesFragments.
 * The factory method newInstance needs to be used.
 */
class RestaurantFragment : Fragment() {
    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val DAY_COUNT = 7

        /**
         * Construct a new instance of the RestaurantFragment.
         */
        fun newInstance(restaurant: Restaurant): RestaurantFragment {
            val args = Bundle()
            args.putString(ARG_RESTAURANT, restaurant.serialize())
            val fragment = RestaurantFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val dayPager: ViewPager by bindView(R.id.day_pager)
    private val dayPagerTabs: TabLayout by bindView(R.id.day_pager_tabs)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**
         * Set up the view pager
         */
        val restaurant = Restaurant.deserialize(arguments.getString(ARG_RESTAURANT))
        val dates = computePagerDates()
        val adapter = DishesPagerAdapter(activity, fragmentManager, restaurant, dates)
        dayPager.adapter = adapter

        dayPagerTabs.setupWithViewPager(dayPager)
    }

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
    private class DishesPagerAdapter(context: Context, fm: FragmentManager, private val restaurant: Restaurant, private val dates: List<Date>) : FragmentStatePagerAdapter(fm) {

        private val dateFormatter = SimpleDateFormat(context.getString(R.string.dateTabFormat))

        override fun getItem(position: Int) = DishesFragment.newInstance(restaurant, dates[position])

        override fun getCount() = dates.size

        override fun getPageTitle(position: Int) = dateFormatter.format(dates[position])
    }

}
