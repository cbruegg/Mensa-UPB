package com.cbruegg.mensaupb.viewmodel

import com.cbruegg.mensaupb.cache.DbRestaurant

/**
 * Extension function used for computing
 * an appropriately-sorted list of restaurants
 * applicable for display in the whole app.
 */
fun List<DbRestaurant>.uiSorted(): List<DbRestaurant> = sortedBy { it.location }.reversed() // Paderborn should be at the top of the list