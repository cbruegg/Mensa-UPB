package com.cbruegg.mensaupb.cache

import android.databinding.adapters.CalendarViewBindingAdapter.setDate
import android.os.Parcelable
import com.cbruegg.mensaupb.deserializeFromSql
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.model.Badge
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.serializeForSql
import io.requery.*
import java.util.*

const val TABLE_DB_DISH = "dishes"

/**
 * Model representing a dish object.
 */
@Entity @Table(name = TABLE_DB_DISH)
abstract class DbDish : Persistable, Parcelable {

    @get:Column(name = "id")
    @get:Key @get:Generated abstract val id: Int

    @get:Column(name = "date")
    abstract val date: Date

    @get:Column(name = "germanName")
    abstract val germanName: String

    @get:Column(name = "germanDescription")
    abstract val germanDescription: String?

    @get:Column(name = "category")
    abstract val category: String

    @get:Column(name = "germanCategory")
    abstract val germanCategory: String

    @get:Column(name = "germanSubcategory")
    abstract val germanSubcategory: String

    @get:Column(name = "studentPrice")
    abstract val studentPrice: Double

    @get:Column(name = "workerPrice")
    abstract val workerPrice: Double

    @get:Column(name = "guestPrice")
    abstract val guestPrice: Double

    @get:Column(name = "allergens")
    abstract val allergensStr: String

    @get:Column(name = "orderInfo")
    abstract val orderInfo: Int

    @get:Column(name = "badges")
    abstract val badgesStr: String

    @get:Column(name = "priceType")
    abstract val priceType: PriceType

    @get:Column(name = "imageUrl")
    abstract val imageUrl: String?

    @get:Column(name = "thumbnailImageUrl")
    abstract val thumbnailImageUrl: String?

    @get:Column(name = "restaurant") @get:ManyToOne(cascade = arrayOf(CascadeAction.DELETE))
    abstract val restaurant: DbRestaurant

    @get:Transient val badges: List<Badge> by lazy { badgesStr.deserializeFromSql { Badge.valueOf(it) } }
    @get:Transient val allergens: List<String> by lazy { allergensStr.deserializeFromSql { it } }

}

fun Iterable<Dish>.toDbDishes(restaurant: DbRestaurant) = map { dish ->
    DbDishEntity().apply {
        require(restaurant.id == dish.restaurantId) { "dish.restaurantId must equal restaurant parameter." }

        setDate(dish.date.atMidnight())
        setGermanName(dish.germanName)
        setGermanDescription(dish.germanDescription)
        setCategory(dish.category)
        setGermanCategory(dish.germanCategory)
        setGermanSubcategory(dish.germanSubcategory)
        setStudentPrice(dish.studentPrice)
        setWorkerPrice(dish.workerPrice)
        setGuestPrice(dish.guestPrice)
        setAllergensStr(dish.allergens.serializeForSql { it })
        setOrderInfo(dish.orderInfo)
        setBadgesStr(dish.badges.serializeForSql(Badge::name))
        setRestaurant(restaurant)
        setPriceType(dish.priceType)
        setImageUrl(dish.imageUrl)
        setThumbnailImageUrl(dish.thumbnailImageUrl)
    }
}