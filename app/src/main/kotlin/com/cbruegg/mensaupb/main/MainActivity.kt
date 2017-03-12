package com.cbruegg.mensaupb.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.BaseActivity
import com.cbruegg.mensaupb.activity.PreferenceActivity
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.toggleDrawer
import com.cbruegg.mensaupb.fragment.RestaurantFragment
import com.cbruegg.mensaupb.provider.DishesAppWidgetProvider
import com.cbruegg.mensaupb.util.OneOff
import com.cbruegg.mensaupb.util.delegates.StringSharedPreferencesPropertyDelegate
import java.io.IOException
import javax.inject.Inject

/**
 * The main activity of the app. It's responsible for keeping the restaurant drawer updated and hosts fragments.
 */
class MainActivity : BaseActivity<MainView, MainPresenter>(), MainView {

    companion object {

        private val ARG_RESTAURANT_ID = "restaurant_id"
        private val ARG_DISH_GERMAN_NAME = "dish_german_name"

        /**
         * Create a start intent for this activity that displays
         * a possibly supplied restaurant. If a dish is supplied,
         * the activity tries to locate it in today's list of dishes
         * of the given restaurant.
         */
        fun createStartIntent(context: Context, restaurant: DbRestaurant? = null, dish: DbDish? = null): Intent
                = Intent(context, MainActivity::class.java).apply {
            fillIntent(this, restaurant, dish)
        }

        /**
         * Same as [createStartIntent], except this fills an existing intent.
         */
        fun fillIntent(intent: Intent, restaurant: DbRestaurant? = null, dish: DbDish? = null) {
            intent.putExtra(ARG_RESTAURANT_ID, restaurant?.id)
            intent.putExtra(ARG_DISH_GERMAN_NAME, dish?.germanName)
            // TODO Add param to request show first day for widget
        }
    }

    /*
     * Constants
     */

    private val PREFS_FILE_NAME = "main_activity_prefs"
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

    @Inject lateinit var downloader: Downloader
    @Inject lateinit var oneOff: OneOff

    override val mvpViewType: Class<MainView>
        get() = MainView::class.java

    override val currentlyDisplayedDay: Int?
        get() = (supportFragmentManager
                .findFragmentById(R.id.fragment_container) as? RestaurantFragment)
                ?.pagerPosition

    override fun showAppWidgetAd() {
        AlertDialog.Builder(this)
                .setTitle(R.string.did_you_know)
                .setMessage(R.string.appwidget_ad)
                .setPositiveButton(R.string.ok, null)
                .show()
    }

    override fun setRestaurants(restaurants: List<DbRestaurant>) {
        (restaurantList.adapter as RestaurantAdapter).list.setAll(restaurants)
    }

    override fun setDrawerStatus(visible: Boolean) {
        if (visible) {
            drawerLayout.openDrawer(GravityCompat.START)
        } else {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    override fun showDishesForRestaurant(restaurant: DbRestaurant, day: Int?, showDishWithGermanName: String?) {
        restaurant.load(day, showDishWithGermanName)
    }

    override fun requestWidgetUpdate() {
        val componentName = ComponentName(this, DishesAppWidgetProvider::class.java)
        val appWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(componentName)
        val intent = Intent(this, DishesAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        sendBroadcast(intent)
    }

    override fun createPresenter() = MainPresenter(
            downloader,
            oneOff,
            MainModel(
                    intent.getStringExtra(ARG_RESTAURANT_ID),
                    intent.getStringExtra(ARG_DISH_GERMAN_NAME),
                    StringSharedPreferencesPropertyDelegate<String?>(
                            sharedPreferences = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE),
                            key = PREFS_KEY_LAST_SELECTED_RESTAURANT,
                            defaultValue = null
                    )
            )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
        setContentView(R.layout.activity_main)

        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_store_mall_directory_white_24dp)
        }

        app.appComponent.inject(this)

        // Setup the restaurant list in the drawer
        val restaurantAdapter = RestaurantAdapter()
        restaurantAdapter.onClickListener = { restaurant, _ ->
            presenter.onRestaurantClick(restaurant)
        }
        restaurantList.adapter = restaurantAdapter
        restaurantList.layoutManager = LinearLayoutManager(this)
    }

    override fun showNetworkError(ioException: IOException) {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
        ioException.printStackTrace()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            drawerLayout.toggleDrawer(GravityCompat.START)
            true
        }
        R.id.settings -> {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_PREFERENCES)
            true
        }
        R.id.stw_url -> {
            startActivity(Intent(Intent.ACTION_VIEW, STUDENTENWERK_URI))
            true
        }
        R.id.opening_hours -> {
            startActivity(Intent(Intent.ACTION_VIEW, STUDENTENWERK_OPENING_HOURS_URI))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_PREFERENCES) {
            presenter.onCameBackFromPreferences()
        }
    }

    /**
     * Show a fragment that displays the dishes for the specified restaurant.
     * @param [day] The day to display inside the fragment.
     * @param [showDishWithGermanName] If non-null, the fragment tries to load the dish details for this dish.
     */
    private fun DbRestaurant.load(day: Int?, showDishWithGermanName: String?) {
        supportActionBar?.title = name
        val restaurantFragment = RestaurantFragment.newInstance(
                this,
                day ?: 0,
                showDishWithGermanName
        )
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, restaurantFragment)
                .commit()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        this.intent = intent
        if (intent != null) {
            presenter.model.requestedRestaurantId = intent.getStringExtra(ARG_RESTAURANT_ID)
            presenter.model.showDishWithGermanName = intent.getStringExtra(ARG_DISH_GERMAN_NAME)
        }
        presenter.onRestaurantsReloadRequested()
    }

}