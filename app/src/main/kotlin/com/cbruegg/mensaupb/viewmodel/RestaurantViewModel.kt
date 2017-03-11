package com.cbruegg.mensaupb.viewmodel

import com.cbruegg.mensaupb.model.Restaurant

/**
 * Extension function used for computing
 * an appropriately-sorted list of restaurants
 * applicable for display in the whole app.
 */
fun List<Restaurant>.uiSorted(): List<Restaurant>
        = sortedBy { it.location }.reversed() // Paderborn should be at the top of the list