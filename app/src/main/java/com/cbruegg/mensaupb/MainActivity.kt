package com.cbruegg.mensaupb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.downloader.downloadRestaurants
import kotlinx.android.synthetic.activity_main.restaurant_list
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class MainActivity : AppCompatActivity() {

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restaurantAdapter = RestaurantAdapter()
        restaurant_list.setAdapter(restaurantAdapter)
        restaurant_list.setLayoutManager(LinearLayoutManager(this))

        subscription = downloadRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    restaurantAdapter.list.clear()
                    restaurantAdapter.list.addAll(it)
                    subscription?.unsubscribe()
                }

    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}
