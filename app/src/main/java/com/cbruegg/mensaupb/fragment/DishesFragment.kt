package com.cbruegg.mensaupb.fragment

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import butterknife.bindView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.activity.PreferenceActivity
import com.cbruegg.mensaupb.adapter.DishViewModelAdapter
import com.cbruegg.mensaupb.downloader.Downloader
import com.cbruegg.mensaupb.extensions.setAll
import com.cbruegg.mensaupb.model.Restaurant
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupb.view.DividerItemDecoration
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.Date

class DishesFragment : Fragment() {

    companion object {
        private val ARG_RESTAURANT = "restaurant"
        private val ARG_DATE = "date"

        fun newInstance(restaurant: Restaurant, date: Date): DishesFragment {
            val args = Bundle()
            args.putString(ARG_RESTAURANT, restaurant.serialize())
            args.putLong(ARG_DATE, date.getTime())

            val fragment = DishesFragment()
            fragment.setArguments(args)
            return fragment
        }
    }

    private val dishList: RecyclerView by bindView(R.id.dish_list)
    private val noDishesMessage: TextView by bindView(R.id.no_dishes_message)
    private var subscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_dishes, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DishViewModelAdapter()
        adapter.onClickListener = { dishViewModel, position -> if (dishViewModel.hasBigImage) showDishDetailsDialog(dishViewModel) }
        dishList.setAdapter(adapter)
        dishList.addItemDecoration(DividerItemDecoration(getActivity()))
        dishList.setLayoutManager(LinearLayoutManager(getActivity()))

        val restaurant = Restaurant.deserialize(getArguments().getString(ARG_RESTAURANT))!!
        val date = Date(getArguments().getLong(ARG_DATE))
        val userType = UserType.findById(PreferenceManager.getDefaultSharedPreferences(
                getActivity()).getString(PreferenceActivity.KEY_PREF_USER_TYPE, UserType.STUDENT.id))!!

        subscription = Downloader(getActivity()).downloadOrRetrieveDishes(restaurant, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    noDishesMessage.setVisibility(if (it.isEmpty()) View.VISIBLE else View.GONE)
                    val dishViewModels = it.toDishViewModels(getActivity(), userType)
                    adapter.list.setAll(dishViewModels)
                    subscription?.unsubscribe()
                }
    }

    class DishDetailsDialog(private val dishViewModel: DishViewModel) : DialogFragment() {

        private val imageView: ImageView by bindView(R.id.dish_image)
        private val progressBar: ProgressBar by bindView(R.id.dish_image_progressbar)

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
                = inflater.inflate(R.layout.dialog_dish_details, container, false)

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            setCancelable(true)
            Picasso.with(getActivity())
                    .load(dishViewModel.dish.imageUrl)
                    .fit()
                    .centerInside()
                    .into(imageView, object : Callback {
                        override fun onSuccess() {
                            progressBar.setVisibility(View.GONE)
                        }

                        override fun onError() {
                            Toast.makeText(getActivity(), R.string.could_not_load_image, Toast.LENGTH_SHORT).show()
                            dismiss()
                        }

                    })
        }
    }

    private fun getDisplaySize(): Pair<Int, Int> {
        val display = getActivity().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val size = Point()
        display.getDefaultDisplay().getSize(size)
        return Pair(size.x, size.y)
    }

    private fun showDishDetailsDialog(dishViewModel: DishViewModel) {
        val dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_dish_details, null, false)
        val imageView = dialogView.findViewById(R.id.dish_image) as ImageView
        val progressBar = dialogView.findViewById(R.id.dish_image_progressbar) as ProgressBar

        val alertDialog = AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(true)
                .create()
        alertDialog.show()

        val displaySize = getDisplaySize()


        // fit() doesn't work here, as AlertDialogs don't provide
        // a container for inflation, so some LayoutParams don't work
        Picasso.with(getActivity())
                .load(dishViewModel.dish.imageUrl)
                .resize(displaySize.first, displaySize.second)
                .onlyScaleDown()
                .centerInside()
                .into(imageView, object : Callback {
                    override fun onSuccess() {
                        progressBar.setVisibility(View.GONE)
                    }

                    override fun onError() {
                        Toast.makeText(getActivity(), R.string.could_not_load_image, Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                    }

                })
    }

    override fun onDestroyView() {
        subscription?.unsubscribe()
        super.onDestroyView()
    }
}