package com.cbruegg.mensaupb.restaurant

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.cbruegg.mensaupb.R
import com.cbruegg.mensaupb.util.lifecycle.LifecycleViewHolder

class DishesViewHolder(itemView: View) : LifecycleViewHolder(itemView) {
    val dishList: RecyclerView = itemView.findViewById(R.id.dishList)
    val noDishesMessage: View = itemView.findViewById(R.id.noDishesMessage)
    val dishProgressBar: ProgressBar = itemView.findViewById(R.id.dishProgressBar)
    val networkErrorMessage: View = itemView.findViewById(R.id.networkErrorMessage)
}