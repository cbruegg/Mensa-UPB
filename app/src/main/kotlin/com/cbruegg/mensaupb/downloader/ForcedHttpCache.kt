package com.cbruegg.mensaupb.downloader

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import android.util.Log
import com.cbruegg.mensaupb.DbThread
import com.cbruegg.mensaupb.compat.OkHttp3Downloader
import com.cbruegg.mensaupb.extensions.md5
import com.cbruegg.mensaupb.extensions.now
import com.cbruegg.mensaupb.util.FileConverter
import com.cbruegg.mensaupb.util.MediaTypeConverter
import io.requery.*
import io.requery.kotlin.BlockingEntityStore
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import okhttp3.*
import okio.Okio
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.util.*

private const val QUERY_PARAM_INDICATOR = "mensaForceCached"
private const val QUERY_PARAM_INDICATOR_VALUE = true.toString()
private const val TAG = "ForcedCache"

/**
 * Convert this to a [Uri] that will
 * forcibly cache in an LRU cache, regardless of any Cache-Control headers.
 */
val Uri.forceCached: Uri
    get() = buildUpon()
            .appendQueryParameter(QUERY_PARAM_INDICATOR, QUERY_PARAM_INDICATOR_VALUE)
            .build()

/**
 * Intercept the chain and check whether the url was created using
 * [forceCached]. If so, it is checked whether a cached file exists
 * and that is returned as a [Response] and the access time is updated.
 * If the file does not exist, the HTTP request will be performed and
 * cached in a file.
 */
fun forceCacheInterceptChain(data: BlockingEntityStore<Persistable>, context: Context, chain: Interceptor.Chain): Response = runBlocking {
    if (chain.request().url().queryParameter(QUERY_PARAM_INDICATOR) != QUERY_PARAM_INDICATOR_VALUE) {
        return@runBlocking chain.proceed(chain.request())
    }

    val dir = File(context.cacheDir, "mensaForceCached")
    if (!dir.exists() && !dir.mkdirs()) throw IOException("Couldn't create cache dir")
    val cacheSize = OkHttp3Downloader.calculateDiskCacheSize(dir)

    val url = chain.request().url()
            .newBuilder()
            .removeAllQueryParameters(QUERY_PARAM_INDICATOR)
            .build()

    val cacheFile = File(dir, url.toString().md5())
    val existingEntry = async(DbThread) {
        data.findByKey(DbForcedCacheEntryEntity::class, cacheFile)?.also {
            it.setLastUsed(now)
            data.update(it)
        }
    }.await()

    if (cacheFile.exists() && existingEntry != null) {
        Log.d(TAG, "Found $cacheFile for $url.")

        val cacheSource = Okio.buffer(Okio.source(cacheFile))
        Response.Builder()
                .request(chain.request())
                .cacheResponse(
                        Response.Builder()
                                .request(chain.request())
                                .code(HttpURLConnection.HTTP_OK)
                                .protocol(Protocol.HTTP_1_1)
                                .build()
                )
                .networkResponse(null)
                .body(
                        ResponseBody.create(existingEntry.contentType, cacheFile.length(), cacheSource)
                )
                .code(HttpURLConnection.HTTP_OK)
                .protocol(Protocol.HTTP_1_1)
                .build()
    } else {
        Log.d(TAG, "Found no cache file for $url.")

        val response = chain.proceed(chain.request())

        if (response.code() != HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Caching for $url aborted due to a non-OK response.")
            response.body()?.close()
            return@runBlocking response
        }

        val contentType = response.body().contentType()
        val contentLength = response.body().contentLength()

        if (cacheFile.exists() && !cacheFile.delete()) throw IOException("Could not delete existing file $cacheFile.")
        if (!cacheFile.createNewFile()) throw IOException("Could not create cache file $cacheFile")

        Okio.buffer(Okio.sink(cacheFile)).use { cacheSink ->
            response.body().source().use { httpSource ->
                httpSource.readAll(cacheSink)
                Log.d(TAG, "Created $cacheFile for $url.")

                launch(DbThread) {
                    // Upsert since cache file could've been deleted by system
                    newDbEntry(cacheFile, contentLength, contentType).also { data.upsert(it) }
                    cleanUpAsync(data, cacheSize)
                }
            }
        }

        val cacheSource = Okio.buffer(Okio.source(cacheFile))
        response.newBuilder()
                .body(ResponseBody.create(contentType, cacheFile.length(), cacheSource))
                .build()
    }

}

/**
 * Create, but don't insert a new [DbForcedCacheEntry]
 */
private fun newDbEntry(cacheFile: File, contentLength: Long, contentType: MediaType) =
        DbForcedCacheEntryEntity().apply {
            setFile(cacheFile)
            setContentType(contentType)
            setLastUsed(now)
            setBytes(contentLength)
        }

/**
 * Limit size
 */
private fun cleanUpAsync(data: BlockingEntityStore<Persistable>, cacheSize: Long) = launch(DbThread) {
    data.withTransaction {
        var sizeSum = 0L
        select(DbForcedCacheEntryEntity::class)
                .orderBy(DbForcedCacheEntryEntity.LAST_USED.desc())
                .get()
                .dropWhile {
                    sizeSum += it.bytes
                    sizeSum <= cacheSize
                }
                .forEach {
                    Log.d(TAG, "Deleted $it (LRU).")
                    it.file.delete()
                    delete(it)
                }
    }
}

@Entity @Table(name = "forced_cache_entries")
interface DbForcedCacheEntry : Persistable, Parcelable {

    @get:Column(name = "file") @get:Key @get:Convert(FileConverter::class)
    val file: File

    @get:Column(name = "content_type") @get:Convert(MediaTypeConverter::class)
    val contentType: MediaType

    @get:Column(name = "bytes")
    val bytes: Long

    @get:Column(name = "last_used")
    val lastUsed: Date

}

