package com.cbruegg.mensaupb.model

import com.cbruegg.mensaupb.cache.DbDish

/**
 * Enum of different users of the app. These correspond to the price types in the API.
 */
enum class UserType(internal val id: String) {
    STUDENT("student"), WORKER("worker"), GUEST("guest");

    companion object {
        /**
         * Each UserType has an id that is used by the API. This method retrieves a UserType by its id.
         * Return value will be null if there's no matching element.
         */
        fun findById(id: String): UserType? = values().firstOrNull { it.id == id }
    }

    /**
     * For a Student, select the studentPrice, ...
     */
    fun selectPrice(dish: DbDish): Double = when (this) {
        STUDENT -> dish.studentPrice
        WORKER -> dish.workerPrice
        GUEST -> dish.guestPrice
    }
}