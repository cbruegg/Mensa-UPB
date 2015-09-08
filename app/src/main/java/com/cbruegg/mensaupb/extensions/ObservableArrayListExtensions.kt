package com.cbruegg.mensaupb.extensions

import android.databinding.ObservableArrayList
import android.databinding.ObservableList

/**
 * Adds a callback to be notified when changes to the list occur.
 * @param callback The callback to be notified on list changes
 */
fun <T> ObservableArrayList<T>.addOnListChangedCallback(callback: ((ObservableArrayList<T>) -> Unit)) {
    addOnListChangedCallback(object : ObservableList.OnListChangedCallback<ObservableArrayList<T>>() {
        override fun onChanged(sender: ObservableArrayList<T>) {
            callback(sender)
        }

        override fun onItemRangeMoved(sender: ObservableArrayList<T>, fromPosition: Int, toPosition: Int, itemCount: Int) {
            callback(sender)
        }

        override fun onItemRangeRemoved(sender: ObservableArrayList<T>, positionStart: Int, itemCount: Int) {
            callback(sender)
        }

        override fun onItemRangeInserted(sender: ObservableArrayList<T>, positionStart: Int, itemCount: Int) {
            callback(sender)
        }

        override fun onItemRangeChanged(sender: ObservableArrayList<T>, positionStart: Int, itemCount: Int) {
            callback(sender)
        }
    })

}