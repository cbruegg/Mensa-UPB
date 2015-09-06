package com.cbruegg.mensaupb

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.bindView
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.downloader.downloadRestaurants
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.fragment.DishesFragment
import com.cbruegg.mensaupb.fragment.RestaurantFragment
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.view.DividerItemDecoration
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class MainActivity : AppCompatActivity() {

    private val DEFAULT_RESTAURANT_NAME = "Mensa Academica"

    private val restaurantList: RecyclerView by bindView(R.id.restaurant_list)
    private val drawerLayout: DrawerLayout by bindView(R.id.drawer_layout)

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val restaurantAdapter = RestaurantAdapter()
        restaurantAdapter.onClickListener = { restaurant, position ->
            drawerLayout.closeDrawer(GravityCompat.START)
            showRestaurant(restaurant)
        }
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
                    loadDefaultRestaurant(preparedList)
                    subscription?.unsubscribe()
                }

    }

    private fun loadDefaultRestaurant(preparedList: List<Restaurant>) {
        val restaurant = preparedList
                .firstOrNull { it.name.toLowerCase().equals(DEFAULT_RESTAURANT_NAME.toLowerCase()) }
                .let { it ?: preparedList.firstOrNull() }
        if (restaurant != null) {
            showRestaurant(restaurant)
        }
    }

    private fun showRestaurant(restaurant: Restaurant) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, RestaurantFragment.newInstance(restaurant))
                .commit()
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}
