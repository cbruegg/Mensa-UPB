package com.cbruegg.mensaupb.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.extensions.toggleDrawer
import com.cbruegg.mensaupb.fragment.RestaurantFragment
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.view.DividerItemDecoration
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

public class MainActivity : AppCompatActivity() {

    private val DEFAULT_RESTAURANT_NAME = "Mensa Academica"
    private val PREFS_KEY_FIRST_LAUNCH = "main_activity_first_launch"

    private val restaurantList: RecyclerView by bindView(R.id.restaurant_list)
    private val drawerLayout: DrawerLayout by bindView(R.id.drawer_layout)

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setContentView(R.layout.activity_main)

        val restaurantAdapter = RestaurantAdapter()
        restaurantAdapter.onClickListener = { restaurant, position ->
            drawerLayout.closeDrawer(GravityCompat.START)
            showRestaurant(restaurant)
        }
        restaurantList.addItemDecoration(DividerItemDecoration(this))
        restaurantList.setAdapter(restaurantAdapter)
        restaurantList.setLayoutManager(LinearLayoutManager(this))

        subscription = Downloader(this).downloadRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val preparedList = it
                            .filter { it.isActive }
                            .sortBy { first, second -> first.location.compareTo(second.location) }
                            .reverse() // Paderborn should be at the top of the list
                    restaurantAdapter.list.setAll(preparedList)
                    checkShowFirstTimeDrawer()
                    loadDefaultRestaurant(preparedList)
                    subscription?.unsubscribe()
                }

    }

    private fun checkShowFirstTimeDrawer() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstLaunch = sharedPreferences.getBoolean(PREFS_KEY_FIRST_LAUNCH, true)
        if (firstLaunch) {
            drawerLayout.openDrawer(GravityCompat.START)
            sharedPreferences.edit().putBoolean(PREFS_KEY_FIRST_LAUNCH, false).apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.settings -> {
                val intent = Intent(this, javaClass<PreferenceActivity>())
                startActivity(intent)
                return true
            }
            R.id.restaurants -> {
                drawerLayout.toggleDrawer(GravityCompat.START)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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
        getSupportActionBar().setTitle(restaurant.name)
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
