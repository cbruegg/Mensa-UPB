package com.cbruegg.mensaupb.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.extensions.toggleDrawer
import com.cbruegg.mensaupb.fragment.RestaurantFragment
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.view.DividerItemDecoration
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * The main activity of the app. It's responsible for keeping the restaurant drawer updated and hosts fragments.
 */
class MainActivity : AppCompatActivity() {

    companion object {

        private val ARG_RESTAURANT_ID = "restaurant_id"
        private val ARG_DISH_GERMAN_NAME = "dish_german_name"

        fun createStartIntent(context: Context, restaurant: Restaurant? = null, dish: Dish? = null): Intent
                = Intent(context, MainActivity::class.java).apply {
            fillIntent(this, restaurant, dish)
        }

        fun fillIntent(intent: Intent, restaurant: Restaurant? = null, dish: Dish? = null) {
            intent.putExtra(ARG_RESTAURANT_ID, restaurant?.id)
            intent.putExtra(ARG_DISH_GERMAN_NAME, dish?.germanName)
        }
    }

    /*
     * Constants
     */

    private val DEFAULT_RESTAURANT_NAME = "Mensa Academica"
    private val PREFS_FILE_NAME = "main_activity_prefs"
    private val PREFS_KEY_FIRST_LAUNCH = "main_activity_first_launch"
    private val PREFS_KEY_LAST_SELECTED_RESTAURANT = "last_selected_restaurant"
    private val STUDENTENWERK_URI = Uri.parse("http://www.studentenwerk-pb.de/gastronomie/")
    private val STUDENTENWERK_OPENING_HOURS_URI = Uri.parse("http://www.studentenwerk-pb.de/gastronomie/oeffnungszeiten")
    private val REQUEST_CODE_PREFERENCES = 1

    /*
     * Views
     */

    private val restaurantList: RecyclerView by bindView(R.id.restaurant_list)
    private val drawerLayout: DrawerLayout by bindView(R.id.drawer_layout)

    /*
     * Other vars
     */

    private var subscription: Subscription? = null
    private var lastRestaurantId: String?
        get() = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).getString(PREFS_KEY_LAST_SELECTED_RESTAURANT, null)
        set(new) {
            getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putString(PREFS_KEY_LAST_SELECTED_RESTAURANT, new)
                    .apply()
        }
    private var lastRestaurant: Restaurant? = null

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
        reload()
    }

    private fun reload() {
        val restaurantAdapter = restaurantList.adapter as RestaurantAdapter
        subscription = Downloader(this).downloadOrRetrieveRestaurants()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    it.fold({ showNetworkError(restaurantAdapter) }) {
                        val preparedList = it
                                .sortBy { first, second -> first.location.compareTo(second.location) }
                                .reversed() // Paderborn should be at the top of the list
                        restaurantAdapter.list.setAll(preparedList)
                        checkShowFirstTimeDrawer()
                        loadDefaultRestaurant(preparedList)
                    }
                    subscription?.unsubscribe()
                }
    }

    private fun showNetworkError(restaurantAdapter: RestaurantAdapter) {
        restaurantAdapter.list.setAll(emptyList())
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
    }

    /**
     * If this is the first time the user opens the app, show the drawer so
     * the user knows about its existence.
     */
    private fun checkShowFirstTimeDrawer() {
        val sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
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
                startActivityForResult(intent, REQUEST_CODE_PREFERENCES)
                return true
            }
            R.id.restaurants -> {
                drawerLayout.toggleDrawer(GravityCompat.START)
                return true
            }
            R.id.stw_url -> {
                startActivity(Intent(Intent.ACTION_VIEW, STUDENTENWERK_URI))
                return true
            }
            R.id.opening_hours -> {
                startActivity(Intent(Intent.ACTION_VIEW, STUDENTENWERK_OPENING_HOURS_URI))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PREFERENCES) {
            lastRestaurant?.let { showRestaurant(it) }
        }
    }

    /**
     * Load a default restaurant fragment. If found in the list of restaurants,
     * the last used restaurant or else if found the [DEFAULT_RESTAURANT_NAME] is used,
     * else the first item in the list.
     */
    private fun loadDefaultRestaurant(preparedList: List<Restaurant>) {
        val restaurant = preparedList.firstOrNull { it.id == intent.getStringExtra(ARG_RESTAURANT_ID) }
                ?: preparedList.firstOrNull { it.id == lastRestaurantId }
                ?: preparedList.firstOrNull { it.name.toLowerCase() == DEFAULT_RESTAURANT_NAME.toLowerCase() }
                ?: preparedList.firstOrNull()
        if (restaurant != null) {
            showRestaurant(restaurant)
        }
    }

    /**
     * Show a fragment that displays the dishes for the specified restaurant.
     * Also updates the [lastRestaurantId].
     */
    private fun showRestaurant(restaurant: Restaurant) {
        val currentPagerPosition = (supportFragmentManager
                .findFragmentById(R.id.fragment_container) as? RestaurantFragment)
                ?.pagerPosition()

        lastRestaurant = restaurant
        lastRestaurantId = restaurant.id
        supportActionBar?.title = restaurant.name
        val germanDishName: String? = intent.getStringExtra(ARG_DISH_GERMAN_NAME)
        val restaurantFragment = RestaurantFragment.newInstance(restaurant,
                currentPagerPosition ?: 0,
                germanDishName)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, restaurantFragment)
                .commit()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        reload()
    }

    override fun onDestroy() {
        subscription?.unsubscribe()
        super.onDestroy()
    }

}