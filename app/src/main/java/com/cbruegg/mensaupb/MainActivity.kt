package com.cbruegg.mensaupb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.bindView
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.downloader.downloadRestaurants
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.fragment.RestaurantFragment
import com.cbruegg.mensaupb.view.DividerItemDecoration
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class MainActivity : AppCompatActivity() {

    private val restaurantList: RecyclerView by bindView(R.id.restaurant_list)
    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restaurantAdapter = RestaurantAdapter()
        restaurantList.addItemDecoration(DividerItemDecoration(this))
        restaurantList.setAdapter(restaurantAdapter)
        restaurantList.setLayoutManager(LinearLayoutManager(this))

        subscription = downloadRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val preparedList = it
                            .filter { it.isActive }
                            .sortBy { first, second -> first.location.compareTo(second.location) }
                            .reverse() // Paderborn should be at the top of the list
                    restaurantAdapter.list.setAll(preparedList)
                    subscription?.unsubscribe()

                    val bistro = it[5]
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, RestaurantFragment.newInstance(bistro))
                            .commit()
                }

    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}
