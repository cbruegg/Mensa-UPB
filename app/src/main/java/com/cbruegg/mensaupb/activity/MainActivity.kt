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

/**
 * The main activity of the app. It's responsible for keeping the restaurant drawer updated and hosts fragments.
 */
public class MainActivity : AppCompatActivity() {

    /*
     * Constants
     */

    private val DEFAULT_RESTAURANT_NAME = "Mensa Academica"
    private val PREFS_KEY_FIRST_LAUNCH = "main_activity_first_launch"

    /*
     * Views
     */

    private val restaurantList: RecyclerView by bindView(R.id.restaurant_list)
    private val drawerLayout: DrawerLayout by bindView(R.id.drawer_layout)

    /*
     * Other vars
     */

    private var subscription: Subscription? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setContentView(R.layout.activity_main)

        // Setup the restaurant list in the drawer
        val restaurantAdapter = RestaurantAdapter()
        restaurantAdapter.onClickListener = { restaurant, position ->
            drawerLayout.closeDrawer(GravityCompat.START)
            showRestaurant(restaurant)
        }
        restaurantList.addItemDecoration(DividerItemDecoration(this))
        restaurantList.adapter = restaurantAdapter
        restaurantList.layoutManager = LinearLayoutManager(this)

        // Download data for the list
        subscription = Downloader(this).downloadRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val preparedList = it
                            .filter { it.isActive }
                            .sortBy { first, second -> first.location.compareTo(second.location) }
                            .reversed() // Paderborn should be at the top of the list
                    restaurantAdapter.list.setAll(preparedList)
                    checkShowFirstTimeDrawer()
                    loadDefaultRestaurant(preparedList)
                    subscription?.unsubscribe()
                }

    }

    /**
     * If this is the first time the user opens the app, show the drawer so
     * the user knows about its existence.
     */
    private fun checkShowFirstTimeDrawer() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstLaunch = sharedPreferences.getBoolean(PREFS_KEY_FIRST_LAUNCH, true)
        if (firstLaunch) {
            drawerLayout.openDrawer(GravityCompat.START)
            sharedPreferences.edit().putBoolean(PREFS_KEY_FIRST_LAUNCH, false).apply()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, PreferenceActivity::class.java)
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

    /**
     * Load a default restaurant fragment. If found in the list of restaurants,
     * [DEFAULT_RESTAURANT_NAME] is used, else the first item in the list.
     */
    private fun loadDefaultRestaurant(preparedList: List<Restaurant>) {
        val restaurant = preparedList
                .firstOrNull { it.name.toLowerCase() == DEFAULT_RESTAURANT_NAME.toLowerCase() }
                .let { it ?: preparedList.firstOrNull() }
        if (restaurant != null) {
            showRestaurant(restaurant)
        }
    }

    /**
     * Show a fragment that displays the dishes for the specified restaurant.
     */
    private fun showRestaurant(restaurant: Restaurant) {
        supportActionBar.title = restaurant.name
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, RestaurantFragment.newInstance(restaurant))
                .commit()
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}
