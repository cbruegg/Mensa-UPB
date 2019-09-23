package com.cbruegg.mensaupb.main

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.GlideApp
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.PreferenceActivity
import com.cbruegg.mensaupb.adapter.RestaurantAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.databinding.ActivityMainBinding
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.getDate
import com.cbruegg.mensaupb.extensions.getDateExtra
import com.cbruegg.mensaupb.extensions.midnight
import com.cbruegg.mensaupb.extensions.putDate
import com.cbruegg.mensaupb.extensions.putDateExtra
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.extensions.toggleDrawer
import com.cbruegg.mensaupb.provider.DishesAppWidgetProvider
import com.cbruegg.mensaupb.restaurant.RestaurantFragment
import com.cbruegg.mensaupb.util.OneOff
import com.cbruegg.mensaupb.util.delegates.StringSharedPreferencesPropertyDelegate
import com.cbruegg.mensaupb.util.observe
import com.cbruegg.mensaupb.util.viewModel
import java.util.Date
import javax.inject.Inject

/**
 * The main activity of the app. It's responsible for keeping the restaurant drawer updated and hosts fragments.
 */
class MainActivity : AppCompatActivity() {

    companion object {

        private const val ARG_REQUESTED_RESTAURANT_ID = "restaurant_id"
        private const val ARG_REQUESTED_DISH_NAME = "dish_name"
        private const val ARG_REQUESTED_SELECTED_DAY = "select_day"

        /**
         * Create a start intent for this activity that displays
         * a possibly supplied restaurant. If a dish is supplied,
         * the activity tries to locate it in today's list of dishes
         * of the given restaurant.
         */
        fun createStartIntent(
            context: Context,
            restaurant: DbRestaurant? = null,
            dish: DbDish? = null,
            selectDay: Date? = null
        ): Intent = Intent(context, MainActivity::class.java).apply {
            fillIntent(this, restaurant, dish, selectDay)
        }

        /**
         * Same as [createStartIntent], except this fills an existing intent.
         */
        fun fillIntent(
            intent: Intent,
            restaurant: DbRestaurant? = null,
            dish: DbDish? = null,
            selectDay: Date? = null
        ) {
            intent.putExtra(ARG_REQUESTED_RESTAURANT_ID, restaurant?.id)
            intent.putExtra(ARG_REQUESTED_DISH_NAME, dish?.name)
            intent.putDateExtra(ARG_REQUESTED_SELECTED_DAY, selectDay)
        }
    }

    /*
     * Constants
     */

    private val prefsFileName = "main_activity_prefs"
    private val prefsKeyLastSelectedRestaurant = "last_selected_restaurant"
    private val studentenwerkUri = Uri.parse("http://www.studierendenwerk-pb.de/gastronomie/")
    private val studentenwerkOpeningHoursUri = Uri.parse("http://www.studierendenwerk-pb.de/gastronomie/oeffnungszeiten")
    private val requestCodePrefererences = 1
    private val keyCurrentlyDisplayedDay = "currently_displayed_day"

    /*
     * Views
     */

    private val restaurantAdapter by lazy { RestaurantAdapter(GlideApp.with(this)) }

    /*
     * Other vars
     */

    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var oneOff: OneOff

    private var isLoading: Boolean
        get() = binding.mainProgressBar.visibility == View.VISIBLE
        set(value) {
            binding.mainProgressBar.visibility = if (value) View.VISIBLE else View.GONE
        }

    private val currentlyDisplayedDay: Date?
        get() = (supportFragmentManager
            .findFragmentById(R.id.fragment_container) as? RestaurantFragment)
            ?.pagerSelectedDate

    private inline fun showAppWidgetAd(crossinline onDismiss: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(R.string.did_you_know)
            .setMessage(R.string.appwidget_ad)
            .setPositiveButton(R.string.ok, null)
            .setOnDismissListener { onDismiss() }
            .show()
    }

    private fun setRestaurants(restaurants: List<DbRestaurant>) {
        restaurantAdapter.list.setAll(restaurants)
    }

