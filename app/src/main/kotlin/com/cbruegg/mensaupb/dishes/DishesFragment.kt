package com.cbruegg.mensaupb.dishes

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.userType
import com.cbruegg.mensaupb.adapter.DishViewModelAdapter
import com.cbruegg.mensaupb.app
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.fragment.BaseFragment
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import java.io.IOException
import java.util.*
import javax.inject.Inject

/**
 * Fragment responsible for displaying the dishes of a restaurant at a specified date.
 * The factory method newInstance needs to be used.
 */
class DishesFragment : BaseFragment<DishesView, DishesPresenter>(), DishesView {

    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_DATE = "date"
        private val ARG_GERMAN_DISH_NAME = "german_dish_name"

        /**
         * Construct a new instance of this fragment.
         * @param germanDishName If non-null, look for a matching dish and
         * shows its image.
         * @see DishesFragment
         */
        fun newInstance(restaurant: DbRestaurant, date: Date, germanDishName: String? = null): DishesFragment {
            val fragment = DishesFragment()
            fragment.arguments = Bundle().apply {
                putParcelable(ARG_RESTAURANT, restaurant)
                putLong(ARG_DATE, date.time)
                putString(ARG_GERMAN_DISH_NAME, germanDishName)
            }
            return fragment
        }
    }

    private val dishList: RecyclerView by bindView(R.id.dish_list)
    private val noDishesMessage: TextView by bindView(R.id.no_dishes_message)
    @Inject lateinit var downloader: Downloader

    private val adapter = DishViewModelAdapter()

    override val mvpViewType: Class<DishesView>
        get() = DishesView::class.java

    override fun createPresenter() = DishesPresenter(
            downloader,
            arguments.getParcelable(ARG_RESTAURANT),
            Date(arguments.getLong(ARG_DATE)),
            context.userType,
            { toDishViewModels(context, it) },
            arguments.getString(ARG_GERMAN_DISH_NAME)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app.appComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.fragment_dishes, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter.onClickListener = { dishViewModel, _ -> presenter.onDishClicked(dishViewModel) }
        dishList.adapter = adapter
        dishList.layoutManager = LinearLayoutManager(activity)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun setShowNoDishesMessage(showMessage: Boolean) {
        noDishesMessage.visibility = if (showMessage) View.VISIBLE else View.GONE
    }

    override fun showNetworkError(e: IOException) {
        noDishesMessage.visibility = View.GONE
        e.printStackTrace()
    }

    override fun showDishes(dishes: List<DishViewModel>) {
        adapter.list.setAll(dishes)
    }

    override fun showDishDetailsDialog(dishViewModel: DishViewModel) {
        showDishDetailsDialog(context, dishViewModel)
    }
}