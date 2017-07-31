package com.cbruegg.mensaupb.cache

import com.cbruegg.mensaupb.deserializeFromSql
import com.cbruegg.mensaupb.extensions.atMidnight
import com.cbruegg.mensaupb.model.Badge
import com.cbruegg.mensaupb.model.Dish
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.serializeForSql
import com.cbruegg.mensaupb.util.LanguageStringSelector
import io.requery.*
import java.util.*

const val TABLE_DB_DISH = "dishes"

/**
 * Model representing a dish object.
 */
@Entity @Table(name = TABLE_DB_DISH)
abstract class DbDish : Persistable {

    @get:Column(name = "id")
    @get:Key @get:Generated abstract val id: Int

    @get:Column(name = "date")
    abstract val date: Date

    @get:Column(name = "nameDE")
    protected abstract val nameDE: String

    @get:Column(name = "nameEN")
    protected abstract val nameEN: String

    @get:Column(name = "descriptionDE")
    protected abstract val descriptionDE: String?

    @get:Column(name = "descriptionEN")
    protected abstract val descriptionEN: String?

    @get:Column(name = "category")
    abstract val category: String

    @get:Column(name = "categoryDE")
    protected abstract val categoryDE: String

    @get:Column(name = "categoryEN")
    protected abstract val categoryEN: String

    @get:Column(name = "subcategoryDE")
    protected abstract val subcategoryDE: String

    @get:Column(name = "subcategoryEN")
    protected abstract val subcategoryEN: String

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

    @get:Transient val name get() = nameDE

    @get:Transient val displayName: LanguageStringSelector<String> by lazy {
        LanguageStringSelector("en" to nameEN, "de" to nameDE)
    }
    @get:Transient val displayDescription: LanguageStringSelector<String?> by lazy {
        LanguageStringSelector("en" to descriptionEN, "de" to descriptionDE)
    }
    @get:Transient val displayCategory: LanguageStringSelector<String> by lazy {
        LanguageStringSelector("en" to categoryEN, "de" to categoryDE)
    }
    @get:Transient val displaySubcategory: LanguageStringSelector<String> by lazy {
        LanguageStringSelector("en" to subcategoryEN, "de" to subcategoryDE)
    }

}

fun Iterable<Dish>.toDbDishes(restaurant: DbRestaurant) = map { dish ->
    DbDishEntity().apply {
        require(restaurant.id == dish.restaurantId) { "dish.restaurantId must equal restaurant parameter." }

        setDate(dish.date.atMidnight)
        setNameDE(dish.nameDE)
        setNameEN(dish.nameEN)
        setDescriptionDE(dish.descriptionDE)
        setDescriptionEN(dish.descriptionEN)
        setCategory(dish.category)
        setCategoryDE(dish.categoryDE)
        setCategoryEN(dish.categoryEN)
        setSubcategoryDE(dish.subcategoryDE)
        setSubcategoryEN(dish.subcategoryEN)
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