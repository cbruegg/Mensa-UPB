package com.cbruegg.mensaupb.cache

import io.requery.Column
import io.requery.Entity
import io.requery.Key
import io.requery.Persistable
import io.requery.Table

@Entity @Table(name = "restaurants")
interface DbRestaurant : Persistable {
   @get:Column(name = "id") @get:Key val id: String
   @get:Column(name = "name") val name: String
   @get:Column(name = "location") val location: String
   @get:Column(name = "isActive") val isActive: Boolean
}