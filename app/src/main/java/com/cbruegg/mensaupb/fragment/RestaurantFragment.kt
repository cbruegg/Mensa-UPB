package com.cbruegg.mensaupb.fragment

import android.os.Bundle
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
import java.util.*
import java.util.concurrent.TimeUnit


class RestaurantFragment : Fragment() {
    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val DAY_COUNT = 7

        fun newInstance(restaurant: Restaurant): RestaurantFragment {
            val args = Bundle()
            args.putString(ARG_RESTAURANT, restaurant.serialize())
            val fragment = RestaurantFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    private val dayPager: ViewPager by bindView(R.id.day_pager)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurant = Restaurant.deserialize(getArguments().getString(ARG_RESTAURANT))!!
        val dates = computePagerDates()
        val adapter = DishesPagerAdapter(getFragmentManager(), restaurant, dates)
        dayPager.setAdapter(adapter)
    }

    private fun computePagerDates(): List<Date> {
        val dates = ArrayList<Date>(DAY_COUNT)
        val todayMs = System.currentTimeMillis()
        val dayInMs = TimeUnit.DAYS.toMillis(1)
        for (i in 0..DAY_COUNT) {
            dates.add(Date(todayMs + i * dayInMs))
        }
        return dates
    }

    class DishesPagerAdapter(fm: FragmentManager, private val restaurant: Restaurant, private val dates: List<Date>) : FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int) = DishesFragment.newInstance(restaurant, dates[position])

        override fun getCount() = dates.size()
    }

}