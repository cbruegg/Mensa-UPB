package com.cbruegg.mensaupb.downloader

import android.annotation.SuppressLint
import arrow.core.Either
import com.cbruegg.mensaupb.BuildConfig
import com.cbruegg.mensaupb.cache.DbRestaurant
import com.cbruegg.mensaupb.extensions.eitherTryIo
import com.cbruegg.mensaupb.util.threadLocal
import com.cbruegg.mensaupbservice.api.Dish
import com.cbruegg.mensaupbservice.api.Restaurant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

private const val BASE_URL = "https://www.studierendenwerk-pb.de/"

typealias IOEither<T> = Either<IOException, T>

private interface MensaService {
    @Throws(IOException::class)
    @GET("fileadmin/shareddata/access2.php?getrestaurants=1")
    suspend fun restaurants(): Map<String, Map<String, *>>

    @Throws(IOException::class)
    @GET("fileadmin/shareddata/access2.php")
    suspend fun dishes(@Query("date") date: String, @Query("restaurant") restaurantId: String): List<JsonDish>
}

@SuppressLint("SimpleDateFormat")
class Downloader @Inject constructor(originalHttpClient: OkHttpClient) {

    private val httpClient = originalHttpClient.newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .addInterceptor {
                val newUrl = it.request().url.newBuilder().addQueryParameter("id", BuildConfig.API_ID).build()
                it.proceed(it.request().newBuilder().url(newUrl).build())
            }
            .build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(MoshiProvider.moshi))
            .build()

    private val service = retrofit.create(MensaService::class.java)

    private val dateFormat by threadLocal { SimpleDateFormat("yyyy-MM-dd") }

    suspend fun downloadRestaurants(): IOEither<List<Restaurant>> = networkAsync { service.restaurants().mapToRestaurants() }

    suspend fun downloadDishes(restaurant: DbRestaurant, date: Date): IOEither<List<Dish>> = networkAsync {
        service.dishes(dateFormat.format(date), restaurant.id).mapToDishes()
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