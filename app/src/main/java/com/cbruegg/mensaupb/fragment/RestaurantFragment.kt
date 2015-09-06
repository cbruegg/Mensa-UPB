package com.cbruegg.mensaupb.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbruegg.mensaupb.downloader.downloadDishes
import com.cbruegg.mensaupb.model.Restaurant
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

private val ARG_RESTAURANT = "restaurant"

class RestaurantFragment : Fragment() {

    companion object {
        fun newInstance(restaurant: Restaurant): RestaurantFragment {
            val args = Bundle()
            args.putString(ARG_RESTAURANT, restaurant.serialize())

            val fragment = RestaurantFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    private var subscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val restaurant = Restaurant.deserialize(getArguments().getString(ARG_RESTAURANT))!!

        subscription = downloadDishes(restaurant, Date())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { dishes ->
                    dishes.forEach { dish ->
                        Log.i("Dish", restaurant.toString() + "; " + dish.toString())
                    }
                }

        return null
    }

    override fun onDestroyView() {
        subscription?.unsubscribe()
        super.onDestroyView()
    }
}