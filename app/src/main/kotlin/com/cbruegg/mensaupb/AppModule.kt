package com.cbruegg.mensaupb

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.cbruegg.mensaupb.cache.Models
import com.cbruegg.mensaupb.util.OneOff
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.kotlin.BlockingEntityStore
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import java.io.File
import javax.inject.Singleton

@Module
class AppModule(private val app: MensaApplication) {

    @Provides
    @Singleton
    fun provideApplication(): MensaApplication = app

    @Provides
    @Singleton
    fun provideContext(): Context = app

    @Provides
    @Singleton
    fun provideOneOff(): OneOff = OneOff(app)

    @Provides
    @Singleton
    fun provideData(): BlockingEntityStore<Persistable> {
        val source = object : DatabaseSource(app, Models.DEFAULT, 14) {
            override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                if (oldVersion == 13 && newVersion > 13) {
                    // Clear removed cache
                    val forcedCacheTable = "forced_cache_entries"
                    val cursor = db.query(forcedCacheTable, arrayOf("file"), null,
                            null, null, null, null)
                    cursor.use {
                        if (it.moveToFirst()) {
                            do {
                                val path = it.getString(it.getColumnIndex("file"))
                                Log.d("DB", "Deleting $path")
                                File(path).delete()
                            } while (it.moveToNext())
                        }
                    }
                    db.execSQL("DROP TABLE $forcedCacheTable")
                }

                super.onUpgrade(db, oldVersion, newVersion)
            }
        }
        source.setTableCreationMode(TableCreationMode.CREATE_NOT_EXISTS)
        if (BuildConfig.DEBUG) {
            source.setLoggingEnabled(true)
        }

        return KotlinEntityDataStore(source.configuration)
    }
}