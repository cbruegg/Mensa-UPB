package com.cbruegg.mensaupb.adapter;

import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cbruegg.mensaupb.R;
import com.cbruegg.mensaupb.databinding.RowDishBinding;
import com.cbruegg.mensaupb.viewmodel.DishViewModel;

/**
 * Adapter responsible for displaying DishViewModels in a RecyclerView.
 */
public class DishViewModelAdapter extends ObservableListAdapter<DishViewModel, DishViewModelAdapter.DishBindingViewHolder> {
    @Override public void onBindViewHolder(@NonNull DishBindingViewHolder holder, DishViewModel item, @NonNull View.OnClickListener onClickListener) {
        holder.binding.setDishViewModel(item);
        holder.binding.setOnClickListener(onClickListener);
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
