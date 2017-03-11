package com.cbruegg.mensaupb.cache

import android.os.Parcelable
import io.requery.*

@Entity @Table(name = "restaurants")
interface DbRestaurant : Persistable, Parcelable {
   @get:Column(name = "id") @get:Key val id: String
   @get:Column(name = "name") val name: String
   @get:Column(name = "location") val location: String
   @get:Column(name = "isActive") val isActive: Boolean
}