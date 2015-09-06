package com.cbruegg.mensaupb.extensions

import android.databinding.ObservableArrayList
import android.databinding.ObservableList

fun <T> ObservableArrayList<T>.addOnListChangedCallback(callback: ((ObservableArrayList<T>) -> Unit)) {
    addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {
        override fun onChanged(sender: ObservableArrayList<T>) {
            callback(sender)
        }

        override fun onItemRangeMoved(sender: ObservableArrayList<T>?, fromPosition: Int, toPosition: Int, itemCount: Int) {
        }

        override fun onItemRangeRemoved(sender: ObservableArrayList<T>?, positionStart: Int, itemCount: Int) {
        }

        override fun onItemRangeInserted(sender: ObservableArrayList<T>?, positionStart: Int, itemCount: Int) {
        }

        override fun onItemRangeChanged(sender: ObservableArrayList<T>?, positionStart: Int, itemCount: Int) {
        }
    })

}