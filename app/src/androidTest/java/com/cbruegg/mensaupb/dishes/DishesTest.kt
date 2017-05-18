package com.cbruegg.mensaupb.dishes

import android.os.Parcel
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.cbruegg.mensaupb.cache.DbDish
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.downloader.Repository
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.model.Badge
import com.cbruegg.mensaupb.model.PriceType
import com.cbruegg.mensaupb.model.UserType
import com.cbruegg.mensaupb.nothing
import com.cbruegg.mensaupb.serializeForSql
import com.cbruegg.mensaupb.viewmodel.DishListViewModel
import com.cbruegg.mensaupb.viewmodel.DishViewModel
import com.cbruegg.mensaupb.viewmodel.toDishViewModels
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.funktionale.either.Either
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.*
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class DishesTest {

    val sampleRestaurant = object : DbRestaurant {
        override fun writeToParcel(dest: Parcel?, flags: Int) = nothing()
        override fun describeContents() = nothing()

        override val id: String
            get() = "id"
        override val name: String
            get() = "name"
        override val location: String
            get() = "location"
        override val isActive: Boolean
            get() = true
    }

    val sampleDish = object : DbDish() {
        override val id: Int
            get() = 0
        override val date: Date
            get() = Date(0)
        override val nameDE: String
            get() = "nameDE"
        override val nameEN: String
            get() = "nameEN"
        override val descriptionDE: String?
            get() = "descriptionDE"
        override val descriptionEN: String?
            get() = "descriptionEN"
        override val category: String
            get() = "category"
        override val categoryDE: String
            get() = "categoryDE"
        override val categoryEN: String
            get() = "categoryEN"
        override val subcategoryDE: String
            get() = "subcategoryDE"
        override val subcategoryEN: String
            get() = "subcategoryEN"
        override val studentPrice: Double
            get() = 1.0
        override val workerPrice: Double
            get() = 1.5
        override val guestPrice: Double
            get() = 2.0
        override val allergensStr: String
            get() = "A1"
        override val orderInfo: Int
            get() = 0
        override val badgesStr: String
            get() = listOf(Badge.VEGETARIAN).serializeForSql { it.name }
        override val priceType: PriceType
            get() = PriceType.FIXED
        override val imageUrl: String?
            get() = "http://someurl.de/"
        override val thumbnailImageUrl: String?
            get() = "http://someurl.de/"
        override val restaurant: DbRestaurant
            get() = sampleRestaurant

        override fun writeToParcel(dest: Parcel?, flags: Int) = nothing()

        override fun describeContents() = nothing()

    }

    // TODO Test stale

    @Test fun testPresenterSucc() = runBlocking {
        val date = now
        val downloader = mock(Repository::class.java)
        given(downloader.downloadOrRetrieveDishesAsync(sampleRestaurant, date)).willReturn(async(CommonPool) {
            Either.Right<IOException, List<DbDish>>(listOf(sampleDish)) // TODO Also test exception
        })
        val userType = UserType.STUDENT
        val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishListViewModel> = {
            toDishViewModels(InstrumentationRegistry.getTargetContext(), it)
        }
        val dishNameToShowOnLoad = null
        val presenter = DishesPresenter(downloader, sampleRestaurant, date, userType, dishViewModelCreator, dishNameToShowOnLoad)

        val mockView = mock(DishesView::class.java)
        presenter.attachView(mockView, savedInstanceState = null, runInit = true)

        delay(200)

        verify(downloader).downloadOrRetrieveDishesAsync(sampleRestaurant, date)
        verify(mockView).setShowNoDishesMessage(false)
        verify(mockView, never()).showNetworkError(IOException())
        verify(mockView).showDishes(listOf(sampleDish).dishViewModelCreator(userType))
        verify(mockView).isLoading = true
        verify(mockView).isLoading = false
    }

    @Test fun testPresenterShowArgDish() = runBlocking {
        val date = now
        val downloader = mock(Repository::class.java)
        given(downloader.downloadOrRetrieveDishesAsync(sampleRestaurant, date)).willReturn(async(CommonPool) {
            Either.Right<IOException, List<DbDish>>(listOf(sampleDish))
        })
        val userType = UserType.STUDENT
        val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishListViewModel> = {
            toDishViewModels(InstrumentationRegistry.getTargetContext(), it)
        }
        val dishNameToShowOnLoad = sampleDish.name
        val presenter = DishesPresenter(downloader, sampleRestaurant, date, userType, dishViewModelCreator, dishNameToShowOnLoad)

        val mockView = mock(DishesView::class.java)
        presenter.attachView(mockView, savedInstanceState = null, runInit = true)

        delay(200)

        val dishViewModels = listOf(sampleDish).dishViewModelCreator(userType)
        verify(downloader).downloadOrRetrieveDishesAsync(sampleRestaurant, date)
        verify(mockView).setShowNoDishesMessage(false)
        verify(mockView, never()).showNetworkError(IOException())
        verify(mockView).showDishes(dishViewModels)
        verify(mockView).showDishDetailsDialog(dishViewModels.filterIsInstance<DishViewModel>().first())
    }

    @Test fun testPresenterError() = runBlocking {
        val date = now
        val downloader = mock(Repository::class.java)
        val ex = IOException("ex")
        given(downloader.downloadOrRetrieveDishesAsync(sampleRestaurant, date)).willReturn(async(CommonPool) {
            Either.Left<IOException, List<DbDish>>(ex)
        })
        val userType = UserType.STUDENT
        val dishViewModelCreator: List<DbDish>.(UserType) -> List<DishListViewModel> = {
            toDishViewModels(InstrumentationRegistry.getTargetContext(), it)
        }
        val dishNameToShowOnLoad = null
        val presenter = DishesPresenter(downloader, sampleRestaurant, date, userType, dishViewModelCreator, dishNameToShowOnLoad)

        val mockView = mock(DishesView::class.java)
        presenter.attachView(mockView, savedInstanceState = null, runInit = true)

        delay(200)

        verify(downloader).downloadOrRetrieveDishesAsync(sampleRestaurant, date)
        verify(mockView, never()).setShowNoDishesMessage(false)
        verify(mockView).showNetworkError(ex)
        verify(mockView).showDishes(emptyList())
        verify(mockView).isLoading = true
        verify(mockView).isLoading = false
    }
}