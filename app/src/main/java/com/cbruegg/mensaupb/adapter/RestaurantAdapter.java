package com.cbruegg.mensaupb.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbruegg.mensaupb.R;
import com.cbruegg.mensaupb.databinding.RowRestaurantBinding;
import com.cbruegg.mensaupb.model.Restaurant;

import org.jetbrains.annotations.NotNull;

public class RestaurantAdapter extends ObservableListAdapter<Restaurant, RestaurantAdapter.RestaurantBindingHolder> {

    @Override public void onBindViewHolder(@NotNull RestaurantBindingHolder holder, Restaurant item, @NonNull View.OnClickListener onClickListener) {
        holder.binding.setRestaurant(item);
        holder.binding.setOnClickListener(onClickListener);
        holder.binding.executePendingBindings();
    }

    @NotNull @Override public RestaurantBindingHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType, @NotNull LayoutInflater inflater) {
        return new RestaurantBindingHolder(inflater.inflate(R.layout.row_restaurant, parent, false));
    }

    static class RestaurantBindingHolder extends RecyclerView.ViewHolder {

        public final RowRestaurantBinding binding;

        public RestaurantBindingHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
