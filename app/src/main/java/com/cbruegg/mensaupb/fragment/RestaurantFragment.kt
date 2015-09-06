package com.cbruegg.mensaupb.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.DishAdapter
import com.cbruegg.mensaupb.downloader.downloadDishes
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.model.Restaurant
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.Date
import butterknife.bindView

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

    private val dishList: RecyclerView by bindView(R.id.dish_list)
    private var subscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_restaurant, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DishAdapter()
        dishList.setAdapter(adapter)
        dishList.setLayoutManager(LinearLayoutManager(getActivity()))

        val restaurant = Restaurant.deserialize(getArguments().getString(ARG_RESTAURANT))!!

        subscription = downloadDishes(restaurant, Date())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    Log.i("Dishes", it.toString())
                    adapter.list.setAll(it)
                    subscription?.unsubscribe()
                }
    }

    override fun onDestroyView() {
        subscription?.unsubscribe()
        super.onDestroyView()
    }
}