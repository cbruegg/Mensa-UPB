package com.cbruegg.mensaupb.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbruegg.mensaupb.BR;
import com.cbruegg.mensaupb.R;
import com.cbruegg.mensaupb.databinding.RowDishBinding;
import com.cbruegg.mensaupb.model.Dish;

import org.jetbrains.annotations.NotNull;

public class DishAdapter extends ObservableListAdapter<Dish, DishAdapter.DishBindingViewHolder> {
    @Override public void onBindViewHolder(@NotNull DishBindingViewHolder holder, Dish item) {
        holder.binding.setVariable(BR.dish, item);
        holder.binding.executePendingBindings();
    }

    @NonNull @Override public DishBindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType, @NonNull LayoutInflater inflater) {
        return new DishBindingViewHolder(inflater.inflate(R.layout.row_dish, parent, false));
    }

    static class DishBindingViewHolder extends RecyclerView.ViewHolder {

        public final RowDishBinding binding;

        public DishBindingViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }
}