    private fun setDrawerStatus(visible: Boolean) {
        if (visible) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        } else {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun showDishesForRestaurant(restaurant: DbRestaurant, day: Date?, showDishWithGermanName: String?, bottomPadding: Int) {
        restaurant.load(day, showDishWithGermanName, bottomPadding)
    }

    private fun requestWidgetUpdate() {
        val componentName = ComponentName(this, DishesAppWidgetProvider::class.java)
        val appWidgetIds = AppWidgetManager.getInstance(this).getAppWidgetIds(componentName)
        val intent = Intent(this, DishesAppWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        sendBroadcast(intent)
    }

    private fun createViewModelController(mainViewModel: MainViewModel, savedInstanceState: Bundle?) = MainViewModelController(
        repository,
        oneOff,
        mainViewModel,
        intent.getStringExtra(ARG_REQUESTED_RESTAURANT_ID) ?: lastRestaurantId,
        intent.getStringExtra(ARG_REQUESTED_DISH_NAME),
        savedInstanceState?.getDate(keyCurrentlyDisplayedDay) ?: intent.getDateExtra(ARG_REQUESTED_SELECTED_DAY)
    )

    private lateinit var viewModel: MainViewModel
    private lateinit var viewModelController: MainViewModelController
    private lateinit var binding: ActivityMainBinding
    private var lastRestaurantId by StringSharedPreferencesPropertyDelegate(
        sharedPreferences = { getSharedPreferences(prefsFileName, Context.MODE_PRIVATE) },
        key = prefsKeyLastSelectedRestaurant,
        defaultValue = null
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        if (Build.VERSION.SDK_INT >= 29) {
            binding.drawerLayout.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            binding.contentContainer.setOnApplyWindowInsetsListener { _, windowInsets ->
                val systemWindowInsets = windowInsets.systemWindowInsets
                binding.contentContainer.setPadding(systemWindowInsets.left, systemWindowInsets.top, systemWindowInsets.right, 0)
                binding.restaurantList.setPadding(systemWindowInsets.left, systemWindowInsets.top, systemWindowInsets.right, systemWindowInsets.bottom)
                viewModelController.applyBottomPadding(systemWindowInsets.bottom)

                windowInsets.consumeSystemWindowInsets()
            }
        }

        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_store_mall_directory_white_24dp)
        }

        app.appComponent.inject(this)

        viewModel = viewModel(::initialMainViewModel)
        viewModelController = createViewModelController(viewModel, savedInstanceState)

        // Setup the restaurant list in the drawer
        restaurantAdapter.onClickListener = { restaurant, _ ->
            viewModelController.onRestaurantClick(restaurant, currentlyDisplayedDay)
        }
        binding.restaurantList.adapter = restaurantAdapter
        binding.restaurantList.layoutManager = LinearLayoutManager(this)

        viewModel.restaurants.observe(this) {
            setRestaurants(it)
        }
        viewModel.networkError.observe(this) {
            if (it) {
                showNetworkError()
            }
        }
        viewModel.drawerShown.observe(this) {
            setDrawerStatus(it)
        }
        viewModel.showAppWidgetAd.observe(this) {
            if (it) {
                showAppWidgetAd { viewModel.showAppWidgetAd.data = false }
            }
        }
        viewModel.isLoading.observe(this) {
            isLoading = it
        }
        viewModel.restaurantLoadSpec.observe(this) {
            it ?: return@observe

            lastRestaurantId = it.restaurant.id
            showDishesForRestaurant(it.restaurant, it.requestedDay, it.requestedDishName, it.bottomPaddingForSystemWindows)
            it.requestedDay = null
            it.requestedDishName = null
        }

        viewModelController.start()
        requestWidgetUpdate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // We save this manually since RestaurantFragment will be reloaded
        // due to the initial call to the observer of restaurantLoadSpec
        outState.putDate(keyCurrentlyDisplayedDay, currentlyDisplayedDay)
    }

    private fun showNetworkError() {
        Toast.makeText(this, R.string.network_error, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            binding.drawerLayout.toggleDrawer(GravityCompat.START)
            true
        }
        R.id.settings -> {
            val intent = Intent(this, PreferenceActivity::class.java)
            startActivityForResult(intent, requestCodePrefererences)
            true
        }
        R.id.stw_url -> {
            studentenwerkUri.showInCustomTab()
            true
        }
        R.id.opening_hours -> {
            studentenwerkOpeningHoursUri.showInCustomTab()
            true
        }
        R.id.allergens_table -> {
            openAllergensTable()
            true
        }
        R.id.privacy_policy -> {
            Uri.parse(BuildConfig.PRIVACY_POLICY_URL).showInCustomTab()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun Uri.showInCustomTab() {
        CustomTabsIntent.Builder()
            .enableUrlBarHiding()
            .setInstantAppsEnabled(false)
            .setShowTitle(true)
            .setToolbarColor(ContextCompat.getColor(this@MainActivity, R.color.primary))
            .build()
            .launchUrl(this@MainActivity, this)
    }

    private fun openAllergensTable() {
        AlertDialog.Builder(this)
            .setTitle(R.string.allergens_table)
            .setMessage(R.string.allergens_table_content)
            .setPositiveButton(R.string.close, null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestCodePrefererences) {
            viewModelController.onCameBackFromPreferences()
        }
    }

    /**
     * Show a fragment that displays the dishes for the specified restaurant.
     * @param [day] The day to display inside the fragment.
     * @param [showDishWithGermanName] If non-null, the fragment tries to load the dish details for this dish.
     */
    private fun DbRestaurant.load(day: Date?, showDishWithGermanName: String?, bottomPadding: Int) {
        supportActionBar?.title = name
        val restaurantFragment = RestaurantFragment(
            this,
            bottomPadding,
            day ?: midnight,
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
            val requestedRestaurantId = intent.getStringExtra(ARG_REQUESTED_RESTAURANT_ID)
            val requestedDishName = intent.getStringExtra(ARG_REQUESTED_DISH_NAME)
            val requestedSelectedDay = intent.getDateExtra(ARG_REQUESTED_SELECTED_DAY)
            if (requestedRestaurantId != null && (requestedDishName != null || requestedSelectedDay != null)) {
                viewModelController.newRequest(requestedRestaurantId, requestedDishName, requestedSelectedDay)
            }
        }
    }

}
