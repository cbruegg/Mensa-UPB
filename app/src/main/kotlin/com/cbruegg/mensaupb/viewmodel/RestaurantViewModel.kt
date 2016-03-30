package com.cbruegg.mensaupb.viewmodel

import com.cbruegg.mensaupb.extensions.sortBy
import com.cbruegg.mensaupb.model.Restaurant

/**
 * Extension function used for computing
 * an appropriately-sorted list of restaurants
 * applicable for display in the whole app.
 */
fun List<Restaurant>.uiSorted(): List<Restaurant>
        = sortBy { first, second -> first.location.compareTo(second.location) }
        .reversed() // Paderborn should be at the top of the list