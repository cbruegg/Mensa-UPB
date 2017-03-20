package com.cbruegg.mensaupb

import android.content.Context
import com.cbruegg.mensaupb.util.OneOff
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.kotlin.BlockingEntityStore
import io.requery.sql.KotlinEntityDataStore
import io.requery.sql.TableCreationMode
import javax.inject.Singleton

@Module
class AppModule(private val app: MensaApplication) {

    @Provides @Singleton
    fun provideApplication(): MensaApplication = app

    @Provides @Singleton
    fun provideContext(): Context = app

    @Provides @Singleton
    fun provideOneOff(): OneOff = OneOff(app)

    @Provides @Singleton
    fun provideData(): BlockingEntityStore<Persistable> {
        val source = DatabaseSource(app, Models.DEFAULT, 13)
        if (BuildConfig.DEBUG) {
            source.setTableCreationMode(TableCreationMode.DROP_CREATE)
        } else {
            source.setTableCreationMode(TableCreationMode.CREATE)
        }

        return KotlinEntityDataStore(source.configuration)
    }
}