package com.cbruegg.mensaupb.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbruegg.mensaupb.BR;
import com.cbruegg.mensaupb.R;
import com.cbruegg.mensaupb.databinding.RowRestaurantBinding;
import com.cbruegg.mensaupb.model.Restaurant;

import org.jetbrains.annotations.NotNull;

public class RestaurantAdapter extends ObservableListAdapter<Restaurant, RestaurantAdapter.RestaurantBindingHolder> {

    @Override public RestaurantBindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RestaurantBindingHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_restaurant, parent, false));
    }

    @Override public void onBindViewHolder(@NotNull RestaurantBindingHolder holder, Restaurant item) {
        holder.binding.setVariable(BR.restaurant, item);
        holder.binding.executePendingBindings();
    }

    static class RestaurantBindingHolder extends RecyclerView.ViewHolder {

        public final RowRestaurantBinding binding;

        public RestaurantBindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
