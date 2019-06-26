package com.cbruegg.mensaupb.downloader

import android.annotation.SuppressLint
import arrow.core.Either
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.extensions.eitherTryIo
import com.cbruegg.mensaupb.util.asStringFormat
import com.cbruegg.mensaupb.util.threadLocal
import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.DishesServiceResult
import com.cbruegg.mensaupbservice.api.Restaurant
import com.cbruegg.mensaupbservice.api.RestaurantsServiceResult
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.protobuf.ProtoBuf
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

private const val BASE_URL = "https://mensaupb.cbruegg.com"

typealias IOEither<T> = Either<IOException, T>

private interface MensaService {
    @Throws(IOException::class)
    @GET("restaurants")
    suspend fun restaurants(): RestaurantsServiceResult

    @Throws(IOException::class)
    @GET("dishes")
    suspend fun dishes(@Query("date") date: String, @Query("restaurantId") restaurantId: String): DishesServiceResult
}

@SuppressLint("SimpleDateFormat")
class Downloader @Inject constructor(originalHttpClient: OkHttpClient) {

    private val httpClient = originalHttpClient.newBuilder()
        .addInterceptor {
            val newUrl = it.request().url().newBuilder().addQueryParameter("apiId", BuildConfig.API_ID).build()
            it.proceed(it.request().newBuilder().url(newUrl).build())
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(ProtoBuf.asStringFormat().asConverterFactory(MediaType.get("application/octet-stream")))
        .build()

    private val service = retrofit.create(MensaService::class.java)

    private val dateFormat by threadLocal { SimpleDateFormat("yyyy-MM-dd") }

    suspend fun downloadRestaurants(): IOEither<List<Restaurant>> = networkAsync { service.restaurants().restaurants }

    suspend fun downloadDishes(restaurant: DbRestaurant, date: Date): IOEither<List<Dish>> = networkAsync {
        service.dishes(dateFormat.format(date), restaurant.id).dishes
    }

    /**
     * Perform the action with the [dispatcher] and wrap it in [eitherTryIo].
     */
    private suspend fun <T : Any> networkAsync(dispatcher: CoroutineDispatcher = Dispatchers.IO, f: suspend () -> T): IOEither<T> =
        withContext(dispatcher) {
            eitherTryIo {
                f()
            }
        }

}