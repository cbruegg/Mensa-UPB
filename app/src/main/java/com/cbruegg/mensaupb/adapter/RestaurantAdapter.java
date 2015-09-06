package com.cbruegg.mensaupb.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbruegg.mensaupb.BR;
import com.cbruegg.mensaupb.R;
import com.cbruegg.mensaupb.databinding.RowRestaurantBinding;
import com.cbruegg.mensaupb.extensions.ExtensionsPackage;
import com.cbruegg.mensaupb.model.Restaurant;

import jet.runtime.typeinfo.JetValueParameter;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantBindingHolder> {

    public final ObservableArrayList<Restaurant> restaurants = new ObservableArrayList<>();

    public RestaurantAdapter() {
        ExtensionsPackage.addOnListChangedCallback(restaurants, new Function1<ObservableArrayList<Restaurant>, Unit>() {
            @Override public Unit invoke(@JetValueParameter(name = "p1") ObservableArrayList<Restaurant> restaurants) {
                notifyDataSetChanged();
                return null;
            }
        });
    }

    @Override public RestaurantBindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RestaurantBindingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_restaurant, parent, false));
    }

    @Override public void onBindViewHolder(RestaurantBindingHolder holder, int position) {
        holder.binding.setVariable(BR.restaurant, restaurants.get(position));
        holder.binding.executePendingBindings();
    }

    @Override public int getItemCount() {
        return restaurants.size();
    }

    static class RestaurantBindingHolder extends RecyclerView.ViewHolder {

        public final RowRestaurantBinding binding;

        public RestaurantBindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
